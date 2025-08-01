package com.app.Repository;

import com.app.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepo extends JpaRepository<Rating, String> {

    @Query("SELECT r FROM Rating r WHERE r.product.id = :productId")
    List<Rating> getAllProductsRating(@Param("productId") String productId);
}
