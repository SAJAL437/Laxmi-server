package com.app.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.app.entity.Cart;

@Repository
public interface CartRepo extends JpaRepository<Cart, String> {

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    public Cart findByUserId(@Param("userId") String userId);
}
