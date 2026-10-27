package dev.vaidik.orders.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderLineRequest(
            @NotNull Long productId,
            @NotNull @Min(1) @Max(1000) Integer quantity) {
    }

    public record PlaceOrderRequest(
            @NotEmpty @Size(max = 50) List<@Valid OrderLineRequest> items) {
    }

    public record StatusUpdateRequest(@NotNull OrderStatus status) {
    }

    public record OrderLineResponse(Long productId, String productName, int quantity, BigDecimal unitPrice) {
    }

    public record OrderResponse(Long id, String customerEmail, OrderStatus status, BigDecimal total,
                                Instant createdAt, List<OrderLineResponse> items) {

        static OrderResponse from(Order order) {
            List<OrderLineResponse> lines = order.getItems().stream()
                    .map(i -> new OrderLineResponse(i.getProduct().getId(), i.getProduct().getName(),
                            i.getQuantity(), i.getUnitPrice()))
                    .toList();
            return new OrderResponse(order.getId(), order.getCustomer().getEmail(), order.getStatus(),
                    order.getTotal(), order.getCreatedAt(), lines);
        }
    }
}
