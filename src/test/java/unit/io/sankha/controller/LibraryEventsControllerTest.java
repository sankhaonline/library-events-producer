package io.sankha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sankha.domain.LibraryEvent;
import io.sankha.producer.LibraryEventsProducer;
import io.sankha.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

// Test slice
@WebMvcTest(LibraryEventsController.class)
class LibraryEventsControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean LibraryEventsProducer eventsProducer;

  @Test
  void postLibraryEvent() throws Exception {
    // given
    var json = objectMapper.writeValueAsString(TestUtil.libraryEventRecord());
    Mockito.when(
            eventsProducer.sendLibraryEventAsyncProducerRecord(
                ArgumentMatchers.isA(LibraryEvent.class)))
        .thenReturn(null);

    // when
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isCreated());
    // then
  }

  @Test
  void postLibraryEvent_4xx() throws Exception {
    // given
    var json = objectMapper.writeValueAsString(TestUtil.libraryEventRecordWithInvalidBook());
    // when
    Mockito.when(
            eventsProducer.sendLibraryEventAsyncProducerRecord(
                ArgumentMatchers.isA(LibraryEvent.class)))
        .thenReturn(null);

    var expectedErrorMessage = "book.bookId - must not be null";

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andExpect(MockMvcResultMatchers.content().string(expectedErrorMessage));
    // then
  }
}
