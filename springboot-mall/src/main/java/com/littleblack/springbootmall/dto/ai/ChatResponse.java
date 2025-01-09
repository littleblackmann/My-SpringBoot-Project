package com.littleblack.springbootmall.dto.ai;

public class ChatResponse {
    private String response;

    // 添加無參數構造函數
    public ChatResponse() {
    }

    // 添加帶參數的構造函數
    public ChatResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}