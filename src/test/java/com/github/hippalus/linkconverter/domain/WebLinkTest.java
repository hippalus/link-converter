package com.github.hippalus.linkconverter.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.hippalus.linkconverter.domain.common.LinkConversionException;
import com.github.hippalus.linkconverter.domain.model.DeepLink;
import com.github.hippalus.linkconverter.domain.model.WebLink;
import com.github.hippalus.linkconverter.domain.model.WebLink.PageURLs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WebLinkTest {

  @Test
  void productDetailPageURLMustContain_p_Text_validLink() {
    //given:
    var validProductLink = "https://www.trendyol.com/casio/mtp-1374d-2avdf-erkek-kol-saati-celik-p-1925865?boutiqueId=439892"
        + "&merchantId=105064";
    var webLink = WebLink.of(validProductLink);
    //when:
    boolean isProductPage = PageURLs.PRODUCT_DETAIL.isMatched(webLink);
    //then:
    assertThat(isProductPage).isTrue();
  }

  @Test
  void productDetailPageURLMustContain_p_Text_invalidLink() {
    //given:
    var invalidProductLink = "https://www.trendyol.com/casio/mtp-1374d-2avdf-erkek-kol-saati-celik1925865?boutiqueId=439892"
        + "&merchantId=105064";
    var webLink = WebLink.of(invalidProductLink);
    //when:
    var isProductPage = PageURLs.PRODUCT_DETAIL.isMatched(webLink);
    //then:
    assertThat(isProductPage).isFalse();
  }

  @Test
  void productDetailPageURLsMustContain_contentId_WhichIsLocatedAfter_p_prefix() {
    //given:
    var invalidProductLink = "https://www.trendyol.com/casio/mtp-1374d-2avdf-erkek-kol-saati-celik-p-?boutiqueId=439892"
        + "&merchantId=105064";
    var webLink = WebLink.of(invalidProductLink);
    //when:
    var isProductPage = PageURLs.PRODUCT_DETAIL.isMatched(webLink);
    //then:
    assertThat(isProductPage).isFalse();
  }

  @Test
  void ifURLDoesntContain_boutiqueId_should_notAdd_CampaignId_toDeepLink() {
    //given:
    var productLink = "https://www.trendyol.com/casio/mtp-1374d-2avdf-erkek-kol-saati-celik-p-1925865?merchantId=105064";
    //when:
    var deepLink = WebLink.of(productLink).toDeepLink();
    //then:
    assertThat(deepLink).isEqualTo(DeepLink.of("ty://?Page=Product&ContentId=1925865&MerchantId=105064"));
  }

  @Test
  void ifURLDoesntContain_merchantId_should_notAdd_MerchantId_toDeepLink() {
    //given:
    var productLink = "https://www.trendyol.com/casio/mtp-1374d-2avdf-erkek-kol-saati-celik-p-1925865?boutiqueId=439892";
    //when:
    var deepLink = WebLink.of(productLink).toDeepLink();
    //then:
    assertThat(deepLink).isEqualTo(DeepLink.of("ty://?Page=Product&ContentId=1925865&CampaignId=439892"));
  }

  @Test
  void validURL() {
    //given:
    var validProductLink = "https://www.trendyol.com/casio/mtp-1374d-2avdf-erkek-kol-saati-celik-p-1925865?boutiqueId=439892"
        + "&merchantId=105064";
    var webLink = WebLink.of(validProductLink);
    //when:
    var deepLink = PageURLs.PRODUCT_DETAIL.convertFrom(webLink);
    //then:
    assertThat(deepLink.getUrl()).isEqualTo("ty://?Page=Product&ContentId=1925865&CampaignId=439892&MerchantId=105064");
  }

  @Test
  void searchPagesPathMustStartWith_sr_and_contain_q() {
    //given:
    var searchLinkStr = "https://www.trendyol.com/sr?q=elbise";
    var invalidSearchLinkStr = "https://www.trendyol.com/";
    var validSearchLink = WebLink.of(searchLinkStr);
    var invalidSearchLink = WebLink.of(invalidSearchLinkStr);
    //when:
    var searchLink = PageURLs.SEARCH.isMatched(validSearchLink);
    var unknown = PageURLs.SEARCH.isMatched(invalidSearchLink);
    //then:
    assertThat(searchLink).isTrue();
    assertThat(unknown).isFalse();
  }

  @Test
  void searchPagesConvertToDeepLink() {
    //given:
    var searchLinkStr1 = "https://www.trendyol.com/sr?q=elbise";
    var searchLinkStr2 = "ttps://www.trendyol.com/sr?q=%C3%BCt%C3%BC";
    //when:
    var searchLink1 = WebLink.of(searchLinkStr1).toDeepLink();
    var searchLink2 = WebLink.of(searchLinkStr2).toDeepLink();
    //then:
    assertThat(searchLink1).isEqualTo(DeepLink.of("ty://?Page=Search&Query=elbise"));
    assertThat(searchLink2).isEqualTo(DeepLink.of("ty://?Page=Search&Query=%C3%BCt%C3%BC"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://www.trendyol.com", "https://www.trendyol.com/Hesabim/#/Siparislerim"})
  void notFilteredAsA_SearchPage_or_ProductDetailPage_mustBeConvertedAsEmpty_Homepage_Deeplink(String searchLink) {
    //when:
    var deepLink = WebLink.of(searchLink).toDeepLink();
    //then:
    assertThat(deepLink).isEqualTo(DeepLink.of("ty://?Page=Home"));
  }

  @Test
  void whenGivenInvalidUrlThrownAnException() {
    var invalidHost = "https://www.xyz.com";
    assertThatThrownBy(() -> WebLink.of(invalidHost))
        .isInstanceOf(LinkConversionException.class)
        .hasMessage("Unknown host please use www.trendyol.com or trendyol.com");

  }
}
