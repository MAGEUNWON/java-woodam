package com.example.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈 컨트롤러
 * 메인 페이지 및 루트 경로를 처리하는 컨트롤러
 */
@Controller
public class HomeController {

  /**
   * 랜딩 페이지
   * GET /
   */
  @GetMapping("/")
  public String home() {
    return "home";
  }
}
