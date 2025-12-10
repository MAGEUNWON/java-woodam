package com.example.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * 회원 정보를 담는 JPA 엔티티 클래스
 * 비밀번호는 BCrypt로 암호화되어 저장됩니다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String username;

  @Column(nullable = false, length = 100)
  private String password; // BCrypt 해시값 저장 (60자)

  @Column(nullable = false, length = 20)
  private String name;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public User(String username, String password, String name) {
    this.username = username;
    this.password = password;
    this.name = name;
  }

  /**
   * 엔티티 저장 전 실행 - 생성일시, 수정일시 설정
   */
  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  /**
   * 엔티티 수정 전 실행 - 수정일시 업데이트
   */
  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 비밀번호 변경
   *
   * @param newPassword 새 비밀번호
   */
  public void changePassword(String newPassword) {
    this.password = newPassword;
  }

  /**
   * 이름 변경
   *
   * @param newName 새 이름
   */
  public void changeName(String newName) {
    this.name = newName;
  }
}
