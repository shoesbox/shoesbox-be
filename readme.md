# ShoesBox Back-End

## 목차

[1. 기술 스택 소개](#1-기술-스택-소개)

[2. Git Branch 전략](#2-git-branch-전략)

[3. 코드 컨벤션](#3-코드-컨벤션)

---

## 1. 기술 스택 소개

### 1-1. Java 11

* Java 8과 함께 가장 많이 사용되는 버전.
* GC의 성능 향상, 타입 추론 키워드 `var`의 추가 등 소소한 이점이 많음.

### 1-2. Spring Boot 2.7.3

* 프로젝트 의존성
  * Gradle
  * Spring Web
  * Spring Data Jpa
  * Spring Security
  * Spring Validation
  * Lombok
  * H2 Database
  * Java JWT
  * Apache Commons Codec

### 1-3. AWS EC2, S3, RDS

* 각각 서버 인스턴스, 이미지 파일 서버, DB 서버로 사용함.

---

## 2. Git Branch 전략

### GitHub Flow 사용

![GitHub Flow](/img/github-flow.png)

### GitHub Flow를 선택한 이유

* Git Flow는 팀이 처한 상황에 비해 지나치게 복잡하다고 판단함.
  * ShoesBox는 현재 서비스 중인 프로젝트도 아니고, 장기적으로 서비스하며 관리할 프로젝드도 아님.
  * hotfix, release 등의 브랜치가 필요할 만큼 상황이 급박하거나, 프로젝트가 거대하지 않음.
* GitLab Flow는 GitHub Flow랑 유사하지만, 개념적으로는 Git Flow에 더 가까운 전략
* GitHub Flow는 단순하지만 GitHub의 장점(ex. PR 등) 대부분을 활용 가능하고, 브랜치 전략이 복잡해서 발생하는 ***프로젝트 오버헤드를 최소화***

### GitHub Flow 정책

* main 브랜치에서 개발이 시작된다.
* 기능 구현이나 버그가 발생하면 issue를 작성한다.
* 팀원들이 issue 해결을 위해 main 브랜치에서 생성한 브랜치에서 개발을 하고 commit log를 작성한다.
  * 브랜치명은 명확하게 작성해야 한다.
  * ex) feature/{feature-number}-{feature-name}
* 정기적으로 원격 브랜치에 push한다.
  * 팀원들이 확인하기 쉽고, 로컬에 문제가 발생했을 때 되돌리기 쉽다.
* 도움, 피드백이 필요하거나 기능이 완성되면 pull request를 생성한다.
  * PR을 통해 팀원들 간의 피드백, 버그 찾기 등이 진행된다. release 브랜치가 없으므로 이 과정이 매우 중요하다.
* 모든 리뷰가 이루어지면, merge하기 전에 배포를 통해 최종 테스트를 진행한다.
* 테스트까지 완료되면 main 브랜치에 merge 후 push 한다.
* 병합된 main 브랜치는 즉시 배포되어야 한다. (GitHub Actions를 활용한 CI/CD구현)
* merge한 이후 PR을 요청한 브랜치는 즉시 삭제한다.
  * 작업이 완료되었음을 의미
  * 누군가 실수로 오래된 브랜치를 사용하는 것을 방지
  * 필요시 삭제한 브랜치의 복구도 가능

### 커밋 메시지 컨벤션

코드 컨벤션 문서 참고

---

## [3. 코드 컨벤션](/convention.md)

## 4. 팀원들의 회고

(추가 예정)
