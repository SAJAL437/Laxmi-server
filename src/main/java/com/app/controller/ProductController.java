package com.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.entity.Product;
import com.app.exception.ProductException;
import com.app.request.CreateProductRequest;
import com.app.response.ApiResponse;
import com.app.service.ProductService;

@RestController
@RequestMapping("/api/admin/products")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/")
    public ResponseEntity<Product> createProductHandler(@RequestBody CreateProductRequest req) throws ProductException {

        Product createdProduct = productService.createProduct(req);

        return new ResponseEntity<Product>(createdProduct, HttpStatus.ACCEPTED);

    }

    @PostMapping("/creates")
    public ResponseEntity<ApiResponse> createMultipleProduct(@RequestBody CreateProductRequest[] reqs)
            throws ProductException {
        for (CreateProductRequest product : reqs) {
            productService.createProduct(product);
        }
        ApiResponse res = new ApiResponse("Products created successfully", true);
        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable String productId) throws ProductException {
        String message = productService.deleteProduct(productId);
        ApiResponse res = new ApiResponse(message, true);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProductHandler(@RequestBody Product req, @PathVariable String productId)
            throws ProductException {

        Product updateProduct = productService.updateProduct(productId, req);

        return new ResponseEntity<Product>(updateProduct, HttpStatus.OK);

    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> findAllProduct() {

        List<Product> products = productService.getAllProducts();

        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);

    }

    @GetMapping("/recent")
    public ResponseEntity<List<Product>> recentlyAddedProduct() {

        List<Product> products = productService.recentlyAddedProduct();

        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
    }

}
