package com.ecommerce.kafka

import com.google.protobuf.MessageLite
import org.apache.kafka.common.serialization.Serializer

class ProtobufSerializer<T : MessageLite> : Serializer<T> {

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        // No configuration needed
    }

    override fun serialize(topic: String?, data: T?): ByteArray? {
        return data?.toByteArray()
    }

    override fun close() {
        // No resources to close
    }
}
