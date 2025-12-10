package com.example.board.service;

import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.board.dto.CommentDto;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글 서비스
 * 댓글 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * 특정 게시글의 모든 댓글 조회 (최상위 댓글만)
     * 대댓글은 각 댓글의 children 필드를 통해 접근
     * @param postId 게시글 ID
     * @return 최상위 댓글 목록
     */
    public List<Comment> findTopLevelCommentsByPostId(Long postId) {
        return commentRepository.findTopLevelCommentsByPostId(postId);
    }

    /**
     * 특정 게시글의 모든 댓글 조회 (계층 구조 포함)
     * @param postId 게시글 ID
     * @return 모든 댓글 목록 (시간순)
     */
    public List<Comment> findAllCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    /**
     * 댓글 상세 조회
     * @param commentId 댓글 ID
     * @return 댓글 정보
     * @throws IllegalArgumentException 댓글이 존재하지 않는 경우
     */
    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. ID: " + commentId));
    }

    /**
     * 댓글 작성 (최상위 댓글)
     * @param postId 게시글 ID
     * @param dto 댓글 데이터
     * @return 저장된 댓글
     * @throws IllegalArgumentException 게시글이 존재하지 않는 경우
     */
    @Transactional
    public Comment createComment(Long postId, CommentDto dto) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + postId));

        // 댓글 생성
        Comment comment = Comment.builder()
                .content(dto.getContent())
                .author(dto.getAuthor())
                .post(post)
                .parent(null) // 최상위 댓글
                .build();

        return commentRepository.save(comment);
    }

    /**
     * 대댓글 작성
     * @param postId 게시글 ID
     * @param parentId 부모 댓글 ID
     * @param dto 댓글 데이터
     * @return 저장된 대댓글
     * @throws IllegalArgumentException 게시글 또는 부모 댓글이 존재하지 않는 경우
     */
    @Transactional
    public Comment createReply(Long postId, Long parentId, CommentDto dto) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + postId));

        // 부모 댓글 존재 여부 확인
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. ID: " + parentId));

        // 부모 댓글이 같은 게시글에 속하는지 확인
        if (!parentComment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("댓글과 게시글이 일치하지 않습니다.");
        }

        // 대댓글 생성
        Comment reply = Comment.builder()
                .content(dto.getContent())
                .author(dto.getAuthor())
                .post(post)
                .parent(parentComment)
                .build();

        Comment savedReply = commentRepository.save(reply);

        // 부모 댓글의 children 목록에 추가
        parentComment.addChild(savedReply);

        return savedReply;
    }

    /**
     * 댓글 수정
     * @param commentId 댓글 ID
     * @param dto 수정할 댓글 데이터
     * @return 수정된 댓글
     * @throws IllegalArgumentException 댓글이 존재하지 않는 경우
     */
    @Transactional
    public Comment updateComment(Long commentId, CommentDto dto) {
        Comment comment = findCommentById(commentId);

        // 댓글 내용 수정
        comment.updateContent(dto.getContent());

        return comment; // JPA 더티 체킹으로 자동 업데이트
    }

    /**
     * 댓글 삭제
     * 대댓글이 있는 경우 함께 삭제됨 (CASCADE)
     * @param commentId 댓글 ID
     * @throws IllegalArgumentException 댓글이 존재하지 않는 경우
     */
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = findCommentById(commentId);

        // 부모 댓글이 있는 경우 부모의 children 목록에서 제거
        if (comment.getParent() != null) {
            comment.getParent().getChildren().remove(comment);
        }

        commentRepository.delete(comment);
        // @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)로 인해
        // 하위 댓글들도 자동으로 삭제됨
    }

    /**
     * 특정 게시글의 댓글 개수 조회
     * @param postId 게시글 ID
     * @return 댓글 개수 (대댓글 포함)
     */
    public Long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    /**
     * 특정 작성자의 댓글 목록 조회
     * @param author 작성자명
     * @return 해당 작성자의 댓글 목록
     */
    public List<Comment> findCommentsByAuthor(String author) {
        return commentRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    /**
     * 댓글 작성 권한 확인 (추후 확장 가능)
     * 현재는 단순히 게시글 존재 여부만 확인
     * @param postId 게시글 ID
     * @return 작성 가능 여부
     */
    public boolean canWriteComment(Long postId) {
        return postRepository.existsById(postId);
    }

    /**
     * 댓글 수정/삭제 권한 확인 (추후 확장 가능)
     * 현재는 단순히 댓글 존재 여부만 확인
     * 실제 서비스에서는 작성자 본인 확인 등의 로직 추가 필요
     * @param commentId 댓글 ID
     * @param author 요청자 (현재는 미사용)
     * @return 수정/삭제 가능 여부
     */
    public boolean canModifyComment(Long commentId, String author) {
        return commentRepository.existsById(commentId);
        // 실제 구현 시:
        // Comment comment = findCommentById(commentId);
        // return comment.getAuthor().equals(author);
    }
}
