package com.example.board.service;

import com.example.board.domain.Post;
import com.example.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시글 서비스
 * 게시글 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

  private final PostRepository postRepository;

  /**
   * 모든 게시글 목록 조회 (최신순)
   * 
   * @return 게시글 목록
   */
  public List<Post> findAllPosts() {
    return postRepository.findAllByOrderByCreatedAtDesc();
  }

  /**
   * 게시글 상세 조회
   * 
   * @param id 게시글 ID
   * @return 게시글 정보
   * @throws IllegalArgumentException 게시글이 존재하지 않는 경우
   */
  public Post findPostById(Long id) {
    return postRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + id));
  }

  /**
   * 게시글 작성
   * 
   * @param title   제목
   * @param content 내용
   * @param author  작성자
   * @return 저장된 게시글
   */
  @Transactional
  public Post createPost(String title, String content, String author) {
    Post post = Post.builder()
        .title(title)
        .content(content)
        .author(author)
        .build();

    return postRepository.save(post);
  }

  /**
   * 게시글 작성 (이미지 포함)
   * 
   * @param title     제목
   * @param content   내용
   * @param author    작성자
   * @param imagePath 이미지 경로
   * @return 저장된 게시글
   */
  @Transactional
  public Post createPostWithImage(String title, String content, String author, String imagePath) {
    Post post = Post.builder()
        .title(title)
        .content(content)
        .author(author)
        .imagePath(imagePath)
        .build();

    return postRepository.save(post);
  }

  /**
   * 게시글 수정
   * 
   * @param id      게시글 ID
   * @param title   수정할 제목
   * @param content 수정할 내용
   * @return 수정된 게시글
   * @throws IllegalArgumentException 게시글이 존재하지 않는 경우
   */
  @Transactional
  public Post updatePost(Long id, String title, String content) {
    Post post = findPostById(id);
    post.update(title, content);
    return post;
  }

  /**
   * 게시글 수정 (이미지 포함)
   * 
   * @param id        게시글 ID
   * @param title     수정할 제목
   * @param content   수정할 내용
   * @param imagePath 수정할 이미지 경로
   * @return 수정된 게시글
   * @throws IllegalArgumentException 게시글이 존재하지 않는 경우
   */
  @Transactional
  public Post updatePostWithImage(Long id, String title, String content, String imagePath) {
    Post post = findPostById(id);
    post.updateWithImage(title, content, imagePath);
    return post;
  }

  /**
   * 게시글 삭제
   * 
   * @param id 게시글 ID
   * @throws IllegalArgumentException 게시글이 존재하지 않는 경우
   */
  @Transactional
  public void deletePost(Long id) {
    Post post = findPostById(id);
    postRepository.delete(post);
  }

  /**
   * 제목으로 게시글 검색
   * 
   * @param title 검색할 제목
   * @return 검색된 게시글 목록
   */
  public List<Post> searchPostsByTitle(String title) {
    return postRepository.findByTitleContainingIgnoreCase(title);
  }

  /**
   * 작성자로 게시글 검색
   * 
   * @param author 작성자명
   * @return 해당 작성자의 게시글 목록
   */
  public List<Post> findPostsByAuthor(String author) {
    return postRepository.findByAuthor(author);
  }
}
