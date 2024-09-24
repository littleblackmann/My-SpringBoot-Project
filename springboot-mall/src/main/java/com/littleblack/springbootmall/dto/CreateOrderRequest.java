package com.littleblack.springbootmall.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateOrderRequest {

    @NotEmpty // buyItemList 不可為空
    private List<BuyItem> buyItemList;

    @NotNull // userEmail 不可為空
    private String userEmail; // 用戶的電子郵件

    @NotNull // totalAmount 不可為空
    private Integer totalAmount; // 總金額

    @NotNull // arrivalDate 不可為空
    private String arrivalDate; // 到達日期

    @NotNull // arrivalTime 不可為空
    private String arrivalTime; // 到達時間

    @NotNull // phoneNumber 不可為空
    private String phoneNumber; // 聯絡電話

    // 原有的 Getter 和 Setter 方法
    public List<BuyItem> getBuyItemList() {
        return buyItemList;
    }

    public void setBuyItemList(List<BuyItem> buyItemList) {
        this.buyItemList = buyItemList;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    // 新增字段的 Getter 和 Setter 方法
    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}