package com.example.board.controller;

import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.board.dto.CommentDto;
import com.example.board.security.CustomUserDetails;
import com.example.board.service.CommentService;
import com.example.board.service.FileService;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * 게시글 컨트롤러
 * 게시판 관련 웹 요청을 처리하는 컨트롤러 클래스
 */
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

  private final PostService postService;
  private final CommentService commentService;
  private final FileService fileService;

  /**
   * 게시글 목록 페이지
   * GET /posts
   */
  @GetMapping
  public String list(Model model) {
    List<Post> posts = postService.findAllPosts();
    model.addAttribute("posts", posts);
    return "list";
  }

  /**
   * 게시글 상세 페이지
   * GET /posts/{id}
   */
  @GetMapping("/{id}")
  public String detail(@PathVariable Long id,
      Model model,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    try {
      Post post = postService.findPostById(id);
      List<Comment> comments = commentService.findTopLevelCommentsByPostId(id);
      Long commentCount = commentService.getCommentCount(id);

      model.addAttribute("post", post);
      model.addAttribute("comments", comments);
      model.addAttribute("commentCount", commentCount);
      model.addAttribute("commentDto", new CommentDto()); // 댓글 작성 폼용

      // 로그인한 사용자 정보를 모델에 추가
      if (userDetails != null) {
        String currentUserName = userDetails.getName();
        model.addAttribute("defaultAuthor", currentUserName);
        model.addAttribute("currentUserName", currentUserName);
      }

      return "detail";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "redirect:/posts";
    }
  }

  /**
   * 게시글 작성 폼 페이지
   * GET /posts/write
   * 인증된 사용자만 접근 가능 (SecurityConfig에서 설정)
   */
  @GetMapping("/write")
  public String writeForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    // 로그인한 사용자 정보를 모델에 추가 (작성자 기본값용)
    if (userDetails != null) {
      model.addAttribute("defaultAuthor", userDetails.getName());
    }
    return "write";
  }

  /**
   * 게시글 작성 처리
   * POST /posts/write
   * 인증된 사용자만 접근 가능 (SecurityConfig에서 설정)
   */
  @PostMapping("/write")
  public String write(@RequestParam String title,
      @RequestParam String content,
      @RequestParam String author,
      @RequestParam(required = false) MultipartFile image,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    // 인증되지 않은 사용자 차단
    if (userDetails == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
      return "redirect:/login";
    }

    try {
      // 이미지 업로드 처리
      String imagePath = null;
      if (image != null && !image.isEmpty()) {
        imagePath = fileService.saveImage(image, "posts");
        log.info("이미지 업로드 완료: {}", imagePath);
      }

      Post savedPost = postService.createPostWithImage(title, content, author, imagePath);
      redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 작성되었습니다.");
      return "redirect:/posts/" + savedPost.getId();
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/posts/write";
    } catch (IOException e) {
      log.error("이미지 업로드 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다.");
      return "redirect:/posts/write";
    } catch (Exception e) {
      log.error("게시글 작성 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "게시글 작성 중 오류가 발생했습니다.");
      return "redirect:/posts/write";
    }
  }

  /**
   * 게시글 수정 폼 페이지
   * GET /posts/{id}/edit
   * 인증된 사용자 + 본인 게시글만 수정 가능
   */
  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      Model model,
      RedirectAttributes redirectAttributes) {

    // 인증되지 않은 사용자 차단
    if (userDetails == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
      return "redirect:/login";
    }

    try {
      Post post = postService.findPostById(id);

      // 본인 게시글인지 확인
      if (!post.getAuthor().equals(userDetails.getName())) {
        redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 게시글만 수정할 수 있습니다.");
        return "redirect:/posts/" + id;
      }

      model.addAttribute("post", post);
      return "edit";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/posts";
    }
  }

  /**
   * 게시글 수정 처리
   * POST /posts/{id}/edit
   * 인증된 사용자 + 본인 게시글만 수정 가능
   */
  @PostMapping("/{id}/edit")
  public String edit(@PathVariable Long id,
      @RequestParam String title,
      @RequestParam String content,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam(required = false) String deleteImage,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    // 인증되지 않은 사용자 차단
    if (userDetails == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
      return "redirect:/login";
    }

    try {
      Post post = postService.findPostById(id);

      // 본인 게시글인지 확인
      if (!post.getAuthor().equals(userDetails.getName())) {
        redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 게시글만 수정할 수 있습니다.");
        return "redirect:/posts/" + id;
      }

      String imagePath = post.getImagePath();

      // 이미지 삭제 요청 처리
      if ("true".equals(deleteImage) && imagePath != null) {
        fileService.deleteImage(imagePath);
        imagePath = null;
        log.info("이미지 삭제 완료");
      }

      // 새 이미지 업로드 처리
      if (image != null && !image.isEmpty()) {
        // 기존 이미지 삭제
        if (post.getImagePath() != null) {
          fileService.deleteImage(post.getImagePath());
        }
        imagePath = fileService.saveImage(image, "posts");
        log.info("새 이미지 업로드 완료: {}", imagePath);
      }

      postService.updatePostWithImage(id, title, content, imagePath);
      redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 수정되었습니다.");
      return "redirect:/posts/" + id;
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/posts";
    } catch (IOException e) {
      log.error("이미지 처리 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "이미지 처리 중 오류가 발생했습니다.");
      return "redirect:/posts/" + id + "/edit";
    } catch (Exception e) {
      log.error("게시글 수정 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정 중 오류가 발생했습니다.");
      return "redirect:/posts/" + id + "/edit";
    }
  }

  /**
   * 게시글 삭제 처리
   * POST /posts/{id}/delete
   * 인증된 사용자 + 본인 게시글만 삭제 가능
   */
  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    // 인증되지 않은 사용자 차단
    if (userDetails == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
      return "redirect:/login";
    }

    try {
      Post post = postService.findPostById(id);

      // 본인 게시글인지 확인
      if (!post.getAuthor().equals(userDetails.getName())) {
        redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 게시글만 삭제할 수 있습니다.");
        return "redirect:/posts/" + id;
      }

      // 게시글 이미지 삭제
      if (post.getImagePath() != null) {
        fileService.deleteImage(post.getImagePath());
        log.info("게시글 이미지 삭제: {}", post.getImagePath());
      }

      postService.deletePost(id);
      redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 삭제되었습니다.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("게시글 삭제 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "게시글 삭제 중 오류가 발생했습니다.");
    }
    return "redirect:/posts";
  }
}
