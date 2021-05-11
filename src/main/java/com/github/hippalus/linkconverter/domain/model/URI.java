package com.github.hippalus.linkconverter.domain.model;

import com.github.hippalus.linkconverter.domain.common.DeASCIIFier;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;


@Data
@Builder
@RequiredArgsConstructor
public class URI {

  protected final String url;
  protected final String scheme;
  protected final String hostname;
  protected final String rawPath;
  protected final String query;
  protected final String fragment;
  protected final List<String> pathElements;
  protected final MultiValueMap<String, String> queryPairs;

  protected URI(@NotNull String url) {
    var clearUrl = DeASCIIFier.clearTurkishChars(url);
    var uriComponents = UriComponentsBuilder.fromUriString(clearUrl).build();
    this.url = clearUrl;
    this.scheme = uriComponents.getScheme();
    this.hostname = uriComponents.getHost();
    this.fragment = uriComponents.getFragment();
    this.query = uriComponents.getQuery();
    this.rawPath = uriComponents.getPath();
    this.pathElements = uriComponents.getPathSegments();
    this.queryPairs = uriComponents.getQueryParams();
  }

}
