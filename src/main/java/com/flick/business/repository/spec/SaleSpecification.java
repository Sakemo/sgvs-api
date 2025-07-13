package com.flick.business.repository.spec;

import com.flick.business.core.entity.Sale;
import com.flick.business.core.entity.SaleItem;
import com.flick.business.core.enums.PaymentMethod;
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
            Long productId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Use LEFT JOIN FETCH to avoid N+1 problems when loading sales
            root.fetch("customer", JoinType.LEFT);
            query.distinct(true);

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
            if (productId != null) {
                // Subquery to check if a sale contains a specific product
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