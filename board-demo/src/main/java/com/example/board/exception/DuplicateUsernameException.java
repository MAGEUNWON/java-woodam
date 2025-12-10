package com.example.board.exception;

/**
 * 중복된 사용자명 예외
 * 회원가입 시 이미 존재하는 사용자명으로 가입을 시도할 때 발생하는 예외
 */
public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String message) {
        super(message);
    }

    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}
