package com.app.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.app.Repository.CartItemRepo;
import com.app.Repository.CartRepo;
import com.app.entity.Cart;
import com.app.entity.CartItem;
import com.app.entity.Product;
import com.app.entity.User;
import com.app.exception.CartItemException;
import com.app.exception.UserException;
import com.app.service.CartItemService;
import com.app.service.UserServices;

@Service
public class CartItemImpl implements CartItemService {

    private CartItemRepo cartItemRepo;
    private UserServices userService;
    private CartRepo cartRepo;

    public CartItemImpl(CartItemRepo cartItemRepo, @Lazy UserServices userService, CartRepo cartRepo) {
        this.cartItemRepo = cartItemRepo;
        this.userService = userService;
        this.cartRepo = cartRepo;
    }

    @Override
    public CartItem createCartItem(CartItem cartItem) {
        cartItem.setId(UUID.randomUUID().toString());
        cartItem.setQuantity(1);
        cartItem.setDiscountedPrice(cartItem.getProduct().getDiscountedPrice() * cartItem.getQuantity());
        CartItem createdCartItem = cartItemRepo.save(cartItem);
        return createdCartItem;
    }

    @Override
    public CartItem updateCartItem(String id, String userId, CartItem cartItem)
            throws CartItemException, UserException {

        CartItem item = findCartItemById(id);
        User user = userService.getUserByid(userId);

        if (user.getId().equals(userId)) {
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(item.getQuantity() * item.getProduct().getPrice());
            item.setDiscountedPrice(item.getQuantity() * item.getProduct().getDiscountedPrice());

            return cartItemRepo.save(item);
        } else {
            throw new CartItemException("You can't update  another users cart_item");
        }

    }

    @Override
    public CartItem isCartItemExist(Cart cart, Product product, String size, String userId) {
        CartItem cartItem = cartItemRepo.isCartItemExist(cart, product, size, userId);
        return cartItem;
    }

    @Override
    public void removeCartItem(String userId, String cartItemId) throws CartItemException, UserException {

        CartItem cartItem = findCartItemById(cartItemId);

        User user = userService.getUserByid(cartItem.getUserId());
        User reqUser = userService.getUserByid(userId);

        if (user.getId().equals(reqUser.getId())) {
            cartItemRepo.deleteById(cartItem.getId());
        } else {
            throw new CartItemException("You can't delete another users cart_item");
        }

    }

    @Override
    public CartItem findCartItemById(String cartItemId) throws CartItemException {
        Optional<CartItem> opt = cartItemRepo.findById(cartItemId);

        if (opt.isPresent()) {
            return opt.get();
        }
        throw new CartItemException("cartItem not found with id : " + cartItemId);

    }

}
