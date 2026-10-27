package dev.vaidik.orders.order;

import dev.vaidik.orders.common.ConflictException;
import dev.vaidik.orders.product.Product;
import dev.vaidik.orders.user.Role;
import dev.vaidik.orders.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private Product keyboard;
    private Product mouse;
    private Order order;

    @BeforeEach
    void setUp() {
        keyboard = new Product("KB-1", "Keyboard", null, new BigDecimal("50.00"), 10);
        mouse = new Product("MS-1", "Mouse", null, new BigDecimal("19.99"), 3);
        order = new Order(new User("a@b.dev", "hash", "A", Role.CUSTOMER));
    }

    @Test
    void addItemReservesStockAndUpdatesTotal() {
        order.addItem(keyboard, 2);
        order.addItem(mouse, 1);

        assertThat(order.getTotal()).isEqualByComparingTo("119.99");
        assertThat(keyboard.getStock()).isEqualTo(8);
        assertThat(mouse.getStock()).isEqualTo(2);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void addItemFailsWhenStockIsInsufficient() {
        assertThatThrownBy(() -> order.addItem(mouse, 4))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Not enough stock");
        assertThat(mouse.getStock()).isEqualTo(3);
    }

    @Test
    void cancellingReturnsStock() {
        order.addItem(keyboard, 4);

        order.changeStatus(OrderStatus.CANCELLED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(keyboard.getStock()).isEqualTo(10);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        order.addItem(keyboard, 1);
        order.changeStatus(OrderStatus.PAID);
        order.changeStatus(OrderStatus.SHIPPED);

        assertThatThrownBy(() -> order.changeStatus(OrderStatus.CANCELLED))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SHIPPED to CANCELLED");
    }
}
