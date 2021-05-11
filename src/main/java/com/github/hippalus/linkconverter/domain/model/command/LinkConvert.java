package com.github.hippalus.linkconverter.domain.model.command;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkConvert {

  private final String url;

  public static LinkConvert of(@NotNull String url) {
    return new LinkConvert(url);
  }
}
