package com.n26.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cache")
@Data
public class TxnStatCacheConfig {
    private Integer window;
    private Integer step;
}
