package com.ecommerce.kafka

import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import org.apache.kafka.common.serialization.Deserializer

class ProtobufDeserializer<T : MessageLite>(
    private val messageClass: Class<T>
) : Deserializer<T> {

    private val parser: Parser<T> by lazy {
        @Suppress("UNCHECKED_CAST")
        messageClass.getDeclaredMethod("parser").invoke(null) as Parser<T>
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        // No configuration needed
    }

    override fun deserialize(topic: String?, data: ByteArray?): T? {
        if (data == null || data.isEmpty()) {
            return null
        }

        return parser.parseFrom(data)
    }

    override fun close() {
        // No resources to close
    }
}
