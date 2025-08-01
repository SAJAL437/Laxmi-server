package com.app.service;

import java.util.List;

import com.app.entity.Rating;
import com.app.entity.User;
import com.app.exception.ProductException;
import com.app.request.RatingRequest;

public interface RatingService {
    public Rating createRating(RatingRequest req, User user) throws ProductException;

    public List<Rating> getProductsRating(String productId);
}
