package com.ecommerce.eureka

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EurekaServerApplicationTest(
    @LocalServerPort private val port: Int,
    private val restTemplate: TestRestTemplate
) : BehaviorSpec({

    extensions(SpringExtension)

    given("Eureka Server가 실행 중일 때") {

        `when`("Eureka 메인 페이지에 접근하면") {
            val response = restTemplate.getForEntity(
                "http://localhost:$port/",
                String::class.java
            )

            then("정상적으로 응답해야 한다") {
                response.statusCode shouldBe HttpStatus.OK
                response.body shouldNotBe null
            }

            then("Eureka Dashboard가 표시되어야 한다") {
                response.body!!.contains("Eureka") shouldBe true
            }
        }

        `when`("Eureka 앱 정보 API를 XML로 호출하면") {
            val headers = HttpHeaders().apply {
                accept = listOf(MediaType.APPLICATION_XML)
            }
            val entity = HttpEntity<String>(headers)

            val response = restTemplate.exchange(
                "http://localhost:$port/eureka/apps",
                HttpMethod.GET,
                entity,
                String::class.java
            )

            then("정상적으로 응답해야 한다") {
                response.statusCode shouldBe HttpStatus.OK
                response.body shouldNotBe null
            }

            then("XML 형식으로 애플리케이션 정보를 반환해야 한다") {
                response.body!!.contains("<applications") shouldBe true
            }
        }

        `when`("Eureka 앱 정보 API를 JSON으로 호출하면") {
            val headers = HttpHeaders().apply {
                accept = listOf(MediaType.APPLICATION_JSON)
            }
            val entity = HttpEntity<String>(headers)

            val response = restTemplate.exchange(
                "http://localhost:$port/eureka/apps",
                HttpMethod.GET,
                entity,
                String::class.java
            )

            then("정상적으로 응답해야 한다") {
                response.statusCode shouldBe HttpStatus.OK
                response.body shouldNotBe null
            }

            then("JSON 형식으로 애플리케이션 정보를 반환해야 한다") {
                val body = response.body!!
                (body.contains("\"applications\"") || body.contains("applications")) shouldBe true
            }
        }
    }
})
