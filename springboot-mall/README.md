# 餐廳網站與智慧型客服系統

## 專案概述

這是一個融合現代科技與傳統餐飲服務的網站系統，旨在為一間充滿回憶的餐廳打造數位化的紀念空間。本系統採用前後端分離架構，結合了先進的人工智慧技術，不僅提供基礎的餐廳服務功能，更整合了智慧型對話系統，為使用者帶來更優質的線上互動體驗。

## 系統特色

### 智慧型客服系統
本系統的核心特色在於整合了先進的 AI 對話技術，具體功能包括：

1. 智慧型對話引擎
    - 採用 OpenAI API 提供強大的自然語言處理能力
    - 結合 RAG（Retrieval-Augmented Generation）技術，實現精準的資訊檢索
    - 具備上下文理解能力，確保對話的連貫性和準確性

2. 餐點推薦系統
    - 根據使用者喜好進行個人化推薦
    - 即時提供菜品詳細資訊
    - 智慧型解答餐點相關疑問

3. 訂單協助功能
    - 自動化訂單狀態查詢
    - 智慧型訂單問題處理
    - 即時回答付款相關問題

4. 知識庫整合
    - 自動更新最新菜單資訊
    - 整合餐廳營業資訊
    - 提供即時的餐廳相關諮詢

### 使用者服務系統
完整的使用者服務體系，包含：

1. 會員管理
    - 個人資料管理
    - 會員註冊及登入
    - 訂單歷史查詢

2. 購物車系統
    - 即時購物車管理
    - 訂單狀態追蹤
    - 電子郵件通知服務

3. 餐點瀏覽
    - 分類展示
    - 詳細資訊查看
    - 價格及庫存即時更新

## 技術架構

### 前端技術棧
1. 核心框架
    - Vue.js 框架
    - 開發環境：VS Code
    - 現代化響應式設計

2. 主要功能模組
    - 使用者介面元件
    - 狀態管理系統
    - 路由管理
    - API 介接層

### 後端技術棧
1. 核心框架
    - Spring Boot 架構
    - 開發環境：IntelliJ IDEA
    - RESTful API 設計

2. 資料庫系統
    - MySQL 資料庫
    - 資料持久層設計
    - 快取管理機制

3. AI 整合服務
    - OpenAI API 整合
    - RAG 檢索引擎
    - 向量資料庫設計

## 系統架構詳解

### 控制層（Controllers）
控制層負責處理所有的 HTTP 請求，主要包含：

1. AIController
    - 處理所有 AI 對話相關請求
    - 管理對話上下文
    - 整合 RAG 檢索結果

2. UserController
    - 處理使用者認證
    - 管理會員資訊
    - 處理個人化設定

3. OrderController
    - 訂單創建與管理
    - 購物車操作
    - 訂單狀態更新

4. ProductController
    - 產品資訊管理
    - 類別管理
    - 價格更新

5. CacheController
    - 系統快取管理
    - 資料更新機制
    - 效能優化控制

### 服務層（Services）
服務層包含核心業務邏輯：

1. AIService 與 AIServiceImpl
    - 實現 AI 對話核心邏輯
    - 管理對話狀態
    - 整合外部 AI 服務

2. MenuContextService 與 MenuContextServiceImpl
    - 菜單上下文管理
    - 動態資訊更新
    - 資料關聯處理

3. RAGService 與 RAGServiceImpl
    - 實現檢索增強生成
    - 管理向量資料庫
    - 優化檢索結果

4. OrderService 與 OrderServiceImpl
    - 訂單處理邏輯
    - 電子郵件通知
    - 訂單狀態管理

5. ProductService 與 ProductServiceImpl
    - 產品資訊處理
    - 庫存管理
    - 價格計算

6. UserService 與 UserServiceImpl
    - 使用者認證
    - 權限管理
    - 個人資料處理

### 資料訪問層（DAO）
資料訪問層處理所有與資料庫的互動：

1. OrderDao 與 OrderDaoImpl
    - 訂單資料存取
    - 訂單狀態更新
    - 歷史記錄管理

2. ProductDao 與 ProductDaoImpl
    - 產品資訊存取
    - 庫存更新
    - 類別管理

3. UserDao 與 UserDaoImpl
    - 使用者資料存取
    - 認證資訊管理
    - 權限資料處理

### 資料模型（Models）
系統核心資料模型：

1. 使用者相關
    - User：使用者基本資訊
    - UserLoginRequest：登入請求
    - UserRegisterRequest：註冊請求

2. 訂單相關
    - Order：訂單主體
    - OrderItem：訂單項目
    - OrderResponse：訂單響應
    - BuyItem：購買項目

3. 產品相關
    - Product：產品資訊
    - ProductCategory：產品分類
    - ProductEmbedding：產品向量嵌入

4. AI 對話相關
    - ChatRequest：對話請求
    - ChatResponse：對話響應

## 開發環境設置

### 系統需求
- 作業系統：macOS
- 開發工具：
    - 後端：IntelliJ IDEA
    - 前端：VS Code
- 資料庫：MySQL
- 外部服務：
    - OpenAI API
    - Gmail SMTP 服務

### 環境配置
1. 後端配置
```properties
# 資料庫設定
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# OpenAI 設定
spring.ai.openai.api-key=your_api_key
spring.ai.openai.model=gpt-4               # 使用的模型版本
spring.ai.openai.embedding.model=text-embedding-ada-002  # 用於RAG的嵌入模型
spring.ai.openai.temperature=0.7           # 回應的創造性程度設定

# 郵件服務設定
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_app_password
```

2. 前端配置
```bash
# 安裝相依套件
npm install

# 啟動開發伺服器
npm run serve
```

## 部署指南

### 後端部署
1. 編譯專案
```bash
./mvnw clean package
```

2. 執行應用
```bash
java -jar target/restaurant-app.jar
```

### 前端部署
1. 建置生產版本
```bash
npm run build
```

## 使用指南

### AI 客服系統使用說明
1. 系統初始化
    - 自動載入產品資料
    - 建立向量資料庫
    - 初始化對話模型

2. 對話功能
    - 支援自然語言查詢
    - 提供上下文相關回答
    - 即時更新餐點資訊

### 訂單系統使用說明
1. 購物流程
    - 瀏覽並選擇餐點
    - 加入購物車
    - 確認訂單資訊
    - 接收確認郵件

2. 訂單管理
    - 查看訂單狀態
    - 修改訂單內容
    - 取消訂單

## 維護指南

### 日常維護
1. 系統更新
    - 更新 OpenAI API 密鑰
    - 更新產品資訊
    - 檢查資料庫備份

2. 效能監控
    - 監控 AI 服務回應時間
    - 檢查資料庫效能
    - 監控系統資源使用

### 問題排除
1. AI 服務問題
    - 檢查 API 密鑰有效性
    - 確認網路連線狀態
    - 檢查對話日誌

2. 資料庫問題
    - 檢查連線狀態
    - 優化查詢效能
    - 進行資料備份

## 專案說明

本專案是一個個人紀念作品，旨在為曾經營運的餐廳創建一個現代化的網站系統。透過整合先進的 AI 技術，不僅保留了餐廳的回憶，更展現了科技與餐飲服務的完美結合。雖然系統主要用於本地使用，但仍在功能完整性和使用者體驗上力求完美。

## 未來展望

1. 功能擴充
    - 整合更多 AI 模型
    - 增加數據分析功能
    - 優化推薦系統

2. 效能提升
    - 優化對話回應速度
    - 改善資料檢索效率
    - 強化系統穩定性

## 作者資訊

個人開發專案，致力於將餐飲服務與現代科技完美結合，創造優質的使用者體驗。