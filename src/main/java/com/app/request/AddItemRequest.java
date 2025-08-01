package com.app.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemRequest {
    private String productId;
    private String size;
    private Integer quantity;
    private int price;
}
