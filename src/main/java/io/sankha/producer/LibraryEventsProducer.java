package io.sankha.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sankha.domain.LibraryEvent;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsProducer {

  @Value("${spring.kafka.topic}")
  private String topic;

  private final KafkaTemplate<Integer, String> kafkaTemplate;

  private final ObjectMapper objectMapper;

  public CompletableFuture<SendResult<Integer, String>> sendLibraryEventAsync(LibraryEvent libraryEvent)
      throws JsonProcessingException {
    var key = libraryEvent.libraryEventId();
    var value = objectMapper.writeValueAsString(libraryEvent);
    var completableFuture = kafkaTemplate.send(topic, key, value);

    return completableFuture.whenComplete(
        (sendResult, throwable) -> {
          if (null != throwable) {
            handleFailure(key, value, throwable);
          } else {
            handleSuccess(key, value, sendResult);
          }
        });
  }

  public CompletableFuture<SendResult<Integer, String>> sendLibraryEventAsyncProducerRecord(LibraryEvent libraryEvent)
          throws JsonProcessingException {
    var key = libraryEvent.libraryEventId();
    var value = objectMapper.writeValueAsString(libraryEvent);

    var producerRecord = buildProducerRecord(key, value);

    var completableFuture = kafkaTemplate.send(producerRecord);

    return completableFuture.whenComplete(
            (sendResult, throwable) -> {
              if (null != throwable) {
                handleFailure(key, value, throwable);
              } else {
                handleSuccess(key, value, sendResult);
              }
            });
  }

  private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {

    List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
    return new ProducerRecord<>(topic, null, key, value, recordHeaders);
  }

  public SendResult<Integer, String> sendLibraryEventSync(LibraryEvent libraryEvent)
      throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
    var key = libraryEvent.libraryEventId();
    var value = objectMapper.writeValueAsString(libraryEvent);
    var sendResult = kafkaTemplate.send(topic, key, value)
            //.get();
                    .get(3, TimeUnit.SECONDS);
    handleSuccess(key, value, sendResult);
    return sendResult;
  }

  private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
    log.info(
        "Message sent successfully for the key: {} and value: {}, partition: {} ",
        key,
        value,
        sendResult.getRecordMetadata().partition());
  }

  private void handleFailure(Integer key, String value, Throwable ex) {
    log.error("Error sending the message and exceptionis {} ", ex.getMessage(), ex);
  }
}
