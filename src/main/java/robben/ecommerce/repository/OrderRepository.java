package robben.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import robben.ecommerce.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findAllByUserId(Long userId);

    List<Order> findByUserIdOrderByIdDesc(Long userId);

    Optional<Order> findByStripeSessionId(String stripeSessionId);
}
