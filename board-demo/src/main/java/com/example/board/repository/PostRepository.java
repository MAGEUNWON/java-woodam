package com.example.board.repository;

import com.example.board.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시글 Repository
 * 게시글 데이터 접근을 위한 JPA Repository 인터페이스
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 모든 게시글을 생성일시 내림차순으로 조회
     * @return 게시글 목록 (최신순)
     */
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllByOrderByCreatedAtDesc();

    /**
     * 제목으로 게시글 검색 (부분 일치, 대소문자 무시)
     * @param title 검색할 제목
     * @return 검색된 게시글 목록
     */
    List<Post> findByTitleContainingIgnoreCase(String title);

    /**
     * 작성자로 게시글 검색
     * @param author 작성자명
     * @return 해당 작성자의 게시글 목록
     */
    List<Post> findByAuthor(String author);
}









