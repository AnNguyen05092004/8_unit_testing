package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.constants.enums.EStatusOrder;
import com.doan2025.webtoeic.domain.Orders;
import com.doan2025.webtoeic.dto.SearchOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    @Query("""
            SELECT o
            FROM Orders o
            WHERE o.user.email = :email
            """)
    Optional<List<Orders>> findByEmail(String email);


    @Query("""
                    SELECT o FROM Orders o
                    JOIN OrderDetail  od ON o.id = od.orders.id
                    WHERE o.user.email = :email
                    AND (COALESCE(:#{#dto.statusOrder}, NULL) IS NULL OR o.status IN (:#{#dto.statusOrder}) )
                    AND (COALESCE(:#{#dto.searchString}, NULL) IS NULL
                            OR LOWER(CAST(od.course.title as string)) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))
                            OR LOWER(CAST(od.course.description as string)) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))
                            OR LOWER(CAST(CONCAT(od.course.author.firstName, ' ', od.course.author.lastName) as string)) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))
                            )
                    AND ( (COALESCE(:#{#dto.fromDate}, NULL) IS NULL AND COALESCE(:#{#dto.toDate}, NULL) IS NULL)
                          OR (COALESCE(:#{#dto.fromDate}, NULL) IS NULL AND COALESCE(:#{#dto.toDate}, NULL) IS NOT NULL AND o.createdAt <= :#{#dto.toDate})
                          OR (COALESCE(:#{#dto.fromDate}, NULL) IS NOT NULL AND COALESCE(:#{#dto.toDate}, NULL) IS NULL AND o.createdAt >= :#{#dto.fromDate})
                          OR o.createdAt BETWEEN :#{#dto.fromDate} AND :#{#dto.toDate}
                          )
            """)
    Page<Orders> findOwnOrders(SearchOrderDto dto, String email, Pageable pageable);

    @Query("""
                    SELECT COUNT(o) FROM Orders o
                    WHERE (COALESCE(:email, NULL) IS NULL OR o.user.email = :email )
                    AND (COALESCE(:statusOrder, NULL) IS NULL OR :statusOrder = o.status)
            """)
    BigDecimal countOrders(EStatusOrder statusOrder, String email);

    @Query("""
                    SELECT SUM(o.totalAmount) FROM Orders o
                    WHERE (COALESCE(:email, NULL) IS NULL OR o.user.email = :email )
                    AND :#{T(com.doan2025.webtoeic.constants.enums.EStatusOrder).COMPLETED} = o.status
            """)
    BigDecimal totalPurchases(String email);
}
