package com.github.hippalus.linkconverter.infra.adapters;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.hippalus.linkconverter.domain.LinkRepository;
import com.github.hippalus.linkconverter.infra.adapters.rest.dto.LinkConvertRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
class LinkConverterIT extends AbstractIT {

  @Autowired
  LinkRepository linkRepository;

  private final ParameterizedTypeReference<Object> linkConversionResponseType =
      new ParameterizedTypeReference<>() {
      };

  @Test
  void shouldConvertWebLinkToDeepLink() {
    //@formatter:off
    toDeepLink("https://www.trendyol.com/casio/saat-p-1925865?boutiqueId=439892&merchantId=105064", "ty://?Page=Product&ContentId=1925865&CampaignId=439892&MerchantId=105064", HttpStatus.OK);
    toDeepLink("https://www.trendyol.com/casio/erkek-kol-saati-p-1925865", "ty://?Page=Product&ContentId=1925865", HttpStatus.OK);
    toDeepLink("https://www.trendyol.com/casio/erkek-kol-saati-p-1925865?boutiqueId=439892", "ty://?Page=Product&ContentId=1925865&CampaignId=439892", HttpStatus.OK);
    toDeepLink("https://www.trendyol.com/casio/erkek-kol-saati-p-1925865?merchantId=105064", "ty://?Page=Product&ContentId=1925865&MerchantId=105064", HttpStatus.OK);
    toDeepLink("https://www.trendyol.com/sr?q=elbise", "ty://?Page=Search&Query=elbise", HttpStatus.OK);
    toDeepLink("https://www.trendyol.com/sr?q=%C3%BCt%C3%BC", "ty://?Page=Search&Query=%C3%BCt%C3%BC", HttpStatus.OK);
    toDeepLink("https://www.trendyol.com/Hesabim/Favoriler", "ty://?Page=Home", HttpStatus.OK);
    toDeepLink("https://www.trendyol.com/Hesabim/#/Siparislerim", "ty://?Page=Home", HttpStatus.OK);
    toDeepLink("invalid", "invalid", HttpStatus.UNPROCESSABLE_ENTITY);
    //@formatter:on
  }

  @Test
  void shouldConvertDeepLinkToWebLink() {
    //@formatter:off
    toWebLink("ty://?Page=Product&ContentId=1925865&CampaignId=439892&MerchantId=105064", "https://www.trendyol.com/brand/name-p-1925865?boutiqueId=439892&merchantId=105064", HttpStatus.OK);
    toWebLink("ty://?Page=Product&ContentId=1925865", "https://www.trendyol.com/brand/name-p-1925865", HttpStatus.OK);
    toWebLink("ty://?Page=Product&ContentId=1925865&CampaignId=439892", "https://www.trendyol.com/brand/name-p-1925865?boutiqueId=439892", HttpStatus.OK);
    toWebLink("ty://?Page=Product&ContentId=1925865&MerchantId=105064", "https://www.trendyol.com/brand/name-p-1925865?merchantId=105064", HttpStatus.OK);
    toWebLink("ty://?Page=Search&Query=elbise", "https://www.trendyol.com/sr?q=elbise", HttpStatus.OK);
    toWebLink("ty://?Page=Search&Query=%C3%BCt%C3%BC", "https://www.trendyol.com/sr?q=%C3%BCt%C3%BC", HttpStatus.OK);
    toWebLink("ty://?Page=Favorites", "https://www.trendyol.com", HttpStatus.OK);
    toWebLink("ty://?Page=Orders", "https://www.trendyol.com", HttpStatus.OK);
    toWebLink("xz://?Page=Orders", "https://www.trendyol.com", HttpStatus.UNPROCESSABLE_ENTITY);
    toWebLink("invalid", "invalid", HttpStatus.UNPROCESSABLE_ENTITY);
    //@formatter:on
  }

  @SneakyThrows
  private void toWebLink(String source, String target, HttpStatus httpStatus) {
    // given
    var request = new LinkConvertRequest(source);

    //when
    ResponseEntity<Object> response = testRestTemplate.exchange(
        "/api/v1/link-converter/deeplink-to-weblink",
        HttpMethod.POST,
        new HttpEntity<>(request),
        linkConversionResponseType);

    //then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(httpStatus);
    assertThat(response.getBody()).isNotNull();

    if (!httpStatus.is2xxSuccessful()) {
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody()).extracting("errorMessage").isNotNull();
      assertThat(response.getBody()).extracting("errorCode").isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(httpStatus);
      return;
    }

    assertThat(response.getBody()).isNotNull();
    var conversionResponse = response.getBody();
    assertThat(conversionResponse).extracting("url").isEqualTo(target);
  }


  @SneakyThrows
  private void toDeepLink(String source, String target, HttpStatus httpStatus) {
    // given
    var request = new LinkConvertRequest(source);

    //when
    ResponseEntity<?> response = testRestTemplate.exchange(
        "/api/v1/link-converter/weblink-to-deeplink",
        HttpMethod.POST,
        new HttpEntity<>(request), linkConversionResponseType);

    //then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(httpStatus);
    assertThat(response.getBody()).isNotNull();

    if (!httpStatus.is2xxSuccessful()) {
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody()).extracting("errorMessage").isNotNull();
      assertThat(response.getBody()).extracting("errorCode").isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(httpStatus);
      return;
    }

    assertThat(response.getBody()).isNotNull();
    var conversionResponse = response.getBody();
    assertThat(conversionResponse).extracting("url").isEqualTo(target);
  }
}