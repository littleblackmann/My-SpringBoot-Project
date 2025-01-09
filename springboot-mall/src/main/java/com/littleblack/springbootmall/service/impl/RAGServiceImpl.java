package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.event.CacheRefreshEvent;
import com.littleblack.springbootmall.model.Product;
import com.littleblack.springbootmall.model.ProductEmbedding;
import com.littleblack.springbootmall.service.MenuContextService;
import com.littleblack.springbootmall.service.RAGService;
import org.springframework.stereotype.Service;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RAGServiceImpl implements RAGService {

    private final MenuContextService menuContextService;
    private final EmbeddingClient embeddingClient;
    private final AtomicReference<Map<String, ProductEmbedding>> productEmbeddings;
    private static final int TOP_K_RESULTS = 3;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 1000L;
    private static final int BATCH_SIZE = 10;

    @Autowired
    public RAGServiceImpl(MenuContextService menuContextService,
                          EmbeddingClient embeddingClient) {
        this.menuContextService = menuContextService;
        this.embeddingClient = embeddingClient;
        this.productEmbeddings = new AtomicReference<>(new ConcurrentHashMap<>());
    }

    @PostConstruct
    public void initialize() {
        log.info("系統初始化，開始更新產品向量表示");
        retryUpdateProductEmbeddings();
    }

    @EventListener(CacheRefreshEvent.class)
    public void handleCacheRefresh(CacheRefreshEvent event) {
        log.info("接收到緩存刷新事件，開始更新向量嵌入");
        retryUpdateProductEmbeddings();
    }

    @Retryable(maxAttempts = MAX_RETRIES, backoff = @Backoff(delay = RETRY_DELAY))
    private void retryUpdateProductEmbeddings() {
        try {
            updateProductEmbeddings();
            log.info("成功更新產品向量表示");
        } catch (Exception e) {
            log.error("重試更新產品向量表示時發生錯誤", e);
            throw e;
        }
    }

    private void updateProductEmbeddings() {
        try {
            List<Product> products = menuContextService.getAllProducts();
            if (products.isEmpty()) {
                log.warn("沒有找到任何產品資料");
                return;
            }

            Map<String, ProductEmbedding> newEmbeddings = new ConcurrentHashMap<>();
            List<List<Product>> batches = createBatches(products, BATCH_SIZE);

            for (List<Product> batch : batches) {
                processBatch(batch, newEmbeddings);
            }

            productEmbeddings.set(newEmbeddings);
            log.info("成功更新產品向量表示，總共處理 {} 個產品", newEmbeddings.size());
        } catch (Exception e) {
            log.error("更新產品向量表示時發生錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("更新產品向量表示失敗", e);
        }
    }

    private List<List<Product>> createBatches(List<Product> items, int batchSize) {
        List<List<Product>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            batches.add(items.subList(i, Math.min(items.size(), i + batchSize)));
        }
        return batches;
    }

    private void processBatch(List<Product> batch, Map<String, ProductEmbedding> embeddings) {
        for (Product product : batch) {
            try {
                if (product == null || product.getProductName() == null) {
                    log.warn("跳過無效的產品資料");
                    continue;
                }

                String combinedText = createCombinedText(product);
                List<Double> embedding = embeddingClient.embed(combinedText);

                if (embedding == null || embedding.isEmpty()) {
                    log.warn("產品 {} 的向量嵌入為空", product.getProductName());
                    continue;
                }

                ProductEmbedding productEmbedding = createProductEmbedding(product, combinedText, embedding);
                embeddings.put(product.getProductName(), productEmbedding);
                log.debug("成功處理產品 {}", product.getProductName());
            } catch (Exception e) {
                log.error("處理產品 {} 時發生錯誤: {}", product.getProductName(), e.getMessage());
            }
        }
    }

    private ProductEmbedding createProductEmbedding(Product product, String combinedText, List<Double> embedding) {
        float[] embeddingArray = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            embeddingArray[i] = embedding.get(i).floatValue();
        }

        ProductEmbedding productEmbedding = new ProductEmbedding();
        productEmbedding.setProduct(product);
        productEmbedding.setCombinedText(combinedText);
        productEmbedding.setEmbedding(embeddingArray);
        return productEmbedding;
    }

    private String createCombinedText(Product product) {
        StringBuilder text = new StringBuilder();
        text.append("產品名稱：").append(product.getProductName()).append("\n");
        text.append("類別：").append(product.getCategory().name()).append("\n");
        text.append("價格：$").append(product.getPrice()).append("\n");
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            text.append("描述：").append(product.getDescription());
        }
        return text.toString();
    }

    @Override
    public List<Product> retrieveRelevantProducts(String query) {
        try {
            List<Double> queryEmbedding = embeddingClient.embed(query);
            float[] queryEmbeddingArray = new float[queryEmbedding.size()];
            for (int i = 0; i < queryEmbedding.size(); i++) {
                queryEmbeddingArray[i] = queryEmbedding.get(i).floatValue();
            }

            // 修正：先獲取 Map 對象
            Map<String, ProductEmbedding> currentEmbeddings = productEmbeddings.get();

            return currentEmbeddings.values().stream()
                    .map(pe -> new AbstractMap.SimpleEntry<>(
                            pe.getProduct(),
                            calculateCosineSimilarity(queryEmbeddingArray, pe.getEmbedding())
                    ))
                    .sorted(Map.Entry.<Product, Float>comparingByValue().reversed())
                    .limit(TOP_K_RESULTS)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("檢索相關產品時發生錯誤：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private float calculateCosineSimilarity(float[] vec1, float[] vec2) {
        float dotProduct = 0;
        float norm1 = 0;
        float norm2 = 0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        norm1 = (float) Math.sqrt(norm1);
        norm2 = (float) Math.sqrt(norm2);

        if (norm1 == 0 || norm2 == 0) {
            return 0;
        }

        return dotProduct / (norm1 * norm2);
    }

    @Override
    public String generateEnhancedPrompt(String userQuery) {
        List<Product> relevantProducts = retrieveRelevantProducts(userQuery);

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("以下是我們為您推薦的菜單項目：\n\n");

        for (Product product : relevantProducts) {
            contextBuilder.append("- ").append(product.getProductName())
                    .append("（$").append(product.getPrice()).append("）\n");
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                contextBuilder.append("  ").append(product.getDescription()).append("\n");
            }
            contextBuilder.append("\n");
        }

        return contextBuilder.toString();
    }
}