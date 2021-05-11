package com.github.hippalus.linkconverter.infra.adapters.rest;

import com.github.hippalus.linkconverter.domain.LinkConverterFacade;
import com.github.hippalus.linkconverter.infra.adapters.rest.dto.LinkConversionResponse;
import com.github.hippalus.linkconverter.infra.adapters.rest.dto.LinkConvertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/link-converter")
public class LinkConverterController {

  private final LinkConverterFacade linkConverterFacade;

  @PostMapping("/weblink-to-deeplink")
  public ResponseEntity<?> toDeepLink(@RequestBody LinkConvertRequest request) {
    var deepLink = linkConverterFacade.toDeepLink(request.toModel());
    return ResponseEntity.ok(LinkConversionResponse.fromModel(deepLink));
  }

  @PostMapping("/deeplink-to-weblink")
  public ResponseEntity<?> toWebLink(@RequestBody LinkConvertRequest request) {
    var webLink = linkConverterFacade.toWebLink(request.toModel());
    return ResponseEntity.ok(LinkConversionResponse.fromModel(webLink));
  }

}