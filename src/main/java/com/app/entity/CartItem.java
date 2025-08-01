package com.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class CartItem {

	@Id
	private String id;

	@JsonIgnore
	@ManyToOne
	private Cart cart;

	@ManyToOne
	private Product product;

	private String size;

	private int quantity;

	private int price;

	private int discountedPrice;

	private String userId; // Changed to String to match CartRepo query
}