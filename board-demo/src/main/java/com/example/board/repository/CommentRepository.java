package com.example.board.repository;

import com.example.board.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글 Repository
 * 댓글 데이터 접근을 위한 JPA Repository 인터페이스
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글의 모든 댓글을 생성일시 순으로 조회
     * 최상위 댓글과 대댓글을 모두 포함하여 시간순으로 정렬
     * @param postId 게시글 ID
     * @return 댓글 목록 (시간순)
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdOrderByCreatedAtAsc(@Param("postId") Long postId);

    /**
     * 특정 게시글의 최상위 댓글만 조회 (부모가 없는 댓글)
     * 대댓글은 제외하고 최상위 댓글만 가져옴
     * @param postId 게시글 ID
     * @return 최상위 댓글 목록 (시간순)
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId);

    /**
     * 특정 부모 댓글의 대댓글들을 조회
     * @param parentId 부모 댓글 ID
     * @return 대댓글 목록 (시간순)
     */
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 특정 게시글의 댓글 개수 조회
     * @param postId 게시글 ID
     * @return 댓글 개수 (대댓글 포함)
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    /**
     * 특정 작성자의 댓글 목록 조회
     * @param author 작성자명
     * @return 해당 작성자의 댓글 목록
     */
    List<Comment> findByAuthorOrderByCreatedAtDesc(String author);

    /**
     * 특정 댓글과 그 하위 댓글들을 모두 삭제하기 위한 조회
     * 댓글 삭제 시 대댓글도 함께 삭제되어야 하므로 계층 구조를 고려
     * @param commentId 삭제할 댓글 ID
     * @return 삭제 대상 댓글들 (자신 + 모든 하위 댓글)
     */
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId OR c.parent.id = :commentId")
    List<Comment> findCommentWithReplies(@Param("commentId") Long commentId);
}
