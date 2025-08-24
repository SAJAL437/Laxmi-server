package com.app.service;

import java.util.List;

import com.app.entity.Address;
import com.app.entity.Order;
import com.app.entity.User;
import com.app.exception.OrderException;

public interface OrderService {

    public Order createOrder(User user, Address shippingAddress);

    public Order findOrderById(String orderId) throws OrderException;

    public List<Order> usersOrderHistory(String userId);

    public Order placedOrder(String orderId) throws OrderException;

    public Order confirmedOrder(String orderId) throws OrderException;

    public Order shippedOrder(String orderId) throws OrderException;

    public Order deliveredOrder(String orderId) throws OrderException;

    public Order cancelledOrder(String orderId) throws OrderException;

    public List<Order> getAllOrders();


    public Order deleteOrder(String orderId) throws OrderException;
}
