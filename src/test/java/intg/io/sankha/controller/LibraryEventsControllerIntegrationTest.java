package io.sankha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sankha.domain.LibraryEvent;
import io.sankha.util.TestUtil;
import java.util.HashMap;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-events")
@TestPropertySource(
    properties = {
      "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
    })
class LibraryEventsControllerIntegrationTest {

  @Autowired TestRestTemplate restTemplate;

  @Autowired EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired ObjectMapper objectMapper;

  private Consumer<Integer, String> consumer;

  @BeforeEach
  void setUp() {
    var configs =
        new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
    configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    consumer =
        new DefaultKafkaConsumerFactory<>(
                configs, new IntegerDeserializer(), new StringDeserializer())
            .createConsumer();
    embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
  }

  @AfterEach
  void tearDown() {
    consumer.close();
  }

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

    ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);

    // assert consumerRecords.count() == 1;

    consumerRecords.forEach(
        record -> {
          var libraryEventActual = TestUtil.parseLibraryEventRecord(objectMapper, record.value());
          System.out.println("libraryEventActual : " + libraryEventActual);
          Assertions.assertEquals(libraryEventActual, TestUtil.libraryEventRecord());
        });
  }
}
