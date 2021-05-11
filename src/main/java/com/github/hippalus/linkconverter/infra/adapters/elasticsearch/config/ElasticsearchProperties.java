package com.github.hippalus.linkconverter.infra.adapters.elasticsearch.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {

  private String[] indexClusterHosts;
  private String indexClusterUsername;
  private String indexClusterPassword;
  private String linkIndexName;
  private int maxInflightRequests;
  private int connectionTimeoutMs;
  private boolean compression;
  private int requestTimeout;
  private int socketTimeoutMs;
  private long maxIdleTimeMs;
}