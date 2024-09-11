package com.littleblack.springbootmall.dao;

import com.littleblack.springbootmall.dto.ProductRequest;
import com.littleblack.springbootmall.model.Product;

public interface ProductDao {

    Product getProductById(Integer productId); // 會去查詢ProductID的方法

    Integer createProduct(ProductRequest productRequest); // 會去新增Product的方法


}
