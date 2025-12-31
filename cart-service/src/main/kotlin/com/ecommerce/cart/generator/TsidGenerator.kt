package com.ecommerce.cart.generator

import io.hypersistence.tsid.TSID
import org.springframework.stereotype.Component

@Component
class TsidGenerator : IdGenerator {

    override fun generate(): Long {
        return TSID.fast().toLong()
    }

    companion object {

        fun generate(): Long {
            return TSID.fast().toLong()
        }

        fun encode(id: Long): String {
            return TSID.from(id).toString()
        }

        fun decode(tsid: String): Long {
            return TSID.from(tsid).toLong()
        }
    }
}
