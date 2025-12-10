package com.example.board.service;

import com.example.board.domain.User;
import com.example.board.dto.SignupRequestDto;
import com.example.board.exception.DuplicateUsernameException;
import com.example.board.exception.InvalidLoginException;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 서비스
 * 회원 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원가입 처리
   *
   * @param dto 회원가입 요청 정보
   * @return 저장된 회원 정보
   * @throws DuplicateUsernameException 사용자명이 중복된 경우
   */
  @Transactional
  public User registerUser(SignupRequestDto dto) {
    // 1. 사용자명 중복 체크
    if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
      throw new DuplicateUsernameException("이미 존재하는 사용자명입니다: " + dto.getUsername());
    }

    // 2. 비밀번호 암호화 (BCrypt)
    String encodedPassword = passwordEncoder.encode(dto.getPassword());

    // 3. DTO -> Entity 변환
    User user = User.builder()
        .username(dto.getUsername())
        .password(encodedPassword)
        .name(dto.getName())
        .build();

    // 4. 회원 정보 저장
    return userRepository.save(user);
  }

  /**
   * 사용자명으로 회원 조회
   *
   * @param username 사용자명
   * @return 회원 정보 (Optional)
   */
  public boolean existsByUsername(String username) {
    return userRepository.findByUsername(username).isPresent();
  }

  /**
   * 로그인 검증
   *
   * @param username 사용자명
   * @param password 비밀번호 (평문)
   * @return 로그인 성공 시 해당 User 객체 반환
   * @throws InvalidLoginException 사용자명이 존재하지 않거나 비밀번호가 일치하지 않는 경우
   */
  public User login(String username, String password) {
    // 1. 사용자명으로 회원 조회
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new InvalidLoginException("아이디 또는 비밀번호가 올바르지 않습니다."));

    // 2. 비밀번호 일치 여부 확인 (BCrypt 검증)
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new InvalidLoginException("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    // 3. 로그인 성공 - User 객체 반환
    return user;
  }

  /**
   * 비밀번호 재설정
   *
   * @param username    사용자명
   * @param name        이름 (본인 확인용)
   * @param newPassword 새 비밀번호 (평문)
   * @return 비밀번호 변경된 User 객체
   * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
   */
  @Transactional
  public User resetPassword(String username, String name, String newPassword) {
    // 1. 사용자명과 이름으로 회원 조회
    User user = userRepository.findByUsernameAndName(username, name)
        .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));

    // 2. 비밀번호 암호화 후 변경 (BCrypt)
    String encodedPassword = passwordEncoder.encode(newPassword);
    user.changePassword(encodedPassword);

    // 3. 변경된 User 반환 (더티 체킹으로 자동 저장)
    return user;
  }

  /**
   * 이름 변경
   *
   * @param userId  사용자 ID
   * @param newName 새 이름
   * @return 이름 변경된 User 객체
   * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
   */
  @Transactional
  public User updateName(Long userId, String newName) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

    user.changeName(newName);
    return user;
  }
}
