package com.app.controller;

import com.app.entity.Cart;
import com.app.entity.CartItem;
import com.app.entity.User;
import com.app.exception.ProductException;
import com.app.exception.UserException;
import com.app.request.AddItemRequest;
import com.app.response.ApiResponse;
import com.app.service.CartService;
import com.app.service.UserServices;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/carts")
public class CartController {

    private CartService cartService;
    private UserServices userService;

    public CartController(CartService cartService, UserServices userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<Cart> findUserCartHandler(@RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.FindUserProfileByJwt(jwt);

        Cart cart = cartService.findUserCart(user.getId());

        return new ResponseEntity<Cart>(cart, HttpStatus.OK);

    }

    @PutMapping("/add")
    public ResponseEntity<CartItem> addItemToCart(@RequestBody AddItemRequest req,
            @RequestHeader("Authorization") String jwt) throws UserException, ProductException {

        User user = userService.FindUserProfileByJwt(jwt);

        CartItem item = cartService.addCartItem(user.getId(), req);

        ApiResponse res = new ApiResponse(true, "Item Added To Cart Successfully");

        return new ResponseEntity<>(item, HttpStatus.OK);

    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> clearCartHandler(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.FindUserProfileByJwt(jwt);
        cartService.clearCart(user.getId());
        return new ResponseEntity<>("Cart cleared successfully", HttpStatus.OK);
    }
}
