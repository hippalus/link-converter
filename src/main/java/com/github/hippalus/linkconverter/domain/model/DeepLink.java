package com.github.hippalus.linkconverter.domain.model;

import static com.github.hippalus.linkconverter.domain.Constants.BOUTIQUE_ID;
import static com.github.hippalus.linkconverter.domain.Constants.CAMPAIGN_ID;
import static com.github.hippalus.linkconverter.domain.Constants.CONTENT_ID;
import static com.github.hippalus.linkconverter.domain.Constants.HOST;
import static com.github.hippalus.linkconverter.domain.Constants.HTTPS;
import static com.github.hippalus.linkconverter.domain.Constants.MERCHANT_ID;
import static com.github.hippalus.linkconverter.domain.Constants.MERCHANT_ID_LOW;
import static com.github.hippalus.linkconverter.domain.Constants.PAGE;
import static com.github.hippalus.linkconverter.domain.Constants.PRODUCT;
import static com.github.hippalus.linkconverter.domain.Constants.QUERY;
import static java.util.Optional.ofNullable;

import com.github.hippalus.linkconverter.domain.Converter;
import com.github.hippalus.linkconverter.domain.common.LinkConversionException;
import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.util.UriComponentsBuilder;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeepLink extends URI {

  public static final DeepLink DEFAULT_DEEP_LINK = DeepLink.of("ty://?Page=Home");

  private DeepLink(@NotNull String url) {
    super(url);
    checkUrl();
  }

  private void checkUrl() {
    if (!"ty".equals(this.scheme)) {
      throw new LinkConversionException("Invalid schema please use ty");
    }
  }

  public static DeepLink of(@NotNull String url) {
    return new DeepLink(url);
  }

  public WebLink toWebLink() {
    return PageURLs.filterAndConvertToWebLink(this);
  }


  public enum PageURLs implements Converter<DeepLink, WebLink> {
    PRODUCT_DETAIL {
      @Override
      public boolean isMatched(@NotNull DeepLink deepLink) {
        return ofNullable(deepLink.queryPairs.get(PAGE))
            .map(values -> PRODUCT.equals(values.get(0)))
            .orElse(false);

      }

      @Override
      public @NotNull WebLink convertFrom(@NotNull DeepLink deepLink) {
        UriComponentsBuilder uriBuilder = getConstantUriBuilder();

        ofNullable(deepLink.queryPairs.get(CONTENT_ID))
            .ifPresent(value -> uriBuilder.path("/brand").path(String.format("/name-p-%s", value.get(0))));

        ofNullable(deepLink.queryPairs.get(CAMPAIGN_ID))
            .ifPresent(value -> uriBuilder.queryParam(BOUTIQUE_ID, value.get(0)));

        ofNullable(deepLink.queryPairs.get(MERCHANT_ID))
            .ifPresent(value -> uriBuilder.queryParam(MERCHANT_ID_LOW, value.get(0)));

        return WebLink.of(uriBuilder.build().toUriString());
      }
    },
    SEARCH {
      @Override
      public boolean isMatched(@NotNull DeepLink deepLink) {
        return ofNullable(deepLink.queryPairs.get(PAGE))
            .map(values -> "Search".equals(values.get(0)))
            .orElse(false)
            && ofNullable(deepLink.queryPairs.get(QUERY)).isPresent();
      }

      @Override
      public @NotNull WebLink convertFrom(@NotNull DeepLink deepLink) {
        UriComponentsBuilder uriBuilder = getConstantUriBuilder();

        ofNullable(deepLink.queryPairs.get(QUERY))
            .ifPresent(value -> uriBuilder.path("/sr").queryParam("q", value.get(0)));

        return WebLink.of(uriBuilder.build().toUriString());
      }
    },
    DEFAULT {
      @Override
      public boolean isMatched(@NotNull DeepLink deepLink) {
        return true;
      }

      @Override
      public @NotNull WebLink convertFrom(@NotNull DeepLink deepLink) {
        return WebLink.DEFAULT_WEB_LINK;
      }
    };

    @NotNull
    private static UriComponentsBuilder getConstantUriBuilder() {
      return UriComponentsBuilder.newInstance()
          .scheme(HTTPS)
          .host(HOST);
    }

    public static WebLink filterAndConvertToWebLink(DeepLink deepLink) {
      return Arrays.stream(values())
          .filter(pageURLs -> pageURLs.isMatched(deepLink))
          .map(pageURLs -> pageURLs.convertFrom(deepLink))
          .findFirst()
          .orElseGet(() -> DEFAULT.convertFrom(deepLink));
    }
  }
}
