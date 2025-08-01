package com.app.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.app.entity.Product;
import com.app.exception.ProductException;
import com.app.request.CreateProductRequest;

public interface ProductService {

    // only for admin and seller
    public Product createProduct(CreateProductRequest req) throws ProductException;

    public String deleteProduct(String productId) throws ProductException;

    public Product updateProduct(String id, Product req) throws ProductException;

    public List<Product> getAllProducts();

    // for user ,admin 
    public Product findProductById(String id) throws ProductException;

    public List<Product> findProductByCategory(String category);

    public List<Product> searchProduct(String query);

    public Page<Product> getAllProduct(String category, List<String> colors, List<String> sizes, Integer minPrice,
            Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber, Integer pageSize);

    public List<Product> recentlyAddedProduct();
}
