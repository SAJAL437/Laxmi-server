package com.app.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.app.Repository.CategoryRepo;
import com.app.Repository.ProductRepo;
import com.app.entity.Category;
import com.app.entity.Product;
import com.app.exception.ProductException;
import com.app.request.CreateProductRequest;
import com.app.service.ProductService;
import com.app.service.UserServices;

@Service
public class ProductImpl implements ProductService {

    private ProductRepo productRepo;
    private UserServices userService;
    private CategoryRepo categoryRepo;

    public ProductImpl(ProductRepo productRepo, @Lazy UserServices userService, CategoryRepo categoryRepo) {
        this.productRepo = productRepo;
        this.userService = userService;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public Product createProduct(CreateProductRequest req) throws ProductException {
        Category topLevel = categoryRepo.findByName(req.getTopLavelCategory());

        if (topLevel == null) {
            Category topLevelCategory = new Category();
            topLevelCategory.setId(UUID.randomUUID().toString());
            topLevelCategory.setName(req.getTopLavelCategory());
            topLevelCategory.setLevel(1);
            topLevel = categoryRepo.save(topLevelCategory);
        }

        Category secondLevel = categoryRepo.findByNameAndParent(req.getSecondLavelCategory(), topLevel.getName());
        if (secondLevel == null) {
            Category secondLevelCategory = new Category();
            secondLevelCategory.setId(UUID.randomUUID().toString());
            secondLevelCategory.setName(req.getSecondLavelCategory());
            secondLevelCategory.setParentCategory(topLevel);
            secondLevelCategory.setLevel(2);
            secondLevel = categoryRepo.save(secondLevelCategory);
        }

        Category thirdLevel = categoryRepo.findByNameAndParent(req.getThirdLavelCategory(), secondLevel.getName());
        if (thirdLevel == null) {
            Category thirdLevelCategory = new Category();
            thirdLevelCategory.setId(UUID.randomUUID().toString());
            thirdLevelCategory.setName(req.getThirdLavelCategory());
            thirdLevelCategory.setParentCategory(secondLevel);
            thirdLevelCategory.setLevel(3);
            thirdLevel = categoryRepo.save(thirdLevelCategory);
        }

        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());
        product.setDescription(req.getDescription());
        product.setDiscountedPrice(req.getDiscountedPrice());
        product.setDiscountPercent(req.getDiscountPersent());
        product.setImageUrl(req.getImageUrl());
        product.setBrand(req.getBrand());
        product.setPrice(req.getPrice());
        product.setSizes(req.getSize());
        product.setQuantity(req.getQuantity());
        product.setCategory(thirdLevel);
        product.setCreatedAt(LocalDateTime.now());

        return productRepo.save(product);
    }

    @Override
    public String deleteProduct(String productId) throws ProductException {
        Product product = findProductById(productId);
        product.getSizes().clear();
        productRepo.delete(product);
        return "Product deleted successfully";
    }

    @Override
    public Product updateProduct(String productId, Product req) throws ProductException {
        Product product = findProductById(productId);

        if (req.getQuantity() > 0) {
            product.setQuantity(req.getQuantity());
        }
        if (req.getDescription() != null && !req.getDescription().isEmpty()) {
            product.setDescription(req.getDescription());
        }
        if (req.getTitle() != null && !req.getTitle().isEmpty()) {
            product.setTitle(req.getTitle());
        }
        if (req.getColor() != null && !req.getColor().isEmpty()) {
            product.setColor(req.getColor());
        }
        if (req.getBrand() != null && !req.getBrand().isEmpty()) {
            product.setBrand(req.getBrand());
        }
        if (req.getImageUrl() != null && !req.getImageUrl().isEmpty()) {
            product.setImageUrl(req.getImageUrl());
        }
        if (req.getPrice() > 0) {
            product.setPrice(req.getPrice());
        }
        if (req.getDiscountedPrice() >= 0) {
            product.setDiscountedPrice(req.getDiscountedPrice());
        }
        if (req.getDiscountPercent() >= 0) {
            product.setDiscountPercent(req.getDiscountPercent());
        }
        if (req.getSizes() != null && !req.getSizes().isEmpty()) {
            product.setSizes(req.getSizes());
        }
        if (req.getCategory() != null && req.getCategory().getId() != null) {
            product.setCategory(req.getCategory());
        }

        return productRepo.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public List<Product> findProductByCategory(String category) {
        return productRepo.findByCategory(category);
    }

    @Override
    public List<Product> searchProduct(String query) {
        return productRepo.searchProduct(query);
    }

    @Override
    public Page<Product> getAllProduct(String category, List<String> colors,
            List<String> sizes, Integer minPrice, Integer maxPrice,
            Integer minDiscount, String sort, String stock, Integer pageNumber, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Product> products = productRepo.filterProducts(category, minPrice, maxPrice, minDiscount, sort);

        if (colors != null && !colors.isEmpty()) {
            products = products.stream()
                    .filter(p -> colors.stream().anyMatch(c -> c.equalsIgnoreCase(p.getColor())))
                    .collect(Collectors.toList());
        }

        if (stock != null) {
            if (stock.equals("in_stock")) {
                products = products.stream().filter(p -> p.getQuantity() > 0).collect(Collectors.toList());
            } else if (stock.equals("out_of_stock")) {
                products = products.stream().filter(p -> p.getQuantity() < 1).collect(Collectors.toList());
            }
        }

        // Sorting
        Comparator<Product> comparator = switch (sort) {
            case "price_low" -> Comparator.comparing(Product::getPrice);
            case "price_high" -> Comparator.comparing(Product::getPrice).reversed();
            default -> Comparator.comparing(Product::getPrice);
        };
        products.sort(comparator);

        // Pagination Handling
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), products.size());

        if (startIndex >= products.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, products.size());
        }

        List<Product> pageContent = products.subList(startIndex, endIndex);
        return new PageImpl<>(pageContent, pageable, products.size());
    }

    @Override
    public List<Product> recentlyAddedProduct() {
        return productRepo.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public Product findProductById(String id) throws ProductException {
        Optional<Product> opt = productRepo.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new ProductException("Product not found with id " + id);
    }
}