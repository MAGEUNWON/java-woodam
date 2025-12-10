package com.example.board.controller;

import com.example.board.domain.User;
import com.example.board.dto.PasswordResetRequestDto;
import com.example.board.dto.ProfileUpdateRequestDto;
import com.example.board.dto.SignupRequestDto;
import com.example.board.exception.DuplicateUsernameException;
import com.example.board.security.CustomUserDetails;
import com.example.board.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 회원 컨트롤러
 * 회원가입, 로그인 페이지, 마이페이지 관련 웹 요청을 처리하는 컨트롤러
 *
 * 로그인/로그아웃 처리는 Spring Security가 담당
 * - POST /login: Spring Security의 formLogin에서 처리
 * - GET/POST /logout: Spring Security의 logout에서 처리
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;

  // ========================================
  // 회원가입 관련 핸들러
  // ========================================

  /**
   * 회원가입 폼 페이지
   *
   * @param model 모델
   * @return 회원가입 템플릿
   */
  @GetMapping("/signup")
  public String signupForm(Model model) {
    model.addAttribute("signupRequestDto", new SignupRequestDto());
    return "user/signup";
  }

  /**
   * 회원가입 처리
   *
   * @param dto                회원가입 요청 정보
   * @param bindingResult      검증 결과
   * @param redirectAttributes 리다이렉트 시 전달할 속성
   * @return 성공 시 로그인 페이지로 리다이렉트, 실패 시 회원가입 폼
   */
  @PostMapping("/signup")
  public String signup(@Valid SignupRequestDto dto,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    // 1. 검증 에러가 있는 경우
    if (bindingResult.hasErrors()) {
      return "user/signup";
    }

    // 2. 비밀번호 확인 일치 여부 검증
    if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
      bindingResult.rejectValue("passwordConfirm", "password.mismatch", "비밀번호가 일치하지 않습니다.");
      return "user/signup";
    }

    try {
      // 3. 회원가입 처리
      userService.registerUser(dto);
      log.info("회원가입 성공: {}", dto.getUsername());

      // 4. 성공 메시지를 flash attribute로 전달
      redirectAttributes.addFlashAttribute("signupSuccess", true);
      redirectAttributes.addFlashAttribute("signupUsername", dto.getUsername());

      // 5. 성공 시 로그인 페이지로 리다이렉트 (로그인 유도)
      return "redirect:/login";

    } catch (DuplicateUsernameException e) {
      // 6. 중복 사용자명 에러 처리
      bindingResult.rejectValue("username", "duplicate.username", e.getMessage());
      log.warn("회원가입 실패 - 중복 사용자명: {}", dto.getUsername());
      return "user/signup";
    } catch (Exception e) {
      // 7. 기타 에러 처리
      bindingResult.reject("signup.error", "회원가입 중 오류가 발생했습니다.");
      log.error("회원가입 중 예상치 못한 오류 발생", e);
      return "user/signup";
    }
  }

  // ========================================
  // 로그인 페이지 핸들러
  // (로그인 처리는 Spring Security가 담당)
  // ========================================

  /**
   * 로그인 폼 페이지
   * 로그인 에러, 세션 만료 등의 메시지 처리
   *
   * @param error   로그인 실패 시 true
   * @param expired 세션 만료 시 true
   * @param model   모델
   * @return 로그인 템플릿
   */
  @GetMapping("/login")
  public String loginForm(@RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "expired", required = false) String expired,
      Model model,
      jakarta.servlet.http.HttpServletRequest request) {

    // 세션을 미리 생성 (CSRF 토큰 생성 전에 세션이 필요함)
    request.getSession(true);

    // 로그인 에러 메시지 처리
    // 세션 만료 메시지 처리
    if (expired != null) {
      model.addAttribute("expiredMessage", "세션이 만료되었습니다. 다시 로그인해주세요.");
    }

    return "user/login";
  }

  // ========================================
  // 비밀번호 찾기/변경 관련 핸들러
  // ========================================

  /**
   * 비밀번호 찾기 폼 페이지
   *
   * @param model 모델
   * @return 비밀번호 찾기 템플릿
   */
  @GetMapping("/password-reset")
  public String passwordResetForm(Model model) {
    model.addAttribute("passwordResetRequestDto", new PasswordResetRequestDto());
    return "user/password-reset";
  }

  /**
   * 비밀번호 재설정 처리
   *
   * @param dto                비밀번호 재설정 요청 정보
   * @param bindingResult      검증 결과
   * @param redirectAttributes 리다이렉트 시 전달할 속성
   * @return 성공 시 로그인 페이지로 리다이렉트, 실패 시 비밀번호 찾기 폼
   */
  @PostMapping("/password-reset")
  public String passwordReset(@Valid PasswordResetRequestDto dto,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    // 1. 검증 에러가 있는 경우
    if (bindingResult.hasErrors()) {
      return "user/password-reset";
    }

    // 2. 새 비밀번호 확인 일치 여부 검증
    if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
      bindingResult.rejectValue("newPasswordConfirm", "password.mismatch", "새 비밀번호가 일치하지 않습니다.");
      return "user/password-reset";
    }

    try {
      // 3. 비밀번호 재설정 처리
      userService.resetPassword(dto.getUsername(), dto.getName(), dto.getNewPassword());
      log.info("비밀번호 재설정 성공: {}", dto.getUsername());

      // 4. 성공 메시지를 flash attribute로 전달
      redirectAttributes.addFlashAttribute("passwordResetSuccess", true);

      // 5. 성공 시 로그인 페이지로 리다이렉트
      return "redirect:/login";

    } catch (IllegalArgumentException e) {
      // 6. 회원 정보 불일치 에러 처리
      bindingResult.reject("user.notFound", "일치하는 회원 정보를 찾을 수 없습니다.");
      log.warn("비밀번호 재설정 실패 - 회원 정보 불일치: username={}, name={}", dto.getUsername(), dto.getName());
      return "user/password-reset";
    } catch (Exception e) {
      // 7. 기타 에러 처리
      bindingResult.reject("passwordReset.error", "비밀번호 재설정 중 오류가 발생했습니다.");
      log.error("비밀번호 재설정 중 예상치 못한 오류 발생", e);
      return "user/password-reset";
    }
  }

  // ========================================
  // 마이페이지 관련 핸들러
  // ========================================

  /**
   * 마이페이지
   * Spring Security의 인증 정보를 사용하여 로그인 사용자 정보 조회
   *
   * @param userDetails 인증된 사용자 정보 (Spring Security가 주입)
   * @param model       모델
   * @return 마이페이지 템플릿
   */
  @GetMapping("/mypage")
  public String mypage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    if (userDetails == null) {
      return "redirect:/login";
    }

    User user = userDetails.getUser();

    ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto();
    dto.setName(user.getName());
    model.addAttribute("profileUpdateRequestDto", dto);
    model.addAttribute("user", user);
    return "user/mypage";
  }

  /**
   * 이름 수정 처리
   * Spring Security의 인증 정보를 갱신하여 변경된 이름 반영
   *
   * @param dto                프로필 수정 요청 정보
   * @param bindingResult      검증 결과
   * @param userDetails        인증된 사용자 정보
   * @param redirectAttributes 리다이렉트 시 전달할 속성
   * @param model              모델
   * @return 성공 시 마이페이지로 리다이렉트, 실패 시 마이페이지
   */
  @PostMapping("/mypage/update-name")
  public String updateName(@Valid ProfileUpdateRequestDto dto,
      BindingResult bindingResult,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (userDetails == null) {
      return "redirect:/login";
    }

    User currentUser = userDetails.getUser();

    // 1. 검증 에러가 있는 경우
    if (bindingResult.hasErrors()) {
      model.addAttribute("user", currentUser);
      model.addAttribute("nameError", true);
      return "user/mypage";
    }

    try {
      // 2. 이름 변경 처리
      User updatedUser = userService.updateName(currentUser.getId(), dto.getName());
      log.info("이름 수정 성공: {} -> 이름: {}", currentUser.getUsername(), dto.getName());

      // 3. Spring Security 인증 정보 갱신
      CustomUserDetails newUserDetails = new CustomUserDetails(updatedUser);
      Authentication newAuth = new UsernamePasswordAuthenticationToken(
          newUserDetails,
          null,
          newUserDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(newAuth);

      // 4. 성공 메시지 전달
      redirectAttributes.addFlashAttribute("nameUpdateSuccess", true);

      // 5. 마이페이지로 리다이렉트
      return "redirect:/mypage";

    } catch (IllegalArgumentException e) {
      model.addAttribute("user", currentUser);
      model.addAttribute("nameErrorMessage", e.getMessage());
      log.warn("이름 수정 실패: {}", e.getMessage());
      return "user/mypage";
    } catch (Exception e) {
      model.addAttribute("user", currentUser);
      model.addAttribute("nameErrorMessage", "이름 수정 중 오류가 발생했습니다.");
      log.error("이름 수정 중 예상치 못한 오류 발생", e);
      return "user/mypage";
    }
  }
}
