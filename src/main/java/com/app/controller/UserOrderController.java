package com.app.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.entity.Address;
import com.app.entity.Order;
import com.app.entity.User;
import com.app.exception.OrderException;
import com.app.exception.UserException;
import com.app.service.OrderService;
import com.app.service.UserServices;

@RestController
@RequestMapping("/api/users/orders")
public class UserOrderController {

    private OrderService orderService;
    private UserServices userService;

    public UserOrderController(OrderService orderService, UserServices userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/")
    public ResponseEntity<Order> createOrderHandler(@RequestBody Address sippingAddress,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.FindUserProfileByJwt(jwt);
        Order order = orderService.createOrder(user, sippingAddress);

        return new ResponseEntity<Order>(order, HttpStatus.OK);

    }

    @GetMapping("/history")
    public ResponseEntity<?> userOrderHistory(@RequestHeader("Authorization") String jwt) throws OrderException {
        try {
            // Validate and extract user from JWT
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid or missing JWT token", "Provide a valid Bearer token"));
            }

            User user = userService.FindUserProfileByJwt(jwt.replace("Bearer ", ""));
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User not found", "Invalid JWT token or user does not exist"));
            }

            // Fetch order history
            List<Order> orders = orderService.usersOrderHistory(user.getId());
            if (orders == null || orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList());
            }

            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> findOrderHandler(@PathVariable String orderId,
            @RequestHeader("Authorization") String jwt) throws OrderException, UserException {

        User user = userService.FindUserProfileByJwt(jwt);
        Order order = orderService.findOrderById(orderId);

        return new ResponseEntity<>(order, HttpStatus.ACCEPTED);

    }

    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Order> confirmOrderHandler(@PathVariable String orderId,
            @RequestHeader("Authorization") String jwt) throws OrderException, UserException {
        User user = userService.FindUserProfileByJwt(jwt);
        Order order = orderService.confirmedOrder(orderId);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(
            @PathVariable String orderId,
            @RequestHeader("Authorization") String jwt) throws OrderException, UserException {
        User user = userService.FindUserProfileByJwt(jwt);
        orderService.deleteOrder(orderId);
        return new ResponseEntity<>("Order deleted successfully", HttpStatus.OK);
    }

    public static class ErrorResponse {
        private String error;
        private String details;
        private String timestamp;

        public ErrorResponse(String error, String details) {
            this.error = error;
            this.details = details;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }

        // Getters and setters
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
