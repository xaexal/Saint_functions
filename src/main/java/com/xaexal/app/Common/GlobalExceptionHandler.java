package com.xaexal.app.Common;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. DB 관련 예외 (DataAccessException) 처리
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDatabaseException(DataAccessException e) {
        Throwable rootCause = e.getRootCause();
        int errorCode = -1;
        String message = "데이터베이스 오류가 발생했습니다.";

        if (rootCause instanceof SQLException) {
            SQLException sqlEx = (SQLException) rootCause;
            errorCode = sqlEx.getErrorCode();

            // 특정 에러 코드별로 친절한 메시지 분기 처리 가능
            switch (errorCode) {
                case 1062: message = "이미 존재하는 데이터입니다. (중복 오류)"; break;
                case 1451:
                case 1833: message = "다른 데이터에서 사용 중인 정보라 변경/삭제할 수 없습니다."; break;
                case 1048: message = "필수 입력 항목이 누락되었습니다."; break;
                default: message = "DB 오류가 발생했습니다. (에러 코드: " + errorCode + ")";
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    // 2. 기타 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

}
/* JPA용으로 완전히 전환됐을 때 해제할 코드
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 엔티티를 찾지 못했을 때 (예: findById().orElseThrow() 호출 시)
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(jakarta.persistence.EntityNotFoundException e) {
        // 404 Not Found 응답
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // 2. DB 관련 예외 (DataAccessException) 처리
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDatabaseException(DataAccessException e) {
        Throwable rootCause = e.getRootCause();
        int errorCode = -1;
        String message = "데이터베이스 오류가 발생했습니다.";

        if (rootCause instanceof SQLException sqlEx) {
            errorCode = sqlEx.getErrorCode();
            message = switch (errorCode) {
                case 1062 -> "이미 존재하는 데이터입니다. (중복 오류)";
                case 1451, 1833 -> "다른 데이터에서 사용 중인 정보라 변경/삭제할 수 없습니다.";
                case 1048 -> "필수 입력 항목이 누락되었습니다.";
                default -> "DB 오류가 발생했습니다. (에러 코드: " + errorCode + ")";
            };
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    // 3. 기타 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
*/