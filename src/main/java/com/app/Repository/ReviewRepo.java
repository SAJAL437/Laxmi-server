package com.app.Repository;

import com.app.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepo extends JpaRepository<Review, String> {

	@Query("SELECT r FROM Review r WHERE r.product.id = :productId")
	List<Review> getAllProductsReview(@Param("productId") String productId);
}
