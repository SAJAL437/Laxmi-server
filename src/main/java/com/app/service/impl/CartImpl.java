package com.app.service.impl;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.app.Repository.CartItemRepo;
import com.app.Repository.CartRepo;
import com.app.entity.Cart;
import com.app.entity.CartItem;
import com.app.entity.Product;
import com.app.entity.User;
import com.app.exception.ProductException;
import com.app.exception.UserException;
import com.app.request.AddItemRequest;
import com.app.service.CartItemService;
import com.app.service.CartService;
import com.app.service.ProductService;
import com.app.service.UserServices;

@Service
public class CartImpl implements CartService {

    private CartRepo cartRepo;
    private CartItemRepo cartItemRepo;
    private CartItemService cartItemService;
    private ProductService productService;
    private UserServices UserServices;

    public CartImpl(CartRepo cartRepo, CartItemService cartItemService, ProductService productService,
            @Lazy UserServices userServices, CartItemRepo cartItemRepo) {
        this.cartRepo = cartRepo;
        this.productService = productService;
        this.cartItemService = cartItemService;
        this.UserServices = userServices;
        this.cartItemRepo = cartItemRepo;
    }

    @Override
    public Cart createCart(User user) {

        Cart cart = new Cart();
        cart.setId(UUID.randomUUID().toString());
        cart.setUser(user);
        Cart createdCart = cartRepo.save(cart);
        return createdCart;
    }

    @Override

    public CartItem addCartItem(String userId, AddItemRequest req) throws ProductException {
        Cart cart = cartRepo.findByUserId(userId);
        if (cart == null) {
            User user = UserServices.getUserByid(userId); // Assume this method exists
            cart = createCart(user);
        }

        Product product = productService.findProductById(req.getProductId());
        if (product == null) {
            throw new ProductException("Product not found with id: " + req.getProductId());
        }
        CartItem isPresent = cartItemService.isCartItemExist(cart, product, req.getSize(), userId);

        if (isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            int quantity = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;
            cartItem.setQuantity(quantity);
            cartItem.setPrice(quantity * product.getPrice());
            cartItem.setDiscountedPrice(quantity * product.getDiscountedPrice());
            cartItem.setSize(req.getSize());
            cartItem.setUserId(userId);

            CartItem createdCartItem = cartItemService.createCartItem(cartItem);
            cart.getCartItems().add(createdCartItem);
            cartRepo.save(cart);
            return createdCartItem;
        }
        return isPresent;
    }

    @Override
    public Cart findUserCart(String userId) {
        Cart cart = cartRepo.findByUserId(userId);

        if (cart == null) {
            throw new RuntimeException("Cart not found for user: " + userId);
        }

        int totalPrice = 0;
        int totalDiscountedprice = 0;
        int totalItem = 0;

        for (CartItem cartItem : cart.getCartItems()) {

            totalPrice += cartItem.getPrice();
            totalDiscountedprice += cartItem.getDiscountedPrice();
            totalItem += cartItem.getQuantity();
        }

        cart.setTotalPrice(totalPrice);
        cart.setTotalItem(cart.getCartItems().size());
        cart.setTotalDiscountedPrice(totalDiscountedprice);
        cart.setDiscount(totalPrice - totalDiscountedprice);
        cart.setTotalItem(totalItem);
        return cartRepo.save(cart);

    }

    @Override
    public void clearCart(String userId) throws UserException {
        Cart cart = cartRepo.findByUserId(userId);
        if (cart == null) {
            throw new UserException("Cart not found for user ID: " + userId);
        }
        cartItemRepo.deleteAll(cart.getCartItems());
        cart.setCartItems(new ArrayList<>());
        cart.setTotalPrice(0);
        cart.setTotalDiscountedPrice(0);
        cart.setTotalItem(0);
        cart.setDiscount(0);
        cartRepo.save(cart);

    }
}
