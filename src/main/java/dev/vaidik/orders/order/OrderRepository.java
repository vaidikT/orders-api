package dev.vaidik.orders.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // No fetch join here: paging + collection fetch would paginate in memory.
    // Items are loaded in batches instead (hibernate.default_batch_fetch_size).
    Page<Order> findByCustomerEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "customer"})
    Optional<Order> findWithItemsById(Long id);
}
