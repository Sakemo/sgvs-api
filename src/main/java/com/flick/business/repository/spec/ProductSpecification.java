package com.flick.business.repository.spec;

import com.flick.business.core.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
  public static Specification<Product> withFilters(String name, Long categoryId, Long userId) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (query != null) {
        query.distinct(true);
      }
      predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

      if (name != null && !name.trim().isEmpty()) {
        predicates.add(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
      }

      if (categoryId != null) {
        predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
