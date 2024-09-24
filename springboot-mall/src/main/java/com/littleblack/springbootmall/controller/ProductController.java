package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.constant.ProductCategory;
import com.littleblack.springbootmall.dto.ProductQueryParams;
import com.littleblack.springbootmall.dto.ProductRequest;
import com.littleblack.springbootmall.model.Product;
import com.littleblack.springbootmall.service.ProductService;
import com.littleblack.springbootmall.util.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated // 驗證參數
@RestController // 返回json格式
public class ProductController {

    @Autowired // 自動注入
    private ProductService productService; // 創建一個ProductService的物件

    // 查詢所有商品
    @GetMapping("/products") // 設定路徑
    public ResponseEntity<Page<Product>> getProducts( // 返回查詢到的所有Product

                                             // 查詢條件 Filtering
                                             @RequestParam(required = false) ProductCategory category,// 會去接住category的值 並且可以為null 代表不是必填
                                             @RequestParam(required = false) String search,// 會去接住search的值 並且可以為null 代表不是必填

                                             // 排序 Sorting
                                             @RequestParam(defaultValue = "created_date") String orderBy, // 會去接住orderBy的值 並且可以為null 代表不是必填
                                             @RequestParam(defaultValue = "desc") String sort, // 會去接住orderBy和sort的值 並且可以為null 代表不是必填

                                             // 分頁 Pagination
                                             @RequestParam(defaultValue = "5") @Max(1000) @Min(0) Integer limit, // 會去接住limit的值 並且可以為null 代表不是必填
                                             @RequestParam(defaultValue = "0") @Min(0) Integer offset  // 會去接住offset的值 並且可以為null 代表不是必填


    ){
        ProductQueryParams productQueryParams = new ProductQueryParams(); // 創建一個ProductQueryParams的物件
        productQueryParams.setCategory(category); // 把category的值放到ProductQueryParams裡
        productQueryParams.setSearch(search); // 把search的值放到ProductQueryParams裡
        productQueryParams.setOrderBy(orderBy); // 把orderBy的值放到ProductQueryParams裡
        productQueryParams.setSort(sort); // 把sort的值放到ProductQueryParams裡
        productQueryParams.setLimit(limit); // 把limit的值放到ProductQueryParams裡
        productQueryParams.setOffset(offset); // 把sort的值放到ProductQueryParams裡

        // 取得product List
        List<Product> productList = productService.getProducts(productQueryParams);

        // 取得product總數
        Integer total = productService.countProducts(productQueryParams); // 獲取總數

        // 分頁
        Page<Product> page = new Page<>();
        page.setLimit(limit);
        page.setOffset(offset);
        page.setTotal(total);
        page.setResults(productList);

        return ResponseEntity.status(HttpStatus.OK).body(page); // 返回查詢到的所有Product
    }

    // 查詢某一項商品
    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer productId) { // 會去接住ProductID的值
        Product product = productService.getProductById(productId); // 會去查詢ProductID的方法

        if (product != null) {                                          // 如果查詢到的Product不是null
            return ResponseEntity.status(HttpStatus.OK).body(product);  // 返回查詢到的Product
        } else {                                                        // 如果查詢到的Product是null
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 返回查詢不到的狀態碼
        }
    }


    // 新增商品
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody @Valid ProductRequest productRequest) { // 會去接住前端傳過來的json參數
        Integer productId =  productService.createProduct(productRequest); // 會去執行新增Product的方法

        Product product = productService.getProductById(productId); // 會去查詢ProductID的方法

        return ResponseEntity.status(HttpStatus.CREATED).body(product); // 返回新增的Product
    }

    // 修改商品
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

    // 刪除商品
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer productId) { // 會去接住ProductID的值
        productService.deleteProductById(productId);                          // 會去執行刪除Product的方法

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();          // 返回刪除的狀態碼
    }
}


