package com.flick.business.repository.spec;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.flick.business.core.entity.Expense;
import com.flick.business.core.enums.ExpenseType;

import jakarta.persistence.criteria.Predicate;

public class ExpenseSpecification {
    public static Specification<Expense> withFilters(String name, ZonedDateTime startDate, ZonedDateTime endDate,
            ExpenseType expenseType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
            }
            if (expenseType != null) {
                predicates.add(cb.equal(root.get("expenseType"), expenseType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}