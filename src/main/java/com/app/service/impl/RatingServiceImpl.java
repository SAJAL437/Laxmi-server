package com.app.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.Repository.RatingRepo;
import com.app.entity.Product;
import com.app.entity.Rating;
import com.app.entity.User;
import com.app.exception.ProductException;
import com.app.request.RatingRequest;
import com.app.service.ProductService;
import com.app.service.RatingService;

@Service
public class RatingServiceImpl implements RatingService {

    private RatingRepo ratingRepo;
    private ProductService productService;

    public RatingServiceImpl(RatingRepo ratingRepo, ProductService productService) {
        this.ratingRepo = ratingRepo;
        this.productService = productService;
    }

    @Override
    public Rating createRating(RatingRequest req, User user) throws ProductException {

        Product product = productService.findProductById(req.getProductId());

        Rating rating = new Rating();
        rating.setProduct(product);
        rating.setUser(user);
        rating.setRating(req.getRating());
        rating.setCreatedAt(LocalDateTime.now());

        return ratingRepo.save(rating);
    }

    @Override
    public List<Rating> getProductsRating(String productId) {
        // TODO Auto-generated method stub
        return ratingRepo.getAllProductsRating(productId);
    }

}
