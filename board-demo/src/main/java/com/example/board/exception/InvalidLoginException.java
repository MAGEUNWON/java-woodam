package com.example.board.exception;

/**
 * 로그인 실패 예외
 * 잘못된 사용자명 또는 비밀번호로 로그인 시도 시 발생하는 예외
 */
public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException(String message) {
        super(message);
    }

    public InvalidLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
