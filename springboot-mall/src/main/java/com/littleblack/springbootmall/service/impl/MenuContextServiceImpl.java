package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.constant.ProductCategory;
import com.littleblack.springbootmall.dto.ProductQueryParams;
import com.littleblack.springbootmall.model.Product;
import com.littleblack.springbootmall.service.MenuContextService;
import com.littleblack.springbootmall.service.ProductService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MenuContextServiceImpl implements MenuContextService {
    private final ProductService productService;
    private Map<String, List<Product>> menuCache;
    private LocalDateTime lastUpdateTime;
    private static final long CACHE_DURATION_MINUTES = 1;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public MenuContextServiceImpl(ProductService productService) {
        this.productService = productService;
        this.menuCache = new HashMap<>();
        updateMenu();
    }

    private void updateMenuIfNeeded() {
        lock.readLock().lock();
        try {
            if (lastUpdateTime == null ||
                    LocalDateTime.now().isAfter(lastUpdateTime.plusMinutes(CACHE_DURATION_MINUTES))) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    // 雙重檢查，確保在等待寫鎖的過程中沒有其他線程已經更新了緩存
                    if (lastUpdateTime == null ||
                            LocalDateTime.now().isAfter(lastUpdateTime.plusMinutes(CACHE_DURATION_MINUTES))) {
                        updateMenu();
                    }
                } finally {
                    lock.writeLock().unlock();
                    lock.readLock().lock();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private void updateMenu() {
        log.info("開始更新菜單緩存");
        Map<String, List<Product>> newCache = new HashMap<>();
        try {
            ProductQueryParams queryParams = new ProductQueryParams();
            queryParams.setLimit(100);
            queryParams.setOffset(0);
            queryParams.setOrderBy("product_id");
            queryParams.setSort("asc");

            List<Product> allProducts = productService.getProducts(queryParams);
            log.info("成功獲取產品數據，共 {} 個產品", allProducts.size());

            for (Product product : allProducts) {
                String category = product.getCategory() != null ? product.getCategory().name() : "UNKNOWN";
                newCache.computeIfAbsent(category, k -> new ArrayList<>()).add(product);
            }

            lock.writeLock().lock();
            try {
                menuCache = newCache;
                lastUpdateTime = LocalDateTime.now();
                log.info("菜單緩存更新完成，更新時間：{}", lastUpdateTime);
            } finally {
                lock.writeLock().unlock();
            }

        } catch (Exception e) {
            log.error("更新菜單緩存時發生錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("更新菜單緩存失敗", e);
        }
    }

    @Override
    public synchronized void refreshCache() {
        log.info("收到手動刷新緩存請求");
        try {
            lock.writeLock().lock();
            updateMenu();
            log.info("手動刷新緩存完成");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String getMenuContext() {
        updateMenuIfNeeded();

        if (menuCache.isEmpty()) {
            log.error("無法讀取菜單資訊");
            return "無法提供菜單資訊。";
        }

        StringBuilder menuInfo = new StringBuilder();
        menuInfo.append("【ZERO Supper 菜單資訊】\n\n");

        // 處理主餐類別
        List<Product> foods = menuCache.get("FOOD");
        if (foods != null && !foods.isEmpty()) {
            menuInfo.append("【究極主餐】\n");
            for (Product food : foods) {
                menuInfo.append(String.format("- %s（$%d）\n",
                        food.getProductName(),
                        food.getPrice()));
                if (food.getDescription() != null && !food.getDescription().isEmpty()) {
                    menuInfo.append("  ").append(food.getDescription()).append("\n");
                }
                menuInfo.append("\n");
            }
        }

        // 處理漢堡類別
        List<Product> burgers = menuCache.get("BURGER");
        if (burgers != null && !burgers.isEmpty()) {
            menuInfo.append("【特色漢堡】\n");
            for (Product burger : burgers) {
                menuInfo.append(String.format("- %s（$%d）\n",
                        burger.getProductName(),
                        burger.getPrice()));
                if (burger.getDescription() != null && !burger.getDescription().isEmpty()) {
                    menuInfo.append("  ").append(burger.getDescription()).append("\n");
                }
                menuInfo.append("\n");
            }
        }

        menuInfo.append("\n【注意事項】\n");
        menuInfo.append("1. 以上為本餐廳所有提供的餐點\n");
        menuInfo.append("2. 價格均已含稅\n");
        menuInfo.append("3. 所有餐點均為限量供應\n");

        return menuInfo.toString();
    }

    @Override
    public boolean validateProductExistence(String productName) {
        updateMenuIfNeeded();
        return menuCache.values().stream()
                .flatMap(List::stream)
                .anyMatch(product -> product.getProductName().equals(productName));
    }

    @Override
    public Product getProduct(String productName) {
        updateMenuIfNeeded();
        return menuCache.values().stream()
                .flatMap(List::stream)
                .filter(product -> product.getProductName().equals(productName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Product> getAllProducts() {
        updateMenuIfNeeded();
        List<Product> allProducts = menuCache.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        log.info("getAllProducts: 返回總共 {} 個產品", allProducts.size());
        return allProducts;
    }
}