package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원가입 요청 DTO
 * 회원가입 폼에서 전달되는 데이터를 담는 클래스
 */
@Getter
@Setter
@NoArgsConstructor
public class SignupRequestDto {

  @NotBlank(message = "아이디는 필수입니다.")
  @Size(min = 3, max = 20, message = "아이디는 3자 이상 20자 이하여야 합니다.")
  private String username;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 6, max = 50, message = "비밀번호는 6자 이상 50자 이하여야 합니다.")
  private String password;

  @NotBlank(message = "비밀번호 확인은 필수입니다.")
  private String passwordConfirm;

  @NotBlank(message = "이름은 필수입니다.")
  @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하여야 합니다.")
  private String name;
}
