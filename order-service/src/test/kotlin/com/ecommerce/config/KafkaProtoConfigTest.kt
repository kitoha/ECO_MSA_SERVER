package com.ecommerce.config

import com.ecommerce.kafka.ProtobufSerializer
import com.ecommerce.proto.order.OrderCreatedEvent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer

class KafkaProtoConfigTest : BehaviorSpec({

    given("KafkaProtoConfig") {
        val config = KafkaProtoConfig()
        val bootstrapServers = "localhost:9092"

        `when`("creating ProducerFactory for Protobuf") {
            val producerFactory = config.protoProducerFactory(bootstrapServers)

            then("should be configured correctly") {
                producerFactory shouldNotBe null

                val configs = producerFactory.configurationProperties
                configs[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] shouldBe bootstrapServers
                configs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] shouldBe StringSerializer::class.java
                configs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] shouldBe ProtobufSerializer::class.java
                configs[ProducerConfig.ACKS_CONFIG] shouldBe "all"
                configs[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] shouldBe true
                configs[ProducerConfig.RETRIES_CONFIG] shouldBe 3
            }
        }

        `when`("creating KafkaTemplate for Protobuf") {
            val producerFactory = config.protoProducerFactory(bootstrapServers)
            val kafkaTemplate = config.protoKafkaTemplate(producerFactory)

            then("should be configured correctly") {
                kafkaTemplate shouldNotBe null
                kafkaTemplate.shouldBeInstanceOf<org.springframework.kafka.core.KafkaTemplate<String, OrderCreatedEvent>>()
            }
        }
    }
})
