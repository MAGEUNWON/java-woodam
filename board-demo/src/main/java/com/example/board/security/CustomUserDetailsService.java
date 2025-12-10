package com.example.board.security;

import com.example.board.domain.User;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 구현체
 * 사용자명으로 사용자를 조회하여 인증에 사용할 UserDetails 객체를 반환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * 사용자명으로 UserDetails 조회
   * Spring Security가 로그인 시 자동으로 호출
   *
   * @param username 사용자명
   * @return UserDetails 객체
   * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("사용자 인증 시도: {}", username);

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> {
          log.warn("인증 실패 - 존재하지 않는 사용자: {}", username);
          return new UsernameNotFoundException("존재하지 않는 사용자입니다: " + username);
        });

    log.debug("사용자 인증 성공: {}", username);
    return new CustomUserDetails(user);
  }
}
