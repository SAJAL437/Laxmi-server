package com.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.app.entity.Review;
import com.app.entity.User;
import com.app.exception.ProductException;
import com.app.exception.UserException;
import com.app.request.ReviewRequest;
import com.app.service.ReviewService;
import com.app.service.UserServices;

public class ReviewController {

	private ReviewService reviewService;
	private UserServices userService;

	public ReviewController(ReviewService reviewService, UserServices userService) {
		this.reviewService = reviewService;
		this.userService = userService;
	}

	@PostMapping("/create")
	public ResponseEntity<Review> createReviewHandler(@RequestBody ReviewRequest req,
			@RequestHeader("Authorization") String jwt) throws UserException, ProductException {
		User user = userService.FindUserProfileByJwt(jwt);
		System.out.println("product id " + req.getProductId() + " - " + req.getReview());
		Review review = reviewService.createReview(req, user);
		System.out.println("product review " + req.getReview());
		return new ResponseEntity<Review>(review, HttpStatus.ACCEPTED);
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<List<Review>> getProductsReviewHandler(@PathVariable String productId) {
		List<Review> reviews = reviewService.getAllReview(productId);
		return new ResponseEntity<List<Review>>(reviews, HttpStatus.OK);
	}

}
