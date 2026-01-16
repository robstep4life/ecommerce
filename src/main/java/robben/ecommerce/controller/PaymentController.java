package robben.ecommerce.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import robben.ecommerce.service.StripePaymentService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    public PaymentController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    // JWT required
    @PostMapping("/stripe/session/{orderId}")
    public ResponseEntity<?> createStripeSession(@PathVariable Long orderId) {
        Session session = stripePaymentService.createCheckoutSession(orderId);
        return ResponseEntity.ok(Map.of(
                "sessionId", session.getId(),
                "url", session.getUrl()
        ));
    }

    // Stripe webhook (NO JWT)
    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws SignatureVerificationException {

        Event event = stripePaymentService.verifyWebhook(payload, sigHeader);

        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer deser = event.getDataObjectDeserializer();
            Optional<StripeObject> obj = deser.getObject();
            if (obj.isPresent() && obj.get() instanceof Session session) {
                stripePaymentService.handleCheckoutSessionCompleted(session);
            }
        }

        return ResponseEntity.ok().build();
    }
}
