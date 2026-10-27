package dev.vaidik.orders.order;

import dev.vaidik.orders.common.ConflictException;
import dev.vaidik.orders.product.Product;
import dev.vaidik.orders.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity(name = "PurchaseOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    public Order(User customer) {
        this.customer = customer;
        this.status = OrderStatus.PENDING;
        this.total = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    /** Adds a line, reserving stock and keeping the total in sync. */
    public void addItem(Product product, int quantity) {
        product.decreaseStock(quantity);
        OrderItem item = new OrderItem(this, product, quantity);
        items.add(item);
        total = total.add(item.lineTotal());
    }

    public void changeStatus(OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new ConflictException("Cannot change order status from " + status + " to " + next);
        }
        if (next == OrderStatus.CANCELLED) {
            items.forEach(i -> i.getProduct().increaseStock(i.getQuantity()));
        }
        status = next;
    }

    public boolean isOwnedBy(String email) {
        return customer.getEmail().equals(email);
    }

    public Long getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
