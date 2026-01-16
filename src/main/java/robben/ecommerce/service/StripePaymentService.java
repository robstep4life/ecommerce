package robben.ecommerce.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robben.ecommerce.entity.*;
import robben.ecommerce.repository.*;

import java.util.List;

@Service
public class StripePaymentService {

    private final String webhookSecret;
    private final String successUrl;
    private final String cancelUrl;

    private final CurrentUserService currentUserService;
    private final OrderRepository orders;
    private final OrderItemRepository orderItems;
    private final ProductRepository products;
    private final CartRepository carts;
    private final CartItemRepository cartItems;

    public StripePaymentService(
            @Value("${stripe.secret-key}") String secretKey,
            @Value("${stripe.webhook-secret}") String webhookSecret,
            @Value("${stripe.success-url}") String successUrl,
            @Value("${stripe.cancel-url}") String cancelUrl,
            CurrentUserService currentUserService,
            OrderRepository orders,
            OrderItemRepository orderItems,
            ProductRepository products,
            CartRepository carts,
            CartItemRepository cartItems
    ) {
        Stripe.apiKey = secretKey;
        this.webhookSecret = webhookSecret;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        this.currentUserService = currentUserService;
        this.orders = orders;
        this.orderItems = orderItems;
        this.products = products;
        this.carts = carts;
        this.cartItems = cartItems;
    }

    @Transactional
    public Session createCheckoutSession(Long orderId) {
        User user = currentUserService.requireUser();

        Order order = orders.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Order not payable");
        }

        List<OrderItem> items = orderItems.findByOrderId(order.getId());

        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata("orderId", String.valueOf(order.getId()));

        for (OrderItem it : items) {
            long unitAmount = it.getUnitPrice().movePointRight(2).longValueExact();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(it.getQuantity().longValue())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency(order.getCurrency().toLowerCase())
                                            .setUnitAmount(unitAmount)
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(it.getName())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            params.addLineItem(lineItem);
        }

        try {
            Session session = Session.create(params.build());
            order.setStripeSessionId(session.getId());
            orders.save(order);
            return session;
        } catch (Exception e) {
            throw new IllegalArgumentException("Stripe error: " + e.getMessage());
        }
    }

    public Event verifyWebhook(String payload, String signatureHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    }

    @Transactional
    public void handleCheckoutSessionCompleted(Session session) {
        String sessionId = session.getId();

        Order order = orders.findByStripeSessionId(sessionId).orElse(null);
        if (order == null) return;

        if (order.getStatus() == OrderStatus.PAID) return; // idempotent

        order.setStatus(OrderStatus.PAID);
        if (session.getPaymentIntent() != null) {
            order.setStripePaymentIntentId(session.getPaymentIntent());
        }
        orders.save(order);

        // reduce inventory
        List<OrderItem> items = orderItems.findByOrderId(order.getId());
        for (OrderItem it : items) {
            Product p = products.findById(it.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            int newInv = p.getInventory() - it.getQuantity();
            if (newInv < 0) throw new IllegalArgumentException("Inventory negative: " + p.getName());

            p.setInventory(newInv);
            products.save(p);
        }

        // clear cart
        Long userId = order.getUser().getId();
        carts.findByUserId(userId).ifPresent(cart -> {
            List<CartItem> cartItemList = cartItems.findByCartId(cart.getId());
            cartItems.deleteAll(cartItemList);
        });
    }
}
