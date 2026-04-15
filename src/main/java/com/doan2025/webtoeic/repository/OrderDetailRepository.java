package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.OrderDetail;
import com.doan2025.webtoeic.domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query("""
                    SELECT od
                    FROM OrderDetail od
                    WHERE od.orders.id=:orderId
            """)
    Optional<OrderDetail> findByOrderId(Long orderId);

    @Query("""
                    SELECT CASE WHEN COUNT(od) > 0 THEN true ELSE false END
                    FROM OrderDetail od
                    WHERE od.course.id=:id AND od.orders.user.email=:email
                          AND od.orders.status != :#{T(com.doan2025.webtoeic.constants.enums.EStatusOrder).CANCELLED}
            """)
    boolean existsByUserAndCourse(String email, Long id);

    void deleteByOrders(Orders orders);
}
