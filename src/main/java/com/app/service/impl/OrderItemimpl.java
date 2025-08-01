package com.app.service.impl;

import org.springframework.stereotype.Service;

import com.app.Repository.OrderItemRepo;
import com.app.entity.OrderItem;
import com.app.service.OrderItemService;

@Service
public class OrderItemimpl implements OrderItemService {

    private OrderItemRepo orderItemRepository;

    public OrderItemimpl(OrderItemRepo orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public OrderItem createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

}
