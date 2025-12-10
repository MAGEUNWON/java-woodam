package com.example.board.controller;

import com.example.board.domain.Comment;
import com.example.board.dto.CommentDto;
import com.example.board.security.CustomUserDetails;
import com.example.board.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 댓글 컨트롤러
 * 댓글 관련 웹 요청을 처리하는 컨트롤러 클래스
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CommentController {

  private final CommentService commentService;

  /**
   * 댓글 작성 처리 (최상위 댓글)
   * POST /posts/{postId}/comments
   * 인증된 사용자만 가능
   *
   * @param postId             게시글 ID
   * @param commentDto         댓글 데이터
   * @param bindingResult      유효성 검사 결과
   * @param userDetails        인증된 사용자 정보
   * @param redirectAttributes 리다이렉트 시 전달할 메시지
   * @return 게시글 상세 페이지로 리다이렉트
   */
  @PostMapping("/posts/{postId}/comments")
  public String createComment(@PathVariable Long postId,
      @Valid @ModelAttribute CommentDto commentDto,
      BindingResult bindingResult,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    // 인증되지 않은 사용자 차단
    if (userDetails == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
      return "redirect:/login";
    }

    // 유효성 검사 실패 시
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("commentError", "댓글 내용을 올바르게 입력해주세요.");
      return "redirect:/posts/" + postId;
    }

    try {
      // 댓글 생성
      Comment savedComment = commentService.createComment(postId, commentDto);
      redirectAttributes.addFlashAttribute("successMessage", "댓글이 성공적으로 작성되었습니다.");

    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

    } catch (Exception e) {
      log.error("댓글 작성 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "댓글 작성 중 오류가 발생했습니다.");
    }

    return "redirect:/posts/" + postId;
  }

  /**
   * 대댓글 작성 처리
   * POST /posts/{postId}/comments/{parentId}/reply
   * 인증된 사용자만 가능
   *
   * @param postId             게시글 ID
   * @param parentId           부모 댓글 ID
   * @param commentDto         댓글 데이터
   * @param bindingResult      유효성 검사 결과
   * @param userDetails        인증된 사용자 정보
   * @param redirectAttributes 리다이렉트 시 전달할 메시지
   * @return 게시글 상세 페이지로 리다이렉트
   */
  @PostMapping("/posts/{postId}/comments/{parentId}/reply")
  public String createReply(@PathVariable Long postId,
      @PathVariable Long parentId,
      @Valid @ModelAttribute CommentDto commentDto,
      BindingResult bindingResult,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    // 인증되지 않은 사용자 차단
    if (userDetails == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
      return "redirect:/login";
    }

    // 유효성 검사 실패 시
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("replyError", "대댓글 내용을 올바르게 입력해주세요.");
      return "redirect:/posts/" + postId;
    }

    try {
      // 대댓글 생성
      Comment savedReply = commentService.createReply(postId, parentId, commentDto);
      redirectAttributes.addFlashAttribute("successMessage", "대댓글이 성공적으로 작성되었습니다.");

    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

    } catch (Exception e) {
      log.error("대댓글 작성 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "대댓글 작성 중 오류가 발생했습니다.");
    }

    return "redirect:/posts/" + postId;
  }

  /**
   * 댓글 수정 폼 페이지
   * GET /comments/{id}/edit
   * 인증된 사용자 + 본인 댓글만 수정 가능
   *
   * @param id                 댓글 ID
   * @param userDetails        인증된 사용자 정보
   * @param model              뷰에 전달할 데이터
   * @param redirectAttributes 리다이렉트 시 전달할 메시지
   * @return 댓글 수정 페이지 또는 오류 시 게시글 목록으로 리다이렉트
   */
  @GetMapping("/comments/{id}/edit")
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
      Comment comment = commentService.findCommentById(id);

      // 본인 댓글인지 확인
      if (!comment.getAuthor().equals(userDetails.getName())) {
        redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 댓글만 수정할 수 있습니다.");
        return "redirect:/posts/" + comment.getPost().getId();
      }

      // CommentDto로 변환하여 폼에 전달
      CommentDto commentDto = CommentDto.builder()
          .id(comment.getId())
          .content(comment.getContent())
          .author(comment.getAuthor())
          .postId(comment.getPost().getId())
          .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
          .build();

      model.addAttribute("comment", comment);
      model.addAttribute("commentDto", commentDto);
      return "comment-edit"; // 댓글 수정 전용 페이지

    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/posts";
    }
  }

  /**
   * 댓글 수정 처리
   * POST /comments/{id}/edit
   * 인증된 사용자 + 본인 댓글만 수정 가능
   *
   * @param id                 댓글 ID
   * @param commentDto         수정할 댓글 데이터
   * @param bindingResult      유효성 검사 결과
   * @param userDetails        인증된 사용자 정보
   * @param redirectAttributes 리다이렉트 시 전달할 메시지
   * @return 게시글 상세 페이지로 리다이렉트
   */
  @PostMapping("/comments/{id}/edit")
  public String updateComment(@PathVariable Long id,
      @Valid @ModelAttribute CommentDto commentDto,
      BindingResult bindingResult,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    // 인증되지 않은 사용자 차단
    if (userDetails == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
      return "redirect:/login";
    }

    Long postId = null;

    try {
      // 기존 댓글 조회하여 postId 획득
      Comment existingComment = commentService.findCommentById(id);
      postId = existingComment.getPost().getId();

      // 본인 댓글인지 확인
      if (!existingComment.getAuthor().equals(userDetails.getName())) {
        redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 댓글만 수정할 수 있습니다.");
        return "redirect:/posts/" + postId;
      }

      // 유효성 검사 실패 시
      if (bindingResult.hasErrors()) {
        redirectAttributes.addFlashAttribute("errorMessage", "댓글 내용을 올바르게 입력해주세요.");
        return "redirect:/posts/" + postId;
      }

      // 댓글 수정
      Comment updatedComment = commentService.updateComment(id, commentDto);
      redirectAttributes.addFlashAttribute("successMessage", "댓글이 성공적으로 수정되었습니다.");

    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

    } catch (Exception e) {
      log.error("댓글 수정 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "댓글 수정 중 오류가 발생했습니다.");
    }

    // postId가 null인 경우 게시글 목록으로 리다이렉트
    return "redirect:/posts/" + (postId != null ? postId : "");
  }

  /**
   * 댓글 삭제 처리
   * POST /comments/{id}/delete
   * 인증된 사용자 + 본인 댓글만 삭제 가능
   *
   * @param id                 댓글 ID
   * @param userDetails        인증된 사용자 정보
   * @param redirectAttributes 리다이렉트 시 전달할 메시지
   * @return 게시글 상세 페이지로 리다이렉트
   */
  @PostMapping("/comments/{id}/delete")
  public String deleteComment(@PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      RedirectAttributes redirectAttributes) {

    // 인증되지 않은 사용자 차단
    if (userDetails == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
      return "redirect:/login";
    }

    Long postId = null;

    try {
      // 삭제 전 댓글 조회
      Comment comment = commentService.findCommentById(id);
      postId = comment.getPost().getId();

      // 본인 댓글인지 확인
      if (!comment.getAuthor().equals(userDetails.getName())) {
        redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 댓글만 삭제할 수 있습니다.");
        return "redirect:/posts/" + postId;
      }

      // 댓글 삭제
      commentService.deleteComment(id);
      redirectAttributes.addFlashAttribute("successMessage", "댓글이 성공적으로 삭제되었습니다.");

    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

    } catch (Exception e) {
      log.error("댓글 삭제 실패", e);
      redirectAttributes.addFlashAttribute("errorMessage", "댓글 삭제 중 오류가 발생했습니다.");
    }

    // postId가 null인 경우 게시글 목록으로 리다이렉트
    return "redirect:/posts/" + (postId != null ? postId : "");
  }

  /**
   * 특정 게시글의 댓글 목록 조회 (AJAX용 - 추후 확장 가능)
   * GET /posts/{postId}/comments
   *
   * @param postId 게시글 ID
   * @param model  뷰에 전달할 데이터
   * @return 댓글 목록 JSON 또는 부분 템플릿
   */
  @GetMapping("/posts/{postId}/comments")
  @ResponseBody
  public String getComments(@PathVariable Long postId, Model model) {
    try {
      var comments = commentService.findTopLevelCommentsByPostId(postId);
      // JSON 형태로 반환하거나 부분 템플릿 렌더링
      // 현재는 단순 문자열 반환 (추후 JSON 변환 로직 추가 가능)
      return "댓글 " + comments.size() + "개";

    } catch (Exception e) {
      return "댓글 조회 오류";
    }
  }
}
