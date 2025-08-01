package com.app.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.DTOs.OrderStatus;
import com.app.DTOs.PaymentMethod;
import com.app.DTOs.PaymentStatus;
import com.app.Repository.AddressRepo;
import com.app.Repository.OrderItemRepo;
import com.app.Repository.OrderRepo;
import com.app.Repository.UserRepo;
import com.app.entity.Address;
import com.app.entity.Cart;
import com.app.entity.CartItem;
import com.app.entity.Order;
import com.app.entity.OrderItem;
import com.app.entity.User;
import com.app.exception.OrderException;
import com.app.exception.UserException;
import com.app.service.CartService;
import com.app.service.OrderItemService;
import com.app.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    private final UserRepo userRepo;
    private final AddressRepo addressRepo;
    private final OrderRepo orderRepo;
    private final CartService cartService;
    private final OrderItemRepo orderItemRepo;

    @Autowired
    public OrderServiceImpl(
            UserRepo userRepo,
            AddressRepo addressRepo,
            OrderRepo orderRepo,
            CartService cartService,
            OrderItemRepo orderItemRepo) {
        this.userRepo = userRepo;
        this.addressRepo = addressRepo;
        this.orderRepo = orderRepo;
        this.cartService = cartService;
        this.orderItemRepo = orderItemRepo;
    }

    @Override
    public Order createOrder(User user, Address shippingAddress) {

        shippingAddress.setUser(user);
        shippingAddress.setId(UUID.randomUUID().toString());
        Address address = addressRepo.save(shippingAddress);
        user.getAddress().add(address);
        userRepo.save(user);

        Cart cart = cartService.findUserCart(user.getId());
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID().toString());
            orderItem.setPrice(item.getPrice());
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSize(item.getSize());
            orderItem.setUserId(item.getUserId());
            orderItem.setDiscountedPrice(item.getDiscountedPrice());

            OrderItem createdOrderItem = orderItemRepo.save(orderItem);

            orderItems.add(createdOrderItem);
        }

        Order createdOrder = new Order();
        createdOrder.setId(UUID.randomUUID().toString());
        createdOrder.setUser(user);
        createdOrder.setOrderItems(orderItems);
        createdOrder.setTotalPrice(cart.getTotalPrice());
        createdOrder.setTotalDiscountedPrice(cart.getTotalDiscountedPrice());
        createdOrder.setDiscount(cart.getDiscount());
        createdOrder.setTotalItem(cart.getTotalItem());

        createdOrder.setShippingAddress(address);
        createdOrder.setOrderDate(LocalDate.now());
        createdOrder.setOrderStatus(OrderStatus.PENDING);
        createdOrder.getPaymentDetails().setStatus(PaymentStatus.PENDING);
        createdOrder.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepo.save(createdOrder);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepo.save(item);
        }

        return savedOrder;

    }

    @Override
    public Order findOrderById(String orderId) throws OrderException {
        Optional<Order> opt = orderRepo.findById(orderId);

        if (opt.isPresent()) {
            return opt.get();
        }
        throw new OrderException("order not exist with id " + orderId);
    }

    @Override
    public List<Order> usersOrderHistory(String userId) {
        List<Order> orders = orderRepo.getUsersOrder(userId);
        return orders;
    }

    @Override
    public Order placedOrder(String orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.PLACED);
        order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
        return order;
    }

    @Override
    public Order confirmedOrder(String orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.getPaymentDetails().setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
        Order savedOrder = orderRepo.save(order);

        // Clear cart
        try {
            cartService.clearCart(order.getUser().getId());
        } catch (UserException e) {
            // Log error but don’t fail order confirmation
            System.err.println(
                    "Failed to clear cart for user ID: " + order.getUser().getId() + ", error: " + e.getMessage());
        }

        return savedOrder;
    }

    @Override
    public Order shippedOrder(String orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.SHIPPED);
        return orderRepo.save(order);
    }

    @Override
    public Order deliveredOrder(String orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.DELIVERED);
        return orderRepo.save(order);
    }

    @Override
    public Order cancledOrder(String orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.CANCELLED);
        return orderRepo.save(order);
    }


    @Override
    public List<Order> getAllOrders() {
        return orderRepo.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Order deleteOrder(String orderId) throws OrderException {
        Order order = findOrderById(orderId);

        orderRepo.deleteById(orderId);
        return order;
    }

}
