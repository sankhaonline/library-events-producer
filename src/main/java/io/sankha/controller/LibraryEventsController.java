package io.sankha.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sankha.domain.LibraryEvent;
import io.sankha.producer.LibraryEventsProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsController {

  private final LibraryEventsProducer eventsProducer;

  @PostMapping("/v1/libraryevent")
  public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent)
      throws JsonProcessingException {
    log.info("libraryEvent : {}", libraryEvent);
    // invoke the Kafka producer
    //eventsProducer.sendLibraryEventAsync(libraryEvent);
    //eventsProducer.sendLibraryEventSync(libraryEvent);
    eventsProducer.sendLibraryEventAsyncProducerRecord(libraryEvent);
    return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
  }
}
