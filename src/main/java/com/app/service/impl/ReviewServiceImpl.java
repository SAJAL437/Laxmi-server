package com.app.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.app.Repository.ProductRepo;
import com.app.Repository.ReviewRepo;
import com.app.entity.Product;
import com.app.entity.Review;
import com.app.entity.User;
import com.app.exception.ProductException;
import com.app.request.ReviewRequest;
import com.app.service.ProductService;
import com.app.service.ReviewService;

public class ReviewServiceImpl implements ReviewService {

    private ReviewRepo reviewRepo;
	private ProductService productService;
	private ProductRepo productRepo;
	
	public ReviewServiceImpl(ReviewRepo reviewRepo,ProductService productService,ProductRepo productRepo) {
		this.reviewRepo=reviewRepo;
		this.productService=productService;
		this.productRepo=productRepo;
	}

	@Override
	public Review createReview(ReviewRequest req,User user) throws ProductException {
		Product product=productService.findProductById(req.getProductId());
		Review review=new Review();
		review.setUser(user);
		review.setProduct(product);
		review.setReview(req.getReview());
		review.setCreatedAt(LocalDateTime.now());
		
//		product.getReviews().add(review);
		productRepo.save(product);
		return reviewRepo.save(review);
	}

	@Override
	public List<Review> getAllReview(String productId) {
		
		return reviewRepo.getAllProductsReview(productId);
	}


}
