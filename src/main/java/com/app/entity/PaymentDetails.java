package com.app.entity;

import com.app.DTOs.PaymentMethod;
import com.app.DTOs.PaymentStatus;

import lombok.Data;

@Data
public class PaymentDetails {
	
	private PaymentMethod paymentMethod;
	private PaymentStatus status;
	private String paymentId;
	private String razorpayPaymentLinkId;
	private String razorpayPaymentLinkReferenceId;
	private String razorpayPaymentLinkStatus;
	private String razorpayPaymentId​;
	private String razorpayOrderId;


}