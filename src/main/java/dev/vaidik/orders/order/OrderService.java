package dev.vaidik.orders.order;

import dev.vaidik.orders.common.ConflictException;
import dev.vaidik.orders.common.NotFoundException;
import dev.vaidik.orders.order.OrderDtos.OrderLineRequest;
import dev.vaidik.orders.order.OrderDtos.OrderResponse;
import dev.vaidik.orders.order.OrderDtos.PlaceOrderRequest;
import dev.vaidik.orders.product.Product;
import dev.vaidik.orders.product.ProductRepository;
import dev.vaidik.orders.user.User;
import dev.vaidik.orders.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;

    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users) {
        this.orders = orders;
        this.products = products;
        this.users = users;
    }

    @Transactional
    public OrderResponse place(String customerEmail, PlaceOrderRequest request) {
        User customer = users.findByEmail(customerEmail)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        Set<Long> seen = new HashSet<>();
        Order order = new Order(customer);
        for (OrderLineRequest line : request.items()) {
            if (!seen.add(line.productId())) {
                throw new ConflictException("Product " + line.productId() + " appears more than once");
            }
            Product product = products.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("Product " + line.productId() + " not found"));
            order.addItem(product, line.quantity());
        }
        return OrderResponse.from(orders.save(order));
    }

    public Page<OrderResponse> listMine(String customerEmail, Pageable pageable) {
        return orders.findByCustomerEmail(customerEmail, pageable).map(OrderResponse::from);
    }

    public OrderResponse get(Long id, String requesterEmail, boolean isAdmin) {
        Order order = find(id);
        assertCanAccess(order, requesterEmail, isAdmin);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancel(Long id, String requesterEmail, boolean isAdmin) {
        Order order = find(id);
        assertCanAccess(order, requesterEmail, isAdmin);
        order.changeStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = find(id);
        order.changeStatus(status);
        return OrderResponse.from(order);
    }

    private Order find(Long id) {
        return orders.findWithItemsById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }

    private static void assertCanAccess(Order order, String email, boolean isAdmin) {
        if (!isAdmin && !order.isOwnedBy(email)) {
            throw new AccessDeniedException("Not your order");
        }
    }
}
