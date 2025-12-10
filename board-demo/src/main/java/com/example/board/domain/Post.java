package com.example.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글 엔티티
 * 게시판의 게시글 정보를 담는 JPA 엔티티 클래스
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false, length = 50)
  private String author;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "image_path", length = 500)
  private String imagePath;

  @Builder
  public Post(String title, String content, String author, String imagePath) {
    this.title = title;
    this.content = content;
    this.author = author;
    this.imagePath = imagePath;
  }

  /**
   * 게시글 수정
   *
   * @param title   수정할 제목
   * @param content 수정할 내용
   */
  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }

  /**
   * 게시글 수정 (이미지 포함)
   *
   * @param title     수정할 제목
   * @param content   수정할 내용
   * @param imagePath 수정할 이미지 경로
   */
  public void updateWithImage(String title, String content, String imagePath) {
    this.title = title;
    this.content = content;
    this.imagePath = imagePath;
  }

  /**
   * 이미지 경로 설정
   *
   * @param imagePath 이미지 경로
   */
  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
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
}
