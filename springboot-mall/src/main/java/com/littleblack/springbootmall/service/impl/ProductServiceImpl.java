package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.constant.ProductCategory;
import com.littleblack.springbootmall.dao.ProductDao;
import com.littleblack.springbootmall.dto.ProductRequest;
import com.littleblack.springbootmall.model.Product;
import com.littleblack.springbootmall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    public List<Product> getProducts(ProductCategory category, String search) {
        return productDao.getProducts(category, search); // 會去查詢Products的方法
    }

    @Override
    public Product getProductById(Integer productId) {
        return productDao.getProductById(productId); // 會去查詢ProductID的方法
    }

    @Override
    public Integer createProduct(ProductRequest productRequest) {
        return productDao.createProduct(productRequest); // 會去新增Product的方法
    }

    @Override
    public void updateProduct(Integer productId, ProductRequest productRequest) {
        productDao.updateProduct(productId, productRequest); // 會去更新Product的方法
    }

    @Override
    public void deleteProductById(Integer productId) {
        productDao.deleteProductById(productId); // 會去刪除Product的方法
    }
}
