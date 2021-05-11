package com.github.hippalus.linkconverter.domain;

import com.github.hippalus.linkconverter.domain.model.LinkPair;

public interface LinkRepository {

  void save(LinkPair linkPair);

  LinkPair findOneBySource(String sourceLink);

}
