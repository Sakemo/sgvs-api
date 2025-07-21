package com.flick.business.service;

import com.flick.business.api.dto.request.ProductRequest;
import com.flick.business.api.mapper.ProductMapper;
import com.flick.business.core.entity.Category;
import com.flick.business.core.entity.GeneralSettings;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.Provider;
import com.flick.business.core.enums.UnitOfSale;
import com.flick.business.core.enums.settings.StockControlType;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ProviderService providerService;

    @InjectMocks
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Product> productArgumentCaptor;
    @Captor
    private ArgumentCaptor<Pageable> pageableArgumentCaptor;
    @Captor
    private ArgumentCaptor<Specification<Product>> specificationArgumentCaptor;

    private Product product;
    private Category category;
    private Provider provider;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Beverages");

        provider = new Provider();
        provider.setId(1L);
        provider.setName("Global Soda Inc.");

        product = Product.builder().id(1L).name("Original Soda").description("A classic soda")
                .salePrice(new BigDecimal("5.00")).stockQuantity(new BigDecimal("100")).unitOfSale(UnitOfSale.UNIT)
                .category(category).provider(provider).active(true).manageStock(true).build();
        productRequest = new ProductRequest("New Soda", "New description", "123456", new BigDecimal("200"),
                new BigDecimal("7.50"), new BigDecimal("3.00"), UnitOfSale.UNIT, true, true, 1L, 1L);
    }

    @Nested
    @DisplayName("Product Creation & Update")
    class CreationAndUpdate {
        @Test
        @DisplayName("should save a new product when request is valid")
        void save_withValidRequest_shouldSaveAndReturnProduct() {
            when(categoryService.findEntityById(1L)).thenReturn(category);
            when(providerService.findById(1L)).thenReturn(provider);
            when(productMapper.toEntity(any(), any(), any())).thenReturn(product);
            when(productRepository.save(any(Product.class))).thenReturn(product);

            productService.save(productRequest);

            verify(productRepository).save(productArgumentCaptor.capture());
            Product capturedProduct = productArgumentCaptor.getValue();

            assertThat(capturedProduct.getCategory().getName()).isEqualTo("Beverages");
            assertThat(capturedProduct.isManageStock()).isTrue();
        }

        @Test
        @DisplayName("should update an existing product successfully")
        void update_withValidRequest_shouldUpdateProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(categoryService.findEntityById(1L)).thenReturn(category);
            when(providerService.findById(1L)).thenReturn(provider);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            productService.update(1L, productRequest);

            verify(productMapper).updateEntityFromRequest(eq(productRequest), eq(product), eq(category), eq(provider));
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when updating non-existent product")
        void update_withNonExistentId_throwsResourceNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.update(99L, productRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found with ID: 99");
        }
    }

    @Nested
    @DisplayName("Product Listing, Filtering & Sorting")
    class Listing {
        @Test
        @DisplayName("should call repository with correct specification and pageable for default sort")
        void listProducts_withFiltersAndDefaultSort_callsRepositoryCorrectly() {
            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(product)));

            productService.listProducts("Soda", 1L, "name_asc", 0, 10);

            verify(productRepository).findAll(specificationArgumentCaptor.capture(), pageableArgumentCaptor.capture());

            Pageable capturedPageable = pageableArgumentCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
            assertThat(capturedPageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("should call repository with correct pageable for descending price sort")
        void listProducts_withPriceDescSort_appliesCorrectSort() {
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

            productService.listProducts(null, null, "price_desc", 1, 20);

            verify(productRepository).findAll(any(Specification.class), pageableArgumentCaptor.capture());

            Pageable capturedPageable = pageableArgumentCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(1);
            assertThat(capturedPageable.getPageSize()).isEqualTo(20);
            assertThat(capturedPageable.getSort().getOrderFor("salePrice").getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("should call 'findAllByMostSold' when orderBy is 'mostSold'")
        void listProducts_whenOrderByMostSold_callsCorrectRepositoryMethod() {
            when(productRepository.findAllByMostSold(any(), any(), any(Pageable.class))).thenReturn(Page.empty());

            productService.listProducts("Soda", 1L, "mostSold", 0, 10);

            verify(productRepository).findAllByMostSold(eq("Soda"), eq(1L), any(Pageable.class));
            verify(productRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should call 'findAllByLeastSold' when orderBy is 'leastSold'")
        void listProducts_whenOrderByLeastSold_callsCorrectRepositoryMethod() {
            when(productRepository.findAllByLeastSold(any(), any(), any(Pageable.class))).thenReturn(Page.empty());

            productService.listProducts(null, null, "leastSold", 0, 10);

            verify(productRepository).findAllByLeastSold(isNull(), isNull(), any(Pageable.class));
            verify(productRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Product Actions")
    class Actions {
        @Test
        @DisplayName("should toggle active status from true to false")
        void toggleActiveStatus_fromTrueToFalse_updatesProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            productService.toggleActiveStatus(1L);

            verify(productRepository).save(productArgumentCaptor.capture());
            assertThat(productArgumentCaptor.getValue().isActive()).isFalse();
        }
    }
}