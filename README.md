# 📝 프로젝트 소개 – 우담(Woodam)

**우담(Woodam)** 
사용자가 하루 동안 느낀 감정이나 경험을 기록할 수 있는 감정 기반 커뮤니티 서비스입니다.

기능적으로는 게시글·댓글 중심의 구조이지만,
프론트엔드는 오두막과 숲속 풍경을 테마로 한 픽셀 스타일 UI로 구성되어
사용자가 편안하게 글을 남길 수 있는 “작은 기록 공간”을 제공합니다.

현재는 겨울 테마가 적용되어 있지만,
계절이 바뀌면 분위기만 변경하며 서비스의 핵심 컨셉(작은 숲속 오두막 공간) 은 유지됩니다.

---

## 🎯 주요 특징

- **감정 중심 기록 서비스**  
  “오늘 하루는 어땠나요?“라는 흐름 속에서 글을 작성하는 간단한 커뮤니티 구조
- **계절 테마 UI**  
  HTML/CSS 픽셀 스타일로 구현된 숲속·오두막 배경  
  (향후 계절별 디자인 업데이트 가능)
- **Spring Boot 기반 REST API 구조**  
  회원가입/로그인, 게시글, 댓글 등 필수 기능 API 제공
- **필요 기능을 실제 서비스 형태로 모두 구현**  
  (댓글, 파일 업로드, 인증, 계층형 구조 등)

---

## 🛠️ 기술 스택

### Backend
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Spring Security + JWT
- Gradle

### Frontend
- HTML/CSS 픽셀 아트
- Thymeleaf 기반 정적 페이지

### Database
- PostgreSQL
- H2 (테스트용)

---

## 📁 프로젝트 구조 요약
```text
board-demo/

├── controller/     # REST API 컨트롤러
├── service/        # 비즈니스 로직
├── repository/     # DB 접근 계층
├── domain/         # 엔티티(JPA)
├── dto/            # 요청/응답 DTO
└── resources/
    └── application.yml
```

---

## 📚 학습한 내용 & 프로젝트 목적

이 프로젝트를 통해 단순 CRUD 학습을 넘어 실제 서비스 개발 환경과 동일한 구조를 경험했습니다.

### 🔧 백엔드 아키텍처 설계
- RESTful API 구조 설계
- Controller → Service → Repository 계층 구조 적용
- DTO와 Entity 분리, 응답 구조 확립

### 🗃️ 데이터 모델링 & JPA 활용
- 관계 기반 엔티티 설계 (Post, Comment, User 등)
- 연관관계 매핑 및 Cascade/Fetch 전략 이해
- 댓글/대댓글과 같은 계층형 데이터 구조 처리

### 🔐 인증 & 보안
- JWT 기반 로그인/인가 흐름 구현
- Spring Security 필터 체인 이해 및 적용

### 🐳 배포 & 운영
- Docker 기반 로컬 실행 환경 구성
- PostgreSQL + Spring Boot 컨테이너링

### 🎨 프론트/UI 연동
- HTML/CSS 픽셀 아트 컨셉 구현
- 애니메이션 요소(연기, 눈 등)와 백엔드 API 연동
- 간단한 SPA 느낌의 인터랙션 구현 경험

---

## 🚀 프로젝트 목표 요약

- CRUD를 넘어 실제 서비스 형태의 아키텍처를 직접 구축하는 것
- 인증, 댓글 구조, 파일 업로드 등 핵심 기능을 실제 구현 경험으로 전환
- 계층형 설계, 엔티티 모델링, API 명세 설계 등            
  백엔드 개발자로서 필수적인 실무 역량 강화
- 추후 계절·UI 업데이트를 고려한 확장 가능한 프론트 구조 실험

---

## 👤 Author

마근원 (Geunwon Ma)
- GitHub: https://github.com/MAGEUNWON
- Email: geunwon1947@gmail.com
- Notion Portfolio: https://mighty-print-cc5.notion.site/
