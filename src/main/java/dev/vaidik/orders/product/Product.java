package dev.vaidik.orders.product;

import dev.vaidik.orders.common.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    /** Optimistic locking: two orders racing for the last item can't both win. */
    @Version
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public Product(String sku, String name, String description, BigDecimal price, int stock) {
        this.sku = sku;
        update(name, description, price, stock);
        this.createdAt = this.updatedAt;
    }

    public void update(String name, String description, BigDecimal price, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.updatedAt = Instant.now();
    }

    /** Reserves stock for an order; fails instead of going negative. */
    public void decreaseStock(int quantity) {
        if (quantity > stock) {
            throw new ConflictException("Not enough stock for product " + sku
                    + " (requested " + quantity + ", available " + stock + ")");
        }
        this.stock -= quantity;
        this.updatedAt = Instant.now();
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
