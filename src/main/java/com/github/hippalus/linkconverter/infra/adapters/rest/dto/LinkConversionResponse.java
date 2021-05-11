package com.github.hippalus.linkconverter.infra.adapters.rest.dto;

import com.github.hippalus.linkconverter.domain.model.URI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkConversionResponse {

  private String url;

  public static LinkConversionResponse fromModel(URI uri) {
    return new LinkConversionResponse(uri.getUrl());
  }

}
