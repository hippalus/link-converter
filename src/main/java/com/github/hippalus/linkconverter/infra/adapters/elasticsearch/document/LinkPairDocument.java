package com.github.hippalus.linkconverter.infra.adapters.elasticsearch.document;

import com.github.hippalus.linkconverter.domain.model.LinkPair;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkPairDocument {

  private String source;
  private String target;
  @Builder.Default
  private Instant lastIndexedTime = Instant.now(); //utc

  public static LinkPairDocument fromModel(LinkPair linkPair) {
    return LinkPairDocument.builder()
        .source(linkPair.getSource())
        .target(linkPair.getTarget())
        .build();
  }

  public LinkPair toModel() {
    return LinkPair.of(source, target);
  }

}
