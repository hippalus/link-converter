package com.github.hippalus.linkconverter.infra.common.exception;

public class LinkNotFoundException extends RuntimeException {

  public LinkNotFoundException(String message) {
    super(message);
  }
}
