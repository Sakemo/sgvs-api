package com.flick.business.repository.spec;

import com.flick.business.core.entity.Customer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {
    public static Specification<Customer> withFilters(String name, Boolean isActive, Boolean hasDebt, Long userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("active"), isActive));
            }
            if (hasDebt != null) {
                if (hasDebt) {
                    predicates.add(cb.greaterThan(root.get("debtBalance"), BigDecimal.ZERO));
                } else {
                    predicates.add(cb.lessThanOrEqualTo(root.get("debtBalance"), BigDecimal.ZERO));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
