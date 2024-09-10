package com.littleblack.springbootmall.dao;

import com.littleblack.springbootmall.model.Product;

public interface ProductDao {

    Product getProductById(Integer productId); // 會去查詢ProductID的方法
}
