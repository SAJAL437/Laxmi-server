package com.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app.DTOs.OrderStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String id;

    private String orderId;

    @ManyToOne
    @NotNull
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @NotNull
    private LocalDate orderDate = LocalDate.now();

    private LocalDate deliveryDate;

    @OneToOne
    private Address shippingAddress;

    @Embedded
    private PaymentDetails paymentDetails = new PaymentDetails();

    private int totalPrice;

    private int totalDiscountedPrice;

    private int discount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private int totalItem;

    private LocalDateTime createdAt = LocalDateTime.now();
}