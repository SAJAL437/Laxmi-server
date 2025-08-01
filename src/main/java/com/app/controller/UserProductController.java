package com.app.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.entity.Product;
import com.app.exception.ProductException;
import com.app.service.ProductService;

@RestController
@RequestMapping("/api/users/products")
public class UserProductController {

    private ProductService productService;

    public UserProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public ResponseEntity<Page<Product>> findProductByCategoryHandler(
            String category,
            @RequestParam(required = false) List<String> color,
            @RequestParam(required = false) List<String> size,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minDiscount,
            @RequestParam(required = false, defaultValue = "price_low") String sort,
            @RequestParam(required = false, defaultValue = "all") String stock,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        if (color == null)
            color = new ArrayList<>();
        if (size == null)
            size = new ArrayList<>();

        // Apply defaults if null
        if (minPrice == null)
            minPrice = 0;
        if (maxPrice == null)
            maxPrice = Integer.MAX_VALUE;
        if (minDiscount == null)
            minDiscount = 0;

        Page<Product> res = productService.getAllProduct(category, color, size, minPrice, maxPrice, minDiscount, sort,
                stock, pageNumber, pageSize);

        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
    }

    @GetMapping("/id/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable String productId) throws ProductException {
        Product product = productService.findProductById(productId);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String search) {
        List<Product> products = productService.searchProduct(search);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

}