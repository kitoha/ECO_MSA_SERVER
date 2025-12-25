package com.ecommerce.util

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class LogSanitizerTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  given("LogSanitizer가 주어졌을 때") {

    `when`("null 입력이 들어오면") {
      then("빈 문자열을 반환해야 한다") {
        LogSanitizer.sanitize(null) shouldBe ""
      }
    }

    `when`("빈 문자열이나 공백만 있는 입력이 들어오면") {
      then("빈 문자열을 반환해야 한다") {
        LogSanitizer.sanitize("") shouldBe ""
        LogSanitizer.sanitize("   ") shouldBe ""
      }
    }

    `when`("캐리지 리턴(\\r)이 포함된 문자열이 들어오면") {
      then("캐리지 리턴을 제거해야 한다") {
        LogSanitizer.sanitize("Hello\rWorld") shouldBe "HelloWorld"
      }
    }

    `when`("개행 문자(\\n)가 포함된 문자열이 들어오면") {
      then("개행 문자를 제거해야 한다") {
        LogSanitizer.sanitize("Hello\nWorld") shouldBe "HelloWorld"
      }
    }

    `when`("CRLF(\\r\\n)가 포함된 문자열이 들어오면") {
      then("CRLF를 제거해야 한다") {
        LogSanitizer.sanitize("Hello\r\nWorld") shouldBe "HelloWorld"
      }
    }

    `when`("탭(\\t)이 포함된 문자열이 들어오면") {
      then("탭을 공백으로 치환해야 한다") {
        LogSanitizer.sanitize("Hello\tWorld") shouldBe "Hello World"
      }
    }

    `when`("null 바이트(\\u0000)가 포함된 문자열이 들어오면") {
      then("null 바이트를 제거해야 한다") {
        LogSanitizer.sanitize("Hello\u0000World") shouldBe "HelloWorld"
      }
    }

    `when`("여러 개의 공백이 연속으로 있는 문자열이 들어오면") {
      then("단일 공백으로 축약해야 한다") {
        LogSanitizer.sanitize("Hello    World") shouldBe "Hello World"
        LogSanitizer.sanitize("Hello  \t  World") shouldBe "Hello World"
      }
    }

    `when`("앞뒤 공백이 있는 문자열이 들어오면") {
      then("앞뒤 공백을 제거해야 한다") {
        LogSanitizer.sanitize("  Hello World  ") shouldBe "Hello World"
      }
    }

    `when`("기본 최대 길이(200자)를 초과하는 문자열이 들어오면") {
      then("200자로 잘라야 한다") {
        val longString = "a".repeat(300)
        val sanitized = LogSanitizer.sanitize(longString)
        sanitized.length shouldBe 200
      }
    }

    `when`("커스텀 최대 길이를 지정하고 초과하는 문자열이 들어오면") {
      then("지정된 길이로 잘라야 한다") {
        val longString = "a".repeat(100)
        val sanitized = LogSanitizer.sanitize(longString, maxLength = 50)
        sanitized.length shouldBe 50
      }
    }

    `when`("로그 인젝션 공격이 시도되면") {
      val maliciousInput = "user\nINFO: Fake log entry"
      val sanitized = LogSanitizer.sanitize(maliciousInput)

      then("개행 문자가 제거되어야 한다") {
        sanitized.contains("\n") shouldBe false
      }

      then("원래 내용은 포함되어야 한다") {
        sanitized.contains("user") shouldBe true
        sanitized.contains("Fake log entry") shouldBe true
      }
    }

    `when`("CRLF 인젝션 공격이 시도되면") {
      val maliciousInput = "innocent\r\nERROR: Injected error log"
      val sanitized = LogSanitizer.sanitize(maliciousInput)

      then("CRLF가 제거되어야 한다") {
        sanitized.contains("\r") shouldBe false
        sanitized.contains("\n") shouldBe false
      }

      then("원래 내용은 포함되어야 한다") {
        sanitized.contains("innocent") shouldBe true
        sanitized.contains("Injected error log") shouldBe true
      }
    }

    `when`("복합적인 로그 인젝션 공격이 시도되면") {
      val maliciousInput = "user:admin\n\rSTATUS:authorized\t\u0000LEVEL:root"
      val sanitized = LogSanitizer.sanitize(maliciousInput)

      then("모든 위험한 문자가 제거되어야 한다") {
        sanitized.contains("\n") shouldBe false
        sanitized.contains("\r") shouldBe false
        sanitized.contains("\u0000") shouldBe false
      }

      then("원래 내용은 포함되어야 한다") {
        sanitized.contains("user:admin") shouldBe true
        sanitized.contains("STATUS:authorized") shouldBe true
        sanitized.contains("LEVEL:root") shouldBe true
      }
    }
  }

  given("LogSanitizer.sanitizeParams가 주어졌을 때") {

    `when`("맵 파라미터를 전달하면") {
      val params = mapOf(
        "userId" to "user123",
        "orderId" to "ORDER-001",
        "amount" to 10000
      )

      then("적절한 형식으로 변환해야 한다") {
        val result = LogSanitizer.sanitizeParams(params)
        result shouldBe "userId=user123, orderId=ORDER-001, amount=10000"
      }
    }

    `when`("맵에 null 값이 포함되어 있으면") {
      val params = mapOf(
        "userId" to "user123",
        "orderId" to null,
        "amount" to 10000
      )

      then("null을 빈 문자열로 처리해야 한다") {
        val result = LogSanitizer.sanitizeParams(params)
        result shouldBe "userId=user123, orderId=, amount=10000"
      }
    }

    `when`("맵 값에 악의적인 입력이 포함되어 있으면") {
      val params = mapOf(
        "userId" to "admin\nINFO: fake log",
        "orderId" to "ORDER\r\n001"
      )
      val result = LogSanitizer.sanitizeParams(params)

      then("위험한 문자가 제거되어야 한다") {
        result.contains("\n") shouldBe false
        result.contains("\r") shouldBe false
      }

      then("원래 내용은 포함되어야 한다") {
        result.contains("admin") shouldBe true
        result.contains("ORDER") shouldBe true
        result.contains("001") shouldBe true
      }
    }
  }

  given("String.sanitizeForLog 확장 함수가 주어졌을 때") {

    `when`("문자열에 적용하면") {
      then("문자열을 정제해야 한다") {
        "Hello\nWorld".sanitizeForLog() shouldBe "HelloWorld"
      }
    }

    `when`("null 문자열에 적용하면") {
      then("빈 문자열을 반환해야 한다") {
        val nullString: String? = null
        nullString.sanitizeForLog() shouldBe ""
      }
    }

    `when`("커스텀 최대 길이를 지정하면") {
      then("지정된 길이로 잘라야 한다") {
        val longString = "a".repeat(100)
        longString.sanitizeForLog(maxLength = 50).length shouldBe 50
      }
    }
  }

  given("Any.sanitizeForLog 확장 함수가 주어졌을 때") {

    `when`("객체에 적용하면") {
      then("객체를 문자열로 변환하고 정제해야 한다") {
        val obj = 12345
        obj.sanitizeForLog() shouldBe "12345"
      }
    }

    `when`("null 객체에 적용하면") {
      then("빈 문자열을 반환해야 한다") {
        val nullObj: Any? = null
        nullObj.sanitizeForLog() shouldBe ""
      }
    }

    `when`("toString()에 악의적인 내용이 있는 객체에 적용하면") {
      val obj = object {
        override fun toString() = "Value\nINFO: fake"
      }
      val sanitized = obj.sanitizeForLog()

      then("위험한 문자가 제거되어야 한다") {
        sanitized.contains("\n") shouldBe false
      }

      then("원래 내용은 포함되어야 한다") {
        sanitized.contains("Value") shouldBe true
        sanitized.contains("fake") shouldBe true
      }
    }

    `when`("커스텀 최대 길이를 지정하면") {
      then("지정된 길이로 잘라야 한다") {
        val longNumber = "1".repeat(100)
        longNumber.sanitizeForLog(maxLength = 30).length shouldBe 30
      }
    }
  }

  given("OWASP 로그 인젝션 방어가 주어졌을 때") {

    `when`("로그 위조 공격이 시도되면") {
      val userInput = "admin\n2025-12-26 10:00:00 INFO User logged in successfully"
      val sanitized = LogSanitizer.sanitize(userInput)

      then("개행 문자가 제거되어야 한다") {
        sanitized.contains("\n") shouldBe false
      }

      then("원래 내용은 포함되어야 한다") {
        sanitized.contains("admin") shouldBe true
        sanitized.contains("2025-12-26") shouldBe true
      }
    }

    `when`("로그 파일 중독 공격이 시도되면") {
      val userInput = "normaluser\r\n\r\nERROR: System compromised by normaluser"
      val sanitized = LogSanitizer.sanitize(userInput)

      then("CRLF 시퀀스가 제거되어야 한다") {
        sanitized.contains("\r") shouldBe false
        sanitized.contains("\n") shouldBe false
      }

      then("원래 내용은 포함되어야 한다") {
        sanitized.contains("normaluser") shouldBe true
        sanitized.contains("ERROR") shouldBe true
      }
    }

    `when`("모든 위험한 문자가 한 문자열에 포함되어 있으면") {
      val dangerous = "\r\n\t\u0000  multiple  spaces  "
      val sanitized = LogSanitizer.sanitize(dangerous)

      then("모든 위험한 문자가 제거/정규화되어야 한다") {
        sanitized.contains("\r") shouldBe false
        sanitized.contains("\n") shouldBe false
        sanitized.contains("\u0000") shouldBe false
      }

      then("원래 내용은 포함되어야 한다") {
        sanitized.contains("multiple") shouldBe true
        sanitized.contains("spaces") shouldBe true
      }
    }
  }
})
