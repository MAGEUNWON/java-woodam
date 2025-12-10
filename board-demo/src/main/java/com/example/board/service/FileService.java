package com.example.board.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 파일 업로드 서비스
 * 이미지 파일의 저장, 삭제를 처리하는 서비스 클래스
 */
@Service
@Slf4j
public class FileService {

  @Value("${file.upload-dir:uploads}")
  private String uploadDir;

  // 허용된 이미지 확장자
  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
      "jpg", "jpeg", "png", "gif", "webp");

  // 최대 파일 크기 (10MB)
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  /**
   * 이미지 파일 저장
   * 
   * @param file   업로드된 파일
   * @param subDir 하위 디렉토리 (예: "posts")
   * @return 저장된 파일의 상대 경로
   * @throws IOException              파일 저장 실패 시
   * @throws IllegalArgumentException 유효하지 않은 파일인 경우
   */
  public String saveImage(MultipartFile file, String subDir) throws IOException {
    // 빈 파일 체크
    if (file == null || file.isEmpty()) {
      return null;
    }

    // 파일 크기 체크
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
    }

    // 파일 확장자 체크
    String originalFilename = file.getOriginalFilename();
    String extension = getFileExtension(originalFilename);
    if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
      throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
    }

    // 저장 경로 생성 (년/월 구조)
    String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
    Path uploadPath = Paths.get(uploadDir, subDir, datePath);

    // 디렉토리 생성
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
      log.info("디렉토리 생성: {}", uploadPath);
    }

    // 파일명 생성 (UUID + 확장자)
    String newFilename = UUID.randomUUID().toString() + "." + extension;
    Path filePath = uploadPath.resolve(newFilename);

    // 파일 저장
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    log.info("파일 저장 완료: {}", filePath);

    // 상대 경로 반환 (웹에서 접근할 경로)
    return "/" + subDir + "/" + datePath + "/" + newFilename;
  }

  /**
   * 이미지 파일 삭제
   * 
   * @param imagePath 이미지 경로
   */
  public void deleteImage(String imagePath) {
    if (imagePath == null || imagePath.isEmpty()) {
      return;
    }

    try {
      // 상대 경로에서 실제 파일 경로 생성
      String relativePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
      Path filePath = Paths.get(uploadDir).resolve(relativePath.replace("/", java.io.File.separator));

      if (Files.exists(filePath)) {
        Files.delete(filePath);
        log.info("파일 삭제 완료: {}", filePath);
      }
    } catch (IOException e) {
      log.error("파일 삭제 실패: {}", imagePath, e);
    }
  }

  /**
   * 파일 확장자 추출
   * 
   * @param filename 파일명
   * @return 확장자
   */
  private String getFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf(".") + 1);
  }

  /**
   * 이미지 파일인지 확인
   * 
   * @param file 파일
   * @return 이미지 파일 여부
   */
  public boolean isImageFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return false;
    }
    String extension = getFileExtension(file.getOriginalFilename());
    return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
  }
}
