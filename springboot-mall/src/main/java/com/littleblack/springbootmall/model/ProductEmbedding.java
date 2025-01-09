package com.littleblack.springbootmall.model;

import lombok.Data;

@Data
public class ProductEmbedding {
    private Product product;
    private String combinedText; // 產品名稱和描述的組合
    private float[] embedding;   // 向量表示
}