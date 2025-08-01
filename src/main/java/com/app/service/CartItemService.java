package com.app.service;

import com.app.entity.Cart;
import com.app.entity.CartItem;
import com.app.entity.Product;
import com.app.exception.CartItemException;
import com.app.exception.UserException;

public interface CartItemService {

    public CartItem createCartItem(CartItem cartItem);

    public CartItem updateCartItem(String id, String userId, CartItem cartItem) throws CartItemException, UserException;

    public CartItem isCartItemExist(Cart cart, Product product, String size, String userId);

    public void removeCartItem(String userId, String cartItemId) throws CartItemException, UserException;

    public CartItem findCartItemById(String cartItemId) throws CartItemException;
}
