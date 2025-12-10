package com.example.board.security;

import com.example.board.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails 구현체
 * User 엔티티를 Spring Security가 인식할 수 있는 형태로 감싸는 클래스
 */
@Getter
public class CustomUserDetails implements UserDetails {

  private final User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  /**
   * 사용자 권한 반환
   * 현재는 모든 사용자에게 ROLE_USER 권한 부여
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  /**
   * 계정 만료 여부 (true = 만료되지 않음)
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * 계정 잠금 여부 (true = 잠기지 않음)
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * 자격 증명(비밀번호) 만료 여부 (true = 만료되지 않음)
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * 계정 활성화 여부 (true = 활성화됨)
   */
  @Override
  public boolean isEnabled() {
    return true;
  }

  // ========================================
  // User 엔티티 정보 접근을 위한 편의 메서드
  // ========================================

  /**
   * 사용자 ID 반환
   */
  public Long getId() {
    return user.getId();
  }

  /**
   * 사용자 이름(닉네임) 반환
   */
  public String getName() {
    return user.getName();
  }
}
