package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.model.Product;
import java.util.List;

public interface RAGService {
    String generateEnhancedPrompt(String userQuery);
    List<Product> retrieveRelevantProducts(String query);
}