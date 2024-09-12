package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.dto.ProductQueryParams;
import com.littleblack.springbootmall.dto.ProductRequest;
import com.littleblack.springbootmall.model.Product;

import java.util.List;

public interface ProductService {

    List<Product> getProducts(ProductQueryParams productQueryParams); // 會去查詢Products的方法

    Product getProductById(Integer productId); // 會去查詢ProductID的方法

    Integer createProduct(ProductRequest productRequest); // 會去新增Product的方法

    void updateProduct(Integer productId, ProductRequest productRequest); // 會去更新Product的方法

    void deleteProductById(Integer productId); // 會去刪除Product的方法

}
