package com.ecommerce.inventory.config

import com.ecommerce.kafka.ProtobufDeserializer
import com.ecommerce.kafka.ProtobufSerializer
import com.ecommerce.proto.order.OrderCancelledEvent
import com.ecommerce.proto.order.OrderConfirmedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaProtoConfig {

    @Bean
    fun orderConfirmedEventConsumerFactory(
        @Value("\${spring.kafka.bootstrap-servers}") bootstrapServers: String
    ): ConsumerFactory<String, OrderConfirmedEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ProtobufDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "inventory-service",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            ProtobufDeserializer(OrderConfirmedEvent::class.java)
        )
    }

    @Bean
    fun orderConfirmedEventListenerContainerFactory(
        orderConfirmedEventConsumerFactory: ConsumerFactory<String, OrderConfirmedEvent>
    ): ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent>()
        factory.consumerFactory = orderConfirmedEventConsumerFactory
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        return factory
    }

    @Bean
    fun orderCancelledEventConsumerFactory(
        @Value("\${spring.kafka.bootstrap-servers}") bootstrapServers: String
    ): ConsumerFactory<String, OrderCancelledEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ProtobufDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "inventory-service",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            ProtobufDeserializer(OrderCancelledEvent::class.java)
        )
    }

    @Bean
    fun orderCancelledEventListenerContainerFactory(
        orderCancelledEventConsumerFactory: ConsumerFactory<String, OrderCancelledEvent>
    ): ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent>()
        factory.consumerFactory = orderCancelledEventConsumerFactory
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        return factory
    }

    @Bean
    fun reservationEventProducerFactory(
        @Value("\${spring.kafka.bootstrap-servers}") bootstrapServers: String
    ): ProducerFactory<String, com.google.protobuf.Message> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ProtobufSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true
        )
        return DefaultKafkaProducerFactory(props, StringSerializer(), ProtobufSerializer())
    }

    @Bean
    fun reservationEventKafkaTemplate(
        reservationEventProducerFactory: ProducerFactory<String, com.google.protobuf.Message>
    ): KafkaTemplate<String, com.google.protobuf.Message> {
        return KafkaTemplate(reservationEventProducerFactory)
    }
}
