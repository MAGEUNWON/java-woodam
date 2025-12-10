package com.example.board.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정
 * 정적 리소스 핸들링 등을 설정하는 클래스
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${file.upload-dir:uploads}")
  private String uploadDir;

  /**
   * 정적 리소스 핸들러 설정
   * 업로드된 파일을 웹에서 접근할 수 있도록 매핑
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 업로드된 이미지 파일 서빙
    // /posts/** URL로 접근하면 uploads/posts/ 폴더의 파일을 서빙
    registry.addResourceHandler("/posts/images/**")
        .addResourceLocations("file:" + uploadDir + "/posts/");

    // 일반 업로드 파일 서빙
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir + "/");
  }
}
