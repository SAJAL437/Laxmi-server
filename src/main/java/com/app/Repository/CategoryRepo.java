package com.app.Repository;

import com.app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends JpaRepository<Category, String> {

    Category findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND (:parentCategoryName IS NULL OR c.parentCategory.name = :parentCategoryName)")
    Category findByNameAndParent(@Param("name") String name, @Param("parentCategoryName") String parentCategoryName);
}