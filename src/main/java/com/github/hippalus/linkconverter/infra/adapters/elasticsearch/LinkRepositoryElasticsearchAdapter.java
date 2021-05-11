package com.github.hippalus.linkconverter.infra.adapters.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hippalus.linkconverter.domain.LinkRepository;
import com.github.hippalus.linkconverter.domain.model.LinkPair;
import com.github.hippalus.linkconverter.infra.adapters.elasticsearch.config.ElasticsearchProperties;
import com.github.hippalus.linkconverter.infra.adapters.elasticsearch.document.LinkPairDocument;
import com.github.hippalus.linkconverter.infra.common.exception.LinkNotFoundException;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkRepositoryElasticsearchAdapter implements LinkRepository {

  private final ObjectMapper objectMapper;
  private final ElasticsearchProperties properties;
  private final RestHighLevelClient restHighLevelClient;

  @Override
  @SneakyThrows
  public void save(LinkPair linkPair) {
    var linkPairDocument = LinkPairDocument.fromModel(linkPair);
    var document = toJsonStr(linkPairDocument);
    if (document != null) {
      var indexRequest = new IndexRequest(properties.getLinkIndexName());
      indexRequest.timeout(TimeValue.timeValueSeconds(properties.getRequestTimeout()));
      indexRequest.source(document, XContentType.JSON);
      restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);//TODO: could be async
    }
  }

  @Override
  @SneakyThrows
  public LinkPair findOneBySource(String sourceLink) {
    var queryBuilder = QueryBuilders.termQuery("source.keyword", sourceLink);
    var findOneBySource = newSearchSourceBuilder().query(queryBuilder).size(1);
    var searchRequest = newSearchRequest(properties.getLinkIndexName()).source(findOneBySource);
    var response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    var hits = response.getHits().getHits();
    return Arrays.stream(hits)
        .map(hit -> toDocument(hit.getSourceAsString()))
        .filter(Objects::nonNull)
        .findFirst()
        .map(LinkPairDocument::toModel)
        .stream()
        .findFirst()
        .orElseThrow(() -> new LinkNotFoundException("Link: " + sourceLink + " not found"));
  }

  private static SearchSourceBuilder newSearchSourceBuilder() {
    return new SearchSourceBuilder();
  }

  private static SearchRequest newSearchRequest(String indexName) {
    return new SearchRequest(indexName);
  }

  @Nullable
  private String toJsonStr(LinkPairDocument linkPair) {
    try {
      return objectMapper.writeValueAsString(linkPair);
    } catch (Exception e) {
      log.error("Exception has been occurred", e);
    }
    return null;
  }

  @Nullable
  private LinkPairDocument toDocument(String jsonDoc) {
    try {
      return objectMapper.readValue(jsonDoc, LinkPairDocument.class);
    } catch (Exception e) {
      log.error("Exception has been occurred", e);
    }
    return null;
  }
}

