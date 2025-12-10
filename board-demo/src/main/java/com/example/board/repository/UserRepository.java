package com.example.board.repository;

import com.example.board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 회원 Repository
 * 회원 데이터 접근을 위한 JPA Repository 인터페이스
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * 사용자명으로 회원 조회 (중복 체크용)
   *
   * @param username 사용자명
   * @return 회원 정보 (Optional)
   */
  Optional<User> findByUsername(String username);

  /**
   * 사용자명과 이름으로 회원 조회 (비밀번호 찾기용)
   *
   * @param username 사용자명
   * @param name     이름
   * @return 회원 정보 (Optional)
   */
  Optional<User> findByUsernameAndName(String username, String name);
}
