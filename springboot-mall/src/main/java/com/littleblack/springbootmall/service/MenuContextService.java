package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.model.Product;
import java.util.List;

public interface MenuContextService {
    String getMenuContext();
    boolean validateProductExistence(String productName);
    Product getProduct(String productName);
    List<Product> getAllProducts();
    // 新增手動刷新方法
    void refreshCache();
}