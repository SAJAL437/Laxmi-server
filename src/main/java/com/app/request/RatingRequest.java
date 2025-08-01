package com.app.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequest {
    private String productId;
    private double rating;
}
