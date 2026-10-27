package dev.vaidik.orders.product;

import dev.vaidik.orders.common.ConflictException;
import dev.vaidik.orders.common.NotFoundException;
import dev.vaidik.orders.product.ProductDtos.ProductRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private final ProductRequest request =
            new ProductRequest("KB-1", "Keyboard", "Tenkeyless", new BigDecimal("50.00"), 5);

    @Test
    void createSavesNewProduct() {
        when(repository.existsBySku("KB-1")).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.create(request);

        assertThat(response.sku()).isEqualTo("KB-1");
        assertThat(response.stock()).isEqualTo(5);
    }

    @Test
    void createRejectsDuplicateSku() {
        when(repository.existsBySku("KB-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updateRejectsSkuChange() {
        Product existing = new Product("OTHER", "Old", null, BigDecimal.ONE, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SKU cannot be changed");
    }

    @Test
    void getThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }
}
