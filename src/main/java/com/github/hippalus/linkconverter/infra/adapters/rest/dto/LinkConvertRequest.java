package com.github.hippalus.linkconverter.infra.adapters.rest.dto;

import com.github.hippalus.linkconverter.domain.model.command.LinkConvert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkConvertRequest {

  private String url;

  public LinkConvert toModel() {
    return LinkConvert.of(url);
  }
}
