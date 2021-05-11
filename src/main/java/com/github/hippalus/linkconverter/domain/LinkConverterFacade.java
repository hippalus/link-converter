package com.github.hippalus.linkconverter.domain;

import com.github.hippalus.linkconverter.domain.model.DeepLink;
import com.github.hippalus.linkconverter.domain.model.LinkPair;
import com.github.hippalus.linkconverter.domain.model.WebLink;
import com.github.hippalus.linkconverter.domain.model.command.LinkConvert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkConverterFacade {

  private final LinkRepository linkRepository;

  public DeepLink toDeepLink(LinkConvert cmd) {
    var webLink = WebLink.of(cmd.getUrl());
    var deepLink = webLink.toDeepLink();
    save(LinkPair.of(webLink.getUrl(), deepLink.getUrl()));
    return deepLink;
  }

  public WebLink toWebLink(LinkConvert cmd) {
    var deepLink = DeepLink.of(cmd.getUrl());
    var webLink = deepLink.toWebLink();
    save(LinkPair.of(deepLink.getUrl(), webLink.getUrl()));
    return webLink;
  }

  public void save(LinkPair linkPair) {
    linkRepository.save(linkPair);
  }
}
