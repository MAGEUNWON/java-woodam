package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 프로필 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ProfileUpdateRequestDto {

  @NotBlank(message = "이름은 필수입니다.")
  @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하여야 합니다.")
  private String name;
}
