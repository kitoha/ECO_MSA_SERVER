package com.ecommerce.cart

import com.ecommerce.cart.client.ProductClient
import com.ecommerce.cart.generator.TsidGenerator
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {
    @Bean
    fun tsidGenerator(): TsidGenerator {
        return TsidGenerator()
    }

    @Bean
    fun productClient(): ProductClient {
        return mockk<ProductClient>()
    }
}
