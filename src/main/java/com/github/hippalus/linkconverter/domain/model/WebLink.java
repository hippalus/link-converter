package com.github.hippalus.linkconverter.domain.model;

import static com.github.hippalus.linkconverter.domain.Constants.BOUTIQUE_ID;
import static com.github.hippalus.linkconverter.domain.Constants.CAMPAIGN_ID;
import static com.github.hippalus.linkconverter.domain.Constants.CONTENT_ID;
import static com.github.hippalus.linkconverter.domain.Constants.HOST;
import static com.github.hippalus.linkconverter.domain.Constants.HOST_ALTERNATIVE;
import static com.github.hippalus.linkconverter.domain.Constants.MERCHANT_ID;
import static com.github.hippalus.linkconverter.domain.Constants.MERCHANT_ID_LOW;
import static com.github.hippalus.linkconverter.domain.Constants.PAGE;
import static com.github.hippalus.linkconverter.domain.Constants.PRODUCT;
import static com.github.hippalus.linkconverter.domain.Constants.Q;
import static com.github.hippalus.linkconverter.domain.Constants.QUERY;
import static java.util.Optional.ofNullable;

import com.github.hippalus.linkconverter.domain.Constants;
import com.github.hippalus.linkconverter.domain.Converter;
import com.github.hippalus.linkconverter.domain.common.LinkConversionException;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.util.UriComponentsBuilder;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class WebLink extends URI {

  public static final WebLink DEFAULT_WEB_LINK = WebLink.of("https://www.trendyol.com");

  private WebLink(final String url) {
    super(url);
    checkUrl();
  }

  private void checkUrl() {
    if (this.hostname == null || !Set.of(HOST, HOST_ALTERNATIVE).contains(this.hostname)) {
      throw new LinkConversionException("Unknown host please use " + HOST + " or " + HOST_ALTERNATIVE);
    }
  }

  public static WebLink of(final String url) {
    return new WebLink(url);
  }

  public DeepLink toDeepLink() {
    return PageURLs.filterAndConvertToDeepLink(this);
  }

  public enum PageURLs implements Converter<WebLink, DeepLink> {
    PRODUCT_DETAIL {
      private static final int PRODUCT_ELEMENT = 1;
      private static final int CONTENT_ID_INDEX = 1;
      private final Pattern regex = Pattern.compile("(.*)-p-(\\d+)");

      @Override
      public boolean isMatched(@NotNull WebLink link) {
        return link.pathElements.size() == 2 && regex.matcher(link.pathElements.get(PRODUCT_ELEMENT)).matches();
      }

      @Override
      public @NotNull DeepLink convertFrom(@NotNull WebLink link) {
        var uriBuilder = getConstantUrlBuilder()
            .queryParam(PAGE, PRODUCT)
            .queryParam(CONTENT_ID, getContentId(link));

        ofNullable(link.queryPairs.get(BOUTIQUE_ID))
            .ifPresent(value -> uriBuilder.queryParam(CAMPAIGN_ID, value.get(0)));

        ofNullable(link.queryPairs.get(MERCHANT_ID_LOW))
            .ifPresent(value -> uriBuilder.queryParam(MERCHANT_ID, value.get(0)));

        return DeepLink.of(uriBuilder.build().toUriString());
      }

      private String getContentId(URI webLink) {
        var productAndContentId = webLink.pathElements.get(PRODUCT_ELEMENT).split("-p-");
        return productAndContentId[CONTENT_ID_INDEX];
      }
    },
    SEARCH {
      @Override
      public boolean isMatched(@NotNull WebLink link) {
        return link.rawPath != null && link.rawPath.startsWith("/sr") && link.queryPairs.containsKey(Q);
      }

      @Override
      public @NotNull DeepLink convertFrom(@NotNull WebLink link) {
        var uriComponents = getConstantUrlBuilder()
            .queryParam(PAGE, Constants.SEARCH)
            .queryParam(QUERY, link.queryPairs.get(Q))
            .build();
        return DeepLink.of(uriComponents.toUriString());
      }
    },
    DEFAULT {
      @Override
      public boolean isMatched(@NotNull WebLink link) {
        return true;
      }

      @Override
      public @NotNull DeepLink convertFrom(@NotNull WebLink link) {
        return DeepLink.DEFAULT_DEEP_LINK;
      }
    };

    @NotNull
    private static UriComponentsBuilder getConstantUrlBuilder() {
      return UriComponentsBuilder.newInstance().scheme("ty").host("");
    }

    public static DeepLink filterAndConvertToDeepLink(WebLink webLink) {
      return Arrays.stream(values())
          .filter(pageURLs -> pageURLs.isMatched(webLink))
          .map(pageURLs -> pageURLs.convertFrom(webLink))
          .findFirst()
          .orElseGet(() -> DEFAULT.convertFrom(webLink));
    }
  }
}
