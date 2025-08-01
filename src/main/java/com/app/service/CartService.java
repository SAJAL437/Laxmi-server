package com.app.service;

import com.app.entity.Cart;
import com.app.entity.CartItem;
import com.app.entity.User;
import com.app.exception.ProductException;
import com.app.exception.UserException;
import com.app.request.AddItemRequest;

public interface CartService {

    public Cart createCart(User user);

    public CartItem addCartItem(String userId, AddItemRequest req)throws ProductException;

    public Cart findUserCart(String userId);

    void clearCart(String userId) throws UserException;
}
