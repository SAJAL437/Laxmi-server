package com.app.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.app.DTOs.OrderStatus;
import com.app.DTOs.PaymentMethod;
import com.app.DTOs.PaymentStatus;
import com.app.Repository.OrderRepo;
import com.app.entity.Order;
import com.app.entity.User;
import com.app.exception.OrderException;
import com.app.exception.UserException;
import com.app.response.ApiResponse;
import com.app.response.PaymentLinkResponse;
import com.app.service.CartService;
import com.app.service.OrderService;
import com.app.service.UserServices;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@RestController
@RequestMapping("/api/users/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    @Value("${razorpay.api.key}")
    String apiKey;

    @Value("${razorpay.api.secret}")
    String apiSecretKey;

    private final OrderService orderService;
    private final UserServices userService;
    private final OrderRepo orderRepo;
    private final CartService cartService;

    public PaymentController(OrderService orderService, UserServices userService,
            OrderRepo orderRepo, CartService cartService) {
        this.orderService = orderService;
        this.userService = userService;
        this.orderRepo = orderRepo;
        this.cartService = cartService;
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<PaymentLinkResponse> createPaymentLink(
            @PathVariable String orderId,
            @RequestHeader("Authorization") String jwt)
            throws RazorpayException, UserException, OrderException {

        System.out.println("Creating payment link for orderId: " + orderId + ", JWT: "
                + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

        User user = userService.FindUserProfileByJwt(jwt);
        Order order = orderService.findOrderById(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            System.err.println("Order does not belong to user: orderId=" + orderId + ", userId=" + user.getId());
            throw new OrderException("Order does not belong to the authenticated user");
        }

        try {
            RazorpayClient client = new RazorpayClient(apiKey, apiSecretKey);

            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount", order.getTotalDiscountedPrice() * 100);
            paymentLinkRequest.put("currency", "INR");

            JSONObject customer = new JSONObject();
            customer.put("name", order.getUser().getName());
            customer.put("contact", order.getUser().getPhone());
            customer.put("email", order.getUser().getEmail());
            paymentLinkRequest.put("customer", customer);

            JSONObject notify = new JSONObject();
            notify.put("sms", true);
            notify.put("email", true);
            paymentLinkRequest.put("notify", notify);

            paymentLinkRequest.put("reminder_enable", true);
            paymentLinkRequest.put("callback_url", "http://localhost:5173/payment/" + orderId);
            paymentLinkRequest.put("callback_method", "get");

            PaymentLink payment = client.paymentLink.create(paymentLinkRequest);

            String paymentLinkId = payment.get("id");
            String paymentLinkUrl = payment.get("short_url");

            PaymentLinkResponse res = new PaymentLinkResponse();
            res.setPayment_link_id(paymentLinkId);
            res.setPayment_link_url(paymentLinkUrl);

            order.getPaymentDetails().setRazorpayPaymentLinkId(paymentLinkId);
            order.getPaymentDetails().setRazorpayOrderId(payment.get("order_id"));
            orderRepo.save(order);

            System.out.println("Payment link created - ID: " + paymentLinkId + ", URL: " + paymentLinkUrl
                    + ", Razorpay Order ID: " + payment.get("order_id"));

            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (RazorpayException e) {
            System.err.println("Error creating payment link: " + e.getMessage());
            throw new RazorpayException("Failed to create payment link: " + e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse> redirect(
            @RequestParam(name = "razorpay_payment_id") String paymentId,
            @RequestParam(name = "order_id") String orderId,
            @RequestHeader(name = "Authorization", required = false) String jwt)
            throws OrderException, RazorpayException {

        System.out.println("Redirect called with paymentId: " + paymentId + ", orderId: " + orderId + ", JWT: "
                + (jwt != null ? jwt.substring(0, Math.min(jwt.length(), 20)) + "..." : "No JWT"));

        try {
            Order order = orderService.findOrderById(orderId);
            System.out.println("Order found: " + order.getId() + ", PaymentLinkId: "
                    + order.getPaymentDetails().getRazorpayPaymentLinkId());

            RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecretKey);
            Payment payment = razorpay.payments.fetch(paymentId);
            System.out.println("Payment details: " + payment.toString() + ", Status: " + payment.get("status"));

            if (payment.get("status").equals("captured")) {
                order.getPaymentDetails().setPaymentId(paymentId);
                order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
                order.getPaymentDetails().setPaymentMethod(PaymentMethod.RAZORPAY);
                order.setOrderStatus(OrderStatus.PLACED);
                orderRepo.save(order);
                System.out.println("Order updated: status=PLACED, paymentId=" + paymentId);

                // Temporarily comment out cart clearing to isolate issue
                /*
                 * try {
                 * cartService.clearCart(order.getUser().getId());
                 * System.out.println("Cart cleared for user ID: " + order.getUser().getId());
                 * } catch (UserException e) {
                 * System.err.println("Failed to clear cart for user ID: " +
                 * order.getUser().getId() + ", error: " + e.getMessage());
                 * }
                 */

                ApiResponse res = new ApiResponse();
                res.setStatus(true);
                res.setMessage("Your order has been placed successfully");
                System.out.println("Returning success response: " + res);
                return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
            } else {
                System.err.println("Payment not captured: status=" + payment.get("status"));
                ApiResponse res = new ApiResponse();
                res.setStatus(false);
                res.setMessage("Payment not captured: " + payment.get("status"));
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
        } catch (OrderException e) {
            System.err.println("OrderException in redirect: " + e.getMessage());
            ApiResponse res = new ApiResponse();
            res.setStatus(false);
            res.setMessage("Order not found: " + e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        } catch (RazorpayException e) {
            System.err.println("RazorpayException in redirect: " + e.getMessage());
            ApiResponse res = new ApiResponse();
            res.setStatus(false);
            res.setMessage("Failed to process payment: " + e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Unexpected error in redirect: " + e.getMessage());
            ApiResponse res = new ApiResponse();
            res.setStatus(false);
            res.setMessage("Unexpected error: " + e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}