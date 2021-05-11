package com.github.hippalus.linkconverter.domain.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkPair {

  private final String source;
  private final String target;

  public static LinkPair of(@NotNull String source, @NotNull String target) {
    return new LinkPair(source, target);
  }

}
