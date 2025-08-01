package com.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.entity.CartItem;
import com.app.entity.User;
import com.app.exception.CartItemException;
import com.app.exception.UserException;
import com.app.response.ApiResponse;
import com.app.service.CartItemService;
import com.app.service.UserServices;

@RestController
@RequestMapping("/api/users/cart_items")
public class CartItemController {

    private CartItemService cartItemServcie;
    private UserServices userService;

    public CartItemController(CartItemService cartItemServcie, UserServices userService) {
        this.cartItemServcie = cartItemServcie;
        this.userService = userService;
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItemHandler(
            @PathVariable String cartItemId,
            @RequestHeader("Authorization") String jwt) throws CartItemException, UserException {

        User user = userService.FindUserProfileByJwt(jwt);

        // 🧠 Pass cartItemId first, then userId (ensure order matches method signature)
        cartItemServcie.removeCartItem(user.getId(), cartItemId);

        ApiResponse res = new ApiResponse("Item removed from cart", true);

        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItemHandler(
            @PathVariable String cartItemId,
            @RequestBody CartItem cartItem,
            @RequestHeader("Authorization") String jwt) throws CartItemException, UserException {

        User user = userService.FindUserProfileByJwt(jwt);

        // ✅ Correct order: cartItemId first, then userId
        CartItem updatedCartItem = cartItemServcie.updateCartItem(cartItemId, user.getId(), cartItem);

        return new ResponseEntity<>(updatedCartItem, HttpStatus.ACCEPTED);
    }

}
