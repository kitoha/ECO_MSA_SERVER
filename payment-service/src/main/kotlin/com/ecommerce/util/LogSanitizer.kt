package com.ecommerce.util

/**
 * Log Injection 공격을 방지하기 위한 로그 데이터 정제 유틸리티
 *
 * OWASP Log Injection 취약점 대응:
 * - 개행 문자(\n, \r) 제거
 * - CRLF Injection 방지
 * - 로그 포맷 문자 이스케이프
 */
object LogSanitizer {

    fun sanitize(input: String?, maxLength: Int = 200): String {
        if (input.isNullOrBlank()) {
            return ""
        }

        return input
            .replace("\r", "")
            .replace("\n", "")
            .replace("\t", " ")
            .replace("\u0000", "")
            .replace(Regex("\\s+"), " ")
            .take(maxLength)
            .trim()
    }

    fun sanitizeParams(params: Map<String, Any?>): String {
        return params.entries.joinToString(", ") { (key, value) ->
            "$key=${sanitize(value?.toString())}"
        }
    }
}

fun String?.sanitizeForLog(maxLength: Int = 200): String {
    return LogSanitizer.sanitize(this, maxLength)
}

fun Any?.sanitizeForLog(maxLength: Int = 200): String {
    return LogSanitizer.sanitize(this?.toString(), maxLength)
}
