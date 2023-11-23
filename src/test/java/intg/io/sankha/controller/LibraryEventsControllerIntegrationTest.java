package io.sankha.controller;

import static org.junit.jupiter.api.Assertions.*;

import io.sankha.domain.LibraryEvent;
import io.sankha.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryEventsControllerIntegrationTest {

  @Autowired TestRestTemplate restTemplate;

  @Test
  void postLibraryEvent() {
    // given
    var httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    var httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(), httpHeaders);

    // when
    var responseEntity =
        restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, httpEntity, LibraryEvent.class);

    // then
    Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
  }
}
