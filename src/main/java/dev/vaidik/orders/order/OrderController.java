package dev.vaidik.orders.order;

import dev.vaidik.orders.order.OrderDtos.OrderResponse;
import dev.vaidik.orders.order.OrderDtos.PlaceOrderRequest;
import dev.vaidik.orders.order.OrderDtos.StatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place an order for the logged-in customer")
    public OrderResponse place(Authentication auth, @Valid @RequestBody PlaceOrderRequest request) {
        return service.place(auth.getName(), request);
    }

    @GetMapping
    @Operation(summary = "List my orders (newest first)")
    public Page<OrderResponse> mine(Authentication auth,
                                    @PageableDefault(size = 20, sort = "createdAt",
                                            direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listMine(auth.getName(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order (owner or admin)")
    public OrderResponse get(Authentication auth, @PathVariable Long id) {
        return service.get(id, auth.getName(), isAdmin(auth));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order and return its stock (owner or admin)")
    public OrderResponse cancel(Authentication auth, @PathVariable Long id) {
        return service.cancel(id, auth.getName(), isAdmin(auth));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move an order through its lifecycle (admin)")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, request.status());
    }

    private static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
