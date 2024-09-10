package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.model.Product;

public interface ProductService {

    Product getProductById(Integer productId); // 會去查詢ProductID的方法

}
