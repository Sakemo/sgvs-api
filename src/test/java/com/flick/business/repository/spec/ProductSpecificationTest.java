package com.flick.business.repository.spec;

import com.flick.business.core.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSpecificationTest {

    @Test
    void withFilters_whenSearchTermProvided_shouldCreateSpecification() {
        Specification<Product> spec = ProductSpecification.withFilters("coca", null, null, 1L);

        assertThat(spec).isNotNull();
    }
}
