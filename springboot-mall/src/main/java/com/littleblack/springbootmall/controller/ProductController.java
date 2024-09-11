package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.ProductRequest;
import com.littleblack.springbootmall.model.Product;
import com.littleblack.springbootmall.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer productId) { // 會去接住ProductID的值
        Product product = productService.getProductById(productId); // 會去查詢ProductID的方法

        if (product != null) {                                          // 如果查詢到的Product不是null
            return ResponseEntity.status(HttpStatus.OK).body(product);  // 返回查詢到的Product
        } else {                                                        // 如果查詢到的Product是null
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 返回查詢不到的狀態碼
        }
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody @Valid ProductRequest productRequest) { // 會去接住前端傳過來的json參數
        Integer productId =  productService.createProduct(productRequest); // 會去執行新增Product的方法

        Product product = productService.getProductById(productId); // 會去查詢ProductID的方法

        return ResponseEntity.status(HttpStatus.CREATED).body(product); // 返回新增的Product
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer productId, // 會去接住ProductID的值
                                                 @RequestBody @Valid ProductRequest productRequest) { // 會去接住前端傳過來的json參數

        // 會去查詢ProductID的方法
        Product product = productService.getProductById(productId);

        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 如果查詢到的Product是null，返回查詢不到的狀態碼
        }

        // 修改商品數據
        productService.updateProduct(productId, productRequest); // 會去執行更新Product的方法

        Product updatedProduct = productService.getProductById(productId); // 會去查詢ProductID的方法

        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct); // 返回更新的Product
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer productId) { // 會去接住ProductID的值
        productService.deleteProductById(productId);                          // 會去執行刪除Product的方法

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();          // 返回刪除的狀態碼
    }
}


