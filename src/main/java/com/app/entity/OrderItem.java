package com.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class OrderItem {

	@Id
	private String id;

	@JsonIgnore
	@ManyToOne
	private Order order;

	@ManyToOne
	private Product product;

	private String size;

	private int quantity;

	private int price;

	private int discountedPrice;

	private String userId;

	private LocalDateTime deliveryDate;
}