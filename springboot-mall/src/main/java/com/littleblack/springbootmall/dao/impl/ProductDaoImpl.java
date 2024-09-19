package com.littleblack.springbootmall.dao.impl;

import com.littleblack.springbootmall.dao.ProductDao;
import com.littleblack.springbootmall.dto.ProductQueryParams;
import com.littleblack.springbootmall.dto.ProductRequest;
import com.littleblack.springbootmall.model.Product;
import com.littleblack.springbootmall.rowmapper.ProductRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductDaoImpl implements ProductDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer countProducts(ProductQueryParams productQueryParams) { // 獲取總數方法
        String sql = "SELECT COUNT(*) FROM product WHERE 1=1";

        Map<String, Object> map = new HashMap<>(); // 創建一個map

        // 查詢條件語句
        sql = addFilterSql(sql, map, productQueryParams);

        Integer total =  namedParameterJdbcTemplate.queryForObject(sql, map, Integer.class); // 獲取總數

        return total;
    }

    @Override
    public List<Product> getProducts(ProductQueryParams productQueryParams) { // 會去查詢Products的方法
        String sql = "SELECT product_id, product_name, category, image_url, price, stock, description, " +
                "created_date, last_modified_date " +
                "FROM product WHERE 1=1";

        Map<String,Object> map = new HashMap<>(); // 創建一個map

        // 查詢條件語句
        sql = addFilterSql(sql, map, productQueryParams);

        // 排序語句
        sql = sql + " ORDER BY " + productQueryParams.getOrderBy() + " " + productQueryParams.getSort(); // 排序

        // 分頁語句
        sql = sql + " LIMIT :limit OFFSET :offset"; // 分頁
        map.put("limit", productQueryParams.getLimit()); // 把limit的值放到map裡
        map.put("offset", productQueryParams.getOffset()); // 把offset的值放到map裡

        List<Product> productList = namedParameterJdbcTemplate.query(sql,map ,new ProductRowMapper()); // 會去查詢Products的方法

        return productList;
    }

    @Override
    public Product getProductById(Integer productId) { // 會去查詢ProductID的方法
        String sql = "SELECT product_id, product_name, category,image_url, price,stock, description, " +
                "created_date, last_modified_date " +
                "FROM product WHERE product_id = :productId";

        Map<String, Object> map = new HashMap<>(); // 創建一個map
        map.put("productId", productId);

        List<Product> productList = namedParameterJdbcTemplate.query(sql, map, new ProductRowMapper());

        if (productList.size() > 0) {
            return productList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Integer createProduct(ProductRequest productRequest) { // 會去新增Product的方法
        String sql = "INSERT INTO product (product_name, category, image_url, price, stock, " +
                "description, created_date, last_modified_date) " +
                "VALUES (:productName, :category, :imageUrl, :price, :stock, :description," +
                " :createdDate, :lastModifiedDate)";

        Map<String, Object> map = new HashMap<>();
        map.put("productName", productRequest.getProductName());
        map.put("category", productRequest.getCategory().toString());
        map.put("imageUrl", productRequest.getImageUrl());
        map.put("price", productRequest.getPrice());
        map.put("stock", productRequest.getStock());
        map.put("description", productRequest.getDescription());


        Date now = new Date();
        map.put("createdDate", now);
        map.put("lastModifiedDate", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        int productId = keyHolder.getKey().intValue();

        return productId;
    }

    @Override
    public void updateProduct(Integer productId, ProductRequest productRequest) { // 會去更新Product的方法
        String sql = "UPDATE product SET product_name = :productName, category = :category, " +
                "image_url = :imageUrl, price = :price, stock = :stock, description = :description, " +
                "last_modified_date = :lastModifiedDate " +
                "WHERE product_id = :productId";

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);

        map.put("productName", productRequest.getProductName());
        map.put("category", productRequest.getCategory().toString());
        map.put("imageUrl", productRequest.getImageUrl());
        map.put("price", productRequest.getPrice());
        map.put("stock", productRequest.getStock());
        map.put("description", productRequest.getDescription());

        map.put("lastModifiedDate", new Date());

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void updateStock(Integer produtId, Integer stock) {
        String sql = "UPDATE product SET stock = :stock, last_modified_date = :lastModifiedDate " +
                "WHERE product_id = :productId";

        Map<String, Object> map = new HashMap<>();
        map.put("productId", produtId);
        map.put("stock", stock);
        map.put("lastModifiedDate", new Date());

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void deleteProductById(Integer productId) { // 會去刪除Product的方法
        String sql = "DELETE FROM product WHERE product_id = :productId";

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);

        namedParameterJdbcTemplate.update(sql, map);
    }

    private String addFilterSql(String sql, Map<String, Object> map,ProductQueryParams productQueryParams){
        if (productQueryParams.getCategory() != null) { // 如果category不是null
            sql = sql + " AND category = :category"; // 就去查詢category
            map.put("category", productQueryParams.getCategory().name()); // 把category的值放到map裡
        }

        if (productQueryParams.getSearch() != null) { // 如果search不是null
            sql = sql + " AND product_name LIKE :search"; // 就去查詢product_name
            map.put("search", "%" + productQueryParams.getSearch() + "%"); // 把search的值放到map裡 並且加上% 代表模糊查詢 例如: %search% 代表查詢包含search的所有資料
        }
        return sql;
    }



}
