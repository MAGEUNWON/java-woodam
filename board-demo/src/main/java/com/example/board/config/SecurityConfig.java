package com.example.board.config;

import com.example.board.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security 설정
 * 인증/인가 및 로그인/로그아웃 설정을 담당
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;

  /**
   * 비밀번호 암호화에 사용할 PasswordEncoder Bean
   * BCrypt 알고리즘 사용 (단방향 해시 + Salt)
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * AuthenticationManager Bean
   * 프로그래밍 방식의 인증 처리에 사용
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  /**
   * Spring Security 필터 체인 설정
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 보호 활성화 (Thymeleaf form에서 자동으로 토큰 추가됨)
        .csrf(csrf -> csrf
            // 정적 리소스와 이미지 업로드 경로는 CSRF 제외
            .ignoringRequestMatchers("/uploads/**", "/posts/images/**"))

        // URL별 접근 권한 설정
        .authorizeHttpRequests(auth -> auth
            // 정적 리소스 - 모든 사용자 접근 허용
            .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/posts/images/**").permitAll()

            // 인증 관련 페이지 - 모든 사용자 접근 허용
            .requestMatchers("/", "/login", "/signup", "/password-reset").permitAll()

            // 게시글 목록 및 상세 - 모든 사용자 접근 허용
            .requestMatchers("/posts", "/posts/{id}").permitAll()

            // 게시글 작성/수정/삭제 - 인증된 사용자만 접근 가능
            .requestMatchers("/posts/write", "/posts/*/edit", "/posts/*/delete").authenticated()

            // 댓글 관련 - 인증된 사용자만 접근 가능
            .requestMatchers("/posts/*/comments/**").authenticated()

            // 마이페이지 - 인증된 사용자만 접근 가능
            .requestMatchers("/mypage/**").authenticated()

            // 그 외 모든 요청은 인증 필요
            .anyRequest().authenticated())

        // 로그인 설정
        .formLogin(form -> form
            // 커스텀 로그인 페이지 사용
            .loginPage("/login")
            // 로그인 처리 URL (POST 요청)
            .loginProcessingUrl("/login")
            // 로그인 폼의 username 파라미터명
            .usernameParameter("username")
            // 로그인 폼의 password 파라미터명
            .passwordParameter("password")
            // 로그인 성공 시 이동할 URL
            .defaultSuccessUrl("/posts", true)
            // 로그인 실패 시 이동할 URL
            .failureUrl("/login?error=true")
            // 로그인 페이지는 모든 사용자 접근 허용
            .permitAll())

        // 로그아웃 설정
        .logout(logout -> logout
            // 로그아웃 요청 URL
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            // 로그아웃 성공 시 이동할 URL
            .logoutSuccessUrl("/posts")
            // 세션 무효화
            .invalidateHttpSession(true)
            // 쿠키 삭제
            .deleteCookies("JSESSIONID")
            // 로그아웃은 모든 사용자 접근 허용
            .permitAll())

        // 세션 관리
        .sessionManagement(session -> session
            // 세션 고정 공격 방지 - 로그인 시 새 세션 ID 발급
            .sessionFixation().changeSessionId()
            // 최대 동시 세션 수 (1개만 허용)
            .maximumSessions(1)
            // 이전 세션 만료 (새 로그인 시 기존 세션 종료)
            .expiredUrl("/login?expired=true"))

        // UserDetailsService 설정
        .userDetailsService(userDetailsService);

    return http.build();
  }
}
