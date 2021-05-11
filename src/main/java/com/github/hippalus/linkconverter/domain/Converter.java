package com.github.hippalus.linkconverter.domain;

import org.jetbrains.annotations.NotNull;

/*
 * @param <T> Target type
 * @Param <S> Source type
 * */
public interface Converter<S, T> {

  boolean isMatched(@NotNull S link);

  @NotNull T convertFrom(@NotNull S link);

}
