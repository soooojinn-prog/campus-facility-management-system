package io.github.wizwix.cfms.global.error;

/// 오류 전송용 레코드
///
/// @param status [Integer] [HTTP 상태 코드](https://developer.mozilla.org/ko/docs/Web/HTTP/Reference/Status)
/// @param error [String] 오류 메시지
/// @param message [String] 메시지
/// @param timestamp [Long] 오류 생성 시각
public record ResponseError(int status, String error, String message, long timestamp) {}
