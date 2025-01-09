package com.littleblack.springbootmall.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

@Getter
public class CacheRefreshEvent extends ApplicationEvent {
    private final LocalDateTime eventTime;
    private final String eventSource;
    private final String triggerType;
    private final String description;

    public CacheRefreshEvent(Object source, String triggerType, String description) {
        super(source);
        this.eventTime = LocalDateTime.now();
        this.eventSource = source.getClass().getSimpleName();
        this.triggerType = triggerType;
        this.description = description;
    }

    public CacheRefreshEvent(Object source) {
        this(source, "MANUAL", "手動觸發的緩存刷新");
    }

    @Override
    public String toString() {
        return String.format("CacheRefreshEvent{eventTime=%s, eventSource='%s', triggerType='%s', description='%s'}",
                eventTime, eventSource, triggerType, description);
    }
}