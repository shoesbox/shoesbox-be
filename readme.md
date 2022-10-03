# ShoesBox Back-End

## 목차

[1. 기술 스택](#1-기술-스택)

[2. Git Branch 전략](#2-git-branch-전략)

[3. 코드 컨벤션](#3-코드-컨벤션)

---

## 1. 기술 스택

* Java 11
* Spring Boot
* NginX
* Redis
* AWS
  * EC2
  * S3
  * RDS (MySQL)
* JWT
* Thumbnailator

---

## 2. Git Branch 전략

### GitHub Flow 사용

![GitHub Flow](/img/github-flow.png)

### GitHub Flow를 선택한 이유

* 대안 중 하나였던 **Git Flow**는 팀이 처한 상황에 비해 지나치게 복잡하다고 판단했습니다.
  * *ShoesBox는 현재 서비스 중인 프로젝트도 아니고, 장기적으로 서비스하며 관리할 프로젝트가 아님.*
  * *hotfix, release 등의 브랜치가 필요할 만큼 상황이 급박하거나, 프로젝트가 거대하지 않음.*
* **GitHub Flow**는 단순하지만 **GitHub**의 장점(ex. PR 등) 대부분을 활용 가능하고, 브랜치 전략이 복잡해서 발생하는 ***프로젝트 오버헤드를 최소화***

### GitHub Flow

* main 브랜치에서 개발이 시작된다.
* 기능 구현이나 버그가 발생하면 issue를 작성한다.
* 팀원들이 issue 해결을 위해 main 브랜치에서 생성한 브랜치에서 개발을 하고 commit log를 작성한다.
  * 브랜치명은 목적이 명확하게 드러나도록 작성해야 한다.
  * ex) feature/{issue-number}-{feature-name}
* 정기적으로 원격 브랜치에 push한다.
  * 팀원들이 확인하기 쉽고, 로컬에 문제가 발생했을 때 되돌리기 쉽다.
* 도움, 피드백이 필요하거나 기능이 완성되면 pull request를 생성한다.
  * PR을 통해 팀원들 간의 피드백, 버그 찾기 등이 진행된다. ***release 브랜치가 없으므로 이 과정이 매우 중요하다.***
* main 브랜치에 생성된 PR은 Actions를 통해 자동으로 빌드 테스트가 수행된다.
* 모든 리뷰가 이루어지면, merge하기 전에 최종 테스트를 진행한다.
* 테스트까지 완료되면 main 브랜치에 merge 후 push 한다.
* 병합된 main 브랜치는 Actions를 통해 자동으로 빌드, 및 배포된다. (`AWS Code Deploy`)
* merge한 이후 PR을 요청한 브랜치는 즉시 삭제한다.
  * 작업이 완료되었음을 의미
  * 누군가 실수로 오래된 브랜치를 사용하는 것을 방지
  * *필요시 삭제한 브랜치의 복구도 가능*

### 커밋 메시지 컨벤션

코드 컨벤션 문서 참고

---

## [3. 코드 컨벤션](/convention.md)

## 4. 팀원들의 회고

(추가 예정)
