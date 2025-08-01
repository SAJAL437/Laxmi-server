package com.app.Repository;

import com.app.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, String> {

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus IN ('PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED')")
    List<Order> getUsersOrder(@Param("userId") String userId);

    List<Order> findAllByOrderByCreatedAtDesc();
}