package com.flick.business.repository.spec;

import com.flick.business.core.entity.Sale;
import com.flick.business.core.entity.SaleItem;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.core.enums.PaymentStatus;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleSpecification {

    public static Specification<Sale> withFilters(
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            Long customerId,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            Long productId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("customer", JoinType.LEFT);
                root.fetch("items", JoinType.LEFT).fetch("product", JoinType.LEFT);
                query.distinct(true);
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("saleDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("saleDate"), endDate));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            if (paymentMethod != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), paymentMethod));
            }
            if (paymentStatus != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            }
            if (productId != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<SaleItem> saleItemRoot = subquery.from(SaleItem.class);
                subquery.select(saleItemRoot.get("sale").get("id"));
                subquery.where(cb.equal(saleItemRoot.get("product").get("id"), productId));
                predicates.add(root.get("id").in(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}