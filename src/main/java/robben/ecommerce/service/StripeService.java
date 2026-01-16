package robben.ecommerce.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class StripeService {

    // Mock for now (real Stripe next)
    public String createCheckoutSession(Long orderId, BigDecimal amount) {
        return "https://fake-stripe-checkout.com/session/" + UUID.randomUUID();
    }
}
