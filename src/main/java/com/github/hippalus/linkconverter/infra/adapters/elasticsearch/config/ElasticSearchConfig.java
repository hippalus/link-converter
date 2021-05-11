package com.github.hippalus.linkconverter.infra.adapters.elasticsearch.config;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticSearchConfig {

  private final ElasticsearchProperties properties;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  @Bean(destroyMethod = "close")
  public RestHighLevelClient restHighLevelClient() {
    log.debug("ElasticSearch Properties: {}", properties.toString());
    return new RestHighLevelClient(buildRestClient());
  }

  private RestClientBuilder buildRestClient() {
    return RestClient.builder(getHttpHosts())
        .setHttpClientConfigCallback(buildHttpClientCallBack())
        .setRequestConfigCallback(buildRequestConfigCallBack());
  }

  @NotNull
  private RestClientBuilder.HttpClientConfigCallback buildHttpClientCallBack() {
    PoolingNHttpClientConnectionManager cm = configureConnectionManager();
    long period = properties.getMaxIdleTimeMs() / 2;
    executor.scheduleAtFixedRate(closeExpiredConnections(cm), properties.getMaxIdleTimeMs(), period, TimeUnit.MILLISECONDS);
    return httpAsyncClientBuilder -> httpAsyncClientBuilder
        .setConnectionManager(cm)
        .setMaxConnPerRoute(properties.getMaxInflightRequests())
        .setMaxConnTotal(properties.getMaxInflightRequests())
        .setDefaultCredentialsProvider(getCredentialsProvider());
  }

  @NotNull
  private Runnable closeExpiredConnections(PoolingNHttpClientConnectionManager cm) {
    return () -> {
      cm.closeExpiredConnections();
      cm.closeIdleConnections(properties.getMaxIdleTimeMs(), TimeUnit.MILLISECONDS);
      log.debug("Expired and idle connections closed");
    };
  }

  @SneakyThrows
  private PoolingNHttpClientConnectionManager configureConnectionManager() {
    ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
    PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
    cm.setDefaultMaxPerRoute(properties.getMaxInflightRequests());
    cm.setMaxTotal(properties.getMaxInflightRequests());
    return cm;
  }


  @NotNull
  private RestClientBuilder.RequestConfigCallback buildRequestConfigCallBack() {
    return requestConfigBuilder -> requestConfigBuilder
        .setConnectTimeout(properties.getConnectionTimeoutMs())
        .setContentCompressionEnabled(properties.isCompression())
        .setConnectionRequestTimeout(properties.getRequestTimeout())
        .setSocketTimeout(properties.getSocketTimeoutMs());
  }

  private CredentialsProvider getCredentialsProvider() {
    var credentials = new UsernamePasswordCredentials(properties.getIndexClusterUsername(), properties.getIndexClusterPassword());
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, credentials);
    return credentialsProvider;
  }


  private HttpHost[] getHttpHosts() {
    return Stream.of(properties.getIndexClusterHosts())
        .map(this::createUrl)
        .map(u -> new HttpHost(u.getHost(), u.getPort(), u.getProtocol()))
        .toArray(HttpHost[]::new);
  }

  private URL createUrl(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(url + Arrays.toString(properties.getIndexClusterHosts()));
    }
  }
}