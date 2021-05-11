package com.github.hippalus.linkconverter.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.hippalus.linkconverter.domain.common.LinkConversionException;
import com.github.hippalus.linkconverter.domain.model.DeepLink;
import com.github.hippalus.linkconverter.domain.model.DeepLink.PageURLs;
import com.github.hippalus.linkconverter.domain.model.WebLink;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DeepLinkTest {

  @Test
  void productDetailDeepLinkMustContain_PageProduct_queryPair() {
    //given:
    var deepLink = "ty://?Page=Product&ContentId=1925865&CampaignId=439892&MerchantId=105064";
    //when:
    var matched = PageURLs.PRODUCT_DETAIL.isMatched(DeepLink.of(deepLink));
    //then:
    assertThat(matched).isTrue();
  }

  @Test
  void productDetailDeepLinkMustContain_PageProduct_queryPair_2() {
    //given: //Category page is invalid  query param
    var deepLink = "ty://?Page=Category&ContentId=1925865&CampaignId=439892&MerchantId=105064";
    //when:
    var webLink = DeepLink.of(deepLink).toWebLink();
    //then:
    assertThat(webLink)
        .isNotEqualTo(WebLink.of("https://www.trendyol.com/brand/name-p-1925865?boutiqueId=439892&merchantId=1050"));
  }

  @ParameterizedTest
  @MethodSource("provideDeepLinkForWebLinkConversionProductPage")
  void addPath_brand_and_name_ifContain_ContentId_queryPair_addQueryParam_boutiqueId_merchantId_ifContains_CampaignId_MerchantId(
      String deepLinkStr, WebLink expected) {
    //when:
    var webLink = DeepLink.of(deepLinkStr).toWebLink();
    //then:
    assertThat(webLink).isEqualTo(expected);

  }

  private static Stream<Arguments> provideDeepLinkForWebLinkConversionProductPage() {
    //@formatter:off
    return Stream.of(
        Arguments.of("ty://?Page=Product&ContentId=1925865", WebLink.of("https://www.trendyol.com/brand/name-p-1925865")),
        Arguments.of("ty://?Page=Product&ContentId=1925865&CampaignId=439892", WebLink.of("https://www.trendyol.com/brand/name-p-1925865?boutiqueId=439892")),
        Arguments.of("ty://?Page=Product&ContentId=1925865&&MerchantId=105064", WebLink.of("https://www.trendyol.com/brand/name-p-1925865?merchantId=105064")),
        Arguments.of("ty://?Page=Product&ContentId=1925865&CampaignId=439892&MerchantId=105064", WebLink.of("https://www.trendyol.com/brand/name-p-1925865?boutiqueId=439892&merchantId=105064")),
        Arguments.of("ty://?Page=Product&ContentId=1925865", WebLink.of("https://www.trendyol.com/brand/name-p-1925865")) ,
        Arguments.of("ty://?Page=Product&ContentId=1925865&CampaignId=439892", WebLink.of("https://www.trendyol.com/brand/name-p-1925865?boutiqueId=439892"))
    );
    //@formatter:on
  }

  @ParameterizedTest
  @MethodSource("provideDeepLinkForWebLinkConversionSearchPage")
  void addPath_sr_addQueryParam_q_ifContain_Query_queryPair(String deepLinkStr, WebLink expected) {
    //when:
    var webLink = DeepLink.of(deepLinkStr).toWebLink();
    //then:
    assertThat(webLink).isEqualTo(expected);

  }

  private static Stream<Arguments> provideDeepLinkForWebLinkConversionSearchPage() {
    return Stream.of(
        Arguments.of("ty://?Page=Search&Query=elbise", WebLink.of("https://www.trendyol.com/sr?q=elbise")),
        Arguments.of("ty://?Page=Search&Query=%C3%BCt%C3%BC", WebLink.of("https://www.trendyol.com/sr?q=%C3%BCt%C3%BC"))
    );
  }

  @ParameterizedTest
  @MethodSource("provideDeepLinkForWebLinkConversionSearchPage")
  void defaultDeepLink(String deepLinkStr, WebLink expected) {
    //when:
    var webLink = DeepLink.of(deepLinkStr).toWebLink();
    //then:
    assertThat(webLink).isEqualTo(expected);

  }

  private static Stream<Arguments> defaultDeepLinkForWeb() {
    return Stream.of(
        Arguments.of("ty://?Page=Favorites", WebLink.DEFAULT_WEB_LINK),
        Arguments.of("ty://?Page=Orders", WebLink.DEFAULT_WEB_LINK)
    );
  }

  @Test
  void whenGivenInvalidUrlThrownAnException() {
    var invalidSchema = "//?Page=Favorites";

    assertThatThrownBy(() -> DeepLink.of(invalidSchema))
        .isInstanceOf(LinkConversionException.class)
        .hasMessage("Invalid schema please use ty");
  }
}
