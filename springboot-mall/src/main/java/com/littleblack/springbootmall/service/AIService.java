package com.littleblack.springbootmall.service;

public interface AIService {
    String generateResponse(String prompt);
    String generateProductRecommendation(String userPreference);
    String generateMenuResponse(String category);
    String generatePriceQuery(String productName);

}