package com.flick.business.service.security;

import com.flick.business.repository.security.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountDeletionService {

    private final EntityManager entityManager;
    private final UserRepository userRepository;

    @Transactional
    public void deleteAccountAndRelatedData(Long userId) {
        // Payment dependencies (payment_sales -> payments -> customers)
        entityManager.createNativeQuery("""
                DELETE FROM payment_sales
                WHERE payment_id IN (
                    SELECT p.id
                    FROM payments p
                    JOIN customers c ON c.id = p.customer_id
                    WHERE c.user_id = :userId
                )
                """)
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.createNativeQuery("""
                DELETE FROM payments
                WHERE customer_id IN (
                    SELECT id FROM customers WHERE user_id = :userId
                )
                """)
                .setParameter("userId", userId)
                .executeUpdate();

        // Sales dependencies (sale_items -> sales)
        entityManager.createNativeQuery("""
                DELETE FROM sale_items
                WHERE sale_id IN (
                    SELECT id FROM sales WHERE user_id = :userId
                )
                """)
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM sales WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        // Expense dependencies (restock_items -> expenses)
        entityManager.createNativeQuery("""
                DELETE FROM restock_items
                WHERE expense_id IN (
                    SELECT id FROM expenses WHERE user_id = :userId
                )
                """)
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM expenses WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        // Core entities with direct user FK
        entityManager.createNativeQuery("DELETE FROM products WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM customers WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM providers WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM categories WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM general_settings WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        userRepository.deleteById(userId);
    }
}
