package com.example.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 엔티티
 * 게시글에 대한 댓글과 대댓글을 관리하는 JPA 엔티티 클래스
 */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 500, message = "댓글은 500자 이하로 작성해주세요")
    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, length = 50)
    private String author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 게시글과의 다대일 관계
     * 하나의 게시글에 여러 댓글이 달릴 수 있음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * 부모 댓글과의 다대일 관계 (대댓글 구조)
     * null이면 최상위 댓글, 값이 있으면 대댓글
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /**
     * 자식 댓글들과의 일대다 관계 (대댓글 목록)
     * 이 댓글에 달린 대댓글들
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Builder
    public Comment(String content, String author, Post post, Comment parent) {
        this.content = content;
        this.author = author;
        this.post = post;
        this.parent = parent;
    }

    /**
     * 댓글 내용 수정
     * @param content 수정할 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 대댓글인지 확인
     * @return 부모 댓글이 있으면 true, 없으면 false
     */
    public boolean isReply() {
        return this.parent != null;
    }

    /**
     * 최상위 댓글인지 확인
     * @return 부모 댓글이 없으면 true, 있으면 false
     */
    public boolean isTopLevel() {
        return this.parent == null;
    }

    /**
     * 자식 댓글 추가
     * @param child 추가할 자식 댓글
     */
    public void addChild(Comment child) {
        this.children.add(child);
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
