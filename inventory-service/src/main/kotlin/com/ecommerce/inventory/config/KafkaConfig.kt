package com.ecommerce.inventory.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Bean
    fun kafkaObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    @Bean
    fun producerFactory(objectMapper: ObjectMapper): ProducerFactory<String, Any> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 5,
            JsonSerializer.TYPE_MAPPINGS to "reservationCreated:com.ecommerce.inventory.event.ReservationCreatedEvent," +
                    "reservationCancelled:com.ecommerce.inventory.event.ReservationCancelledEvent," +
                    "reservationConfirmed:com.ecommerce.inventory.event.ReservationConfirmedEvent"
        )
        return DefaultKafkaProducerFactory(configProps, StringSerializer(), JsonSerializer<Any>(objectMapper))
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun consumerFactory(objectMapper: ObjectMapper): ConsumerFactory<String, Any> {
        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            // 보안: 신뢰할 수 있는 패키지만 명시적으로 지정 (RCE 방지)
            JsonDeserializer.TRUSTED_PACKAGES to "com.ecommerce.inventory.event",
            JsonDeserializer.TYPE_MAPPINGS to "reservationCreated:com.ecommerce.inventory.event.ReservationCreatedEvent," +
                    "reservationCancelled:com.ecommerce.inventory.event.ReservationCancelledEvent," +
                    "reservationConfirmed:com.ecommerce.inventory.event.ReservationConfirmedEvent"
        )
        return DefaultKafkaConsumerFactory(
            configProps,
            StringDeserializer(),
            JsonDeserializer(Any::class.java, objectMapper, false)
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, Any>
    ): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.setConcurrency(3)
        return factory
    }

    @Bean
    fun reservationCreatedTopic(): NewTopic {
        return TopicBuilder.name("reservation-created")
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun reservationCancelTopic(): NewTopic {
        return TopicBuilder.name("reservation-cancel")
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun reservationConfirmedTopic(): NewTopic {
        return TopicBuilder.name("reservation-confirmed")
            .partitions(3)
            .replicas(1)
            .build()
    }
}
