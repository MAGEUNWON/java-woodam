package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 댓글 데이터 전송 객체 (DTO)
 * 클라이언트와 서버 간 댓글 데이터 전송을 위한 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    /**
     * 댓글 ID (수정 시 사용)
     */
    private Long id;

    /**
     * 댓글 내용
     * 필수 입력, 최대 500자 제한
     */
    @NotBlank(message = "댓글 내용을 입력해주세요")
    @Size(max = 500, message = "댓글은 500자 이하로 작성해주세요")
    private String content;

    /**
     * 작성자명
     * 필수 입력, 최대 50자 제한
     */
    @NotBlank(message = "작성자명을 입력해주세요")
    @Size(max = 50, message = "작성자명은 50자 이하로 입력해주세요")
    private String author;

    /**
     * 게시글 ID
     * 댓글이 속한 게시글을 식별
     */
    private Long postId;

    /**
     * 부모 댓글 ID
     * 대댓글인 경우에만 값이 있음 (null이면 최상위 댓글)
     */
    private Long parentId;

    /**
     * 최상위 댓글 생성을 위한 생성자
     * @param content 댓글 내용
     * @param author 작성자명
     * @param postId 게시글 ID
     */
    public CommentDto(String content, String author, Long postId) {
        this.content = content;
        this.author = author;
        this.postId = postId;
        this.parentId = null; // 최상위 댓글
    }

    /**
     * 대댓글 생성을 위한 생성자
     * @param content 댓글 내용
     * @param author 작성자명
     * @param postId 게시글 ID
     * @param parentId 부모 댓글 ID
     */
    public CommentDto(String content, String author, Long postId, Long parentId) {
        this.content = content;
        this.author = author;
        this.postId = postId;
        this.parentId = parentId;
    }

    /**
     * 대댓글인지 확인
     * @return 부모 댓글 ID가 있으면 true, 없으면 false
     */
    public boolean isReply() {
        return this.parentId != null;
    }

    /**
     * 최상위 댓글인지 확인
     * @return 부모 댓글 ID가 없으면 true, 있으면 false
     */
    public boolean isTopLevel() {
        return this.parentId == null;
    }
}
