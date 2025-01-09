package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.event.CacheRefreshEvent;
import com.littleblack.springbootmall.service.MenuContextService;
import com.littleblack.springbootmall.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cache")
public class CacheController {
    private final MenuContextService menuContextService;
    private final RAGService ragService;
    private final ApplicationEventPublisher eventPublisher;
    private LocalDateTime lastRefreshTime;
    private int refreshCount = 0;
    private static final long MIN_REFRESH_INTERVAL_SECONDS = 30;

    @Autowired
    public CacheController(MenuContextService menuContextService,
                           RAGService ragService,
                           ApplicationEventPublisher eventPublisher) {
        this.menuContextService = menuContextService;
        this.ragService = ragService;
        this.eventPublisher = eventPublisher;
        this.lastRefreshTime = LocalDateTime.now();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshCache() {
        log.info("接收到緩存刷新請求");
        Map<String, String> response = new HashMap<>();

        if (shouldThrottleRefresh()) {
            String message = String.format("緩存刷新請求過於頻繁，請至少間隔 %d 秒", MIN_REFRESH_INTERVAL_SECONDS);
            log.warn(message);
            response.put("status", "throttled");
            response.put("message", message);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        }

        try {
            log.info("開始執行緩存刷新");
            menuContextService.refreshCache();
            eventPublisher.publishEvent(new CacheRefreshEvent(this));

            updateRefreshMetrics();

            response.put("status", "success");
            response.put("message", "緩存刷新成功");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("refreshCount", String.valueOf(refreshCount));

            log.info("緩存刷新完成");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("緩存刷新失敗", e);
            response.put("status", "error");
            response.put("message", "緩存刷新失敗: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("lastRefreshTime", lastRefreshTime.toString());
        status.put("refreshCount", refreshCount);
        status.put("timeSinceLastRefresh",
                LocalDateTime.now().getSecond() - lastRefreshTime.getSecond());

        return ResponseEntity.ok(status);
    }

    private boolean shouldThrottleRefresh() {
        return lastRefreshTime != null &&
                LocalDateTime.now().isBefore(lastRefreshTime.plusSeconds(MIN_REFRESH_INTERVAL_SECONDS));
    }

    private void updateRefreshMetrics() {
        lastRefreshTime = LocalDateTime.now();
        refreshCount++;
        log.info("更新緩存刷新指標 - 總刷新次數: {}, 最後刷新時間: {}", refreshCount, lastRefreshTime);
    }
}