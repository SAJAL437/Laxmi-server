package com.app.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.app.entity.Cart;
import com.app.entity.CartItem;
import com.app.entity.Product;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, String> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product AND ci.size = :size AND ci.userId = :userId")
    public CartItem isCartItemExist(
            @Param("cart") Cart cart,
            @Param("product") Product product,
            @Param("size") String size,
            @Param("userId") String userId);
}
