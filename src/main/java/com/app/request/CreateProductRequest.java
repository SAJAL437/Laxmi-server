package com.app.request;

import java.util.HashSet;
import java.util.Set;

import com.app.entity.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {

    private String title;

    private String description;

    private Integer price;

    private Integer discountedPrice;

    private Integer discountPersent;

    private Integer quantity;

    private String brand;

    private String color;

    private Set<Size> size = new HashSet<>();

    private String imageUrl;

    private String topLavelCategory;
    private String secondLavelCategory;
    private String thirdLavelCategory;

}
