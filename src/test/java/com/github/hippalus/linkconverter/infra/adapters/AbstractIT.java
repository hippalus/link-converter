package com.github.hippalus.linkconverter.infra.adapters;


import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

@Slf4j
@ActiveProfiles("integrationTest")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
/*@TestPropertySource(value="classpath:application.yml")*/
public abstract class AbstractIT {

  @Autowired
  protected TestRestTemplate testRestTemplate;

  @LocalServerPort
  protected Integer port;

  protected static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:7.8.1";
  private static final String ELASTICSEARCH_HOST_PROPERTY = "elasticsearch.indexClusterHosts";
  private static final String ELASTICSEARCH_PASSWORD = "elasticsearch.indexClusterPassword";

  private static ElasticsearchContainer container;

  @BeforeAll
  static void beforeAll() {
    if (System.getProperty(ELASTICSEARCH_HOST_PROPERTY) == null) {
      log.debug("Create Elasticsearch container");
      var mappedPort = createContainer();
      System.setProperty(ELASTICSEARCH_HOST_PROPERTY, "http://localhost:" + mappedPort);
      System.setProperty(ELASTICSEARCH_PASSWORD, "changeme");
      var host = System.getProperty(ELASTICSEARCH_HOST_PROPERTY);
      log.debug("Created Elasticsearch container at {}", host);
    }
  }

  @AfterAll
  static void afterAll() {
    if (container != null) {
      String host = System.getProperty(ELASTICSEARCH_HOST_PROPERTY);
      log.debug("Removing Elasticsearch container at {}", host);
      container.stop();
    }
  }

  private static int createContainer() {
    ElasticsearchContainer container = new ElasticsearchContainer(ELASTICSEARCH_IMAGE);
    container.withEnv("cluster.name", "integration-test-cluster");
    container.withEnv("ELASTIC_PASSWORD", "changeme");
    container.start();
    return container.getMappedPort(9200);
  }

}
