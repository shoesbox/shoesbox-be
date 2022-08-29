# 코딩 컨벤션

[뒤로가기](/readme.md)

대부분의 서식(들여쓰기, 공백, 빈 줄 등)은 인텔리제이가 대신 해주기 때문에 참고용으로 사용한다.

## 목차

[1. Java](#1-java)

* [1-1. 소스파일 공통](#1-1-소스파일-공통)
* [1-2. 이름(Naming)](#1-2-이름naming)
* [1-3. 선언(Declaration)](#1-3-선언declaration)
* [1-4. 들여쓰기(Indentation)](#1-4-들여쓰기indentation)
* [1-5. 중괄호(Braces)](#1-5-중괄호braces)
* [1-6. 줄바꿈(Line-wrapping)](#1-6-줄바꿈line-wrapping)
* [1-7. 빈 줄(Blank lines)](#1-7-빈-줄blank-lines)
* [1-8. 주석(Comment)](#1-8-주석comment)
* [1-9. 기타(etc)](#1-9-기타etc)

[2. DataBase](#2-database)

[3. Package Directory Structure](#3-package-directory-structure)

[4. GitHub](#4-github)

## 1. Java

## 1-1. 소스파일 공통

* JAVA 파일 이름은 포함된 소스의 최상위 클래스의 이름과 .java 확장자로 구성한다.

* 모든 소스코드 인코딩은 UTF-8을 사용한다.

* import 문에서는 와일드카드(`ex. java.util.*` 처럼 아스테리스크로 하위 클래스를 다 적용하는 방식)를 사용하지 않는다.

[목차로 돌아가기](#목차)

## 1-2. 이름(Naming)

* 식별자에는 영문/숫자/언더스코어만 허용한다.
* 이름은 짧지만 의미 있어야 한다.
* 이름은 해당 변수나 클래스의 사용 의도를 알 수 있도록 명확한 의미를 지니고 있어야 한다.
* 무분별한 약어는 절대 금지한다.

```Java
// 좋은 예
int likeCount;
String profileImageUrl;
// 나쁜 예
int lcnt = 0;
int a = 0;
String pUrl;
```

* 이름을 지을 때 약어는 피해야 한다. 단, HTML, URL 등 원래의 단어보다 더 많이 사용되고 있는 경우는 예외로 둔다.

* 약어의 경우 첫 번째 글자만 대문자로 적는다.

```Java
// 좋은 예
public createImageUrl(String url) {}
// 나쁜 예
public createImageURL(String url) {}
```

* 패키지 이름은 소문자여야 한다.

```Java
// 좋은 예
package com.shoesbox.post
// 나쁜 예
package com.shoesbox.Post
```

* 클래스/인터페이스명에는 파스칼 표기법(Pascal Case)을 사용한다.
* (각 단어의 첫 글자를 대문자로 작성)

```Java
// 좋은 예
public class PostController
// 나쁜 예
public class postController
```

* 클래스 이름은 명사나 명사절을 사용한다.
* 인터페이스 이름은 명사, 명사절, 형용사, 형용사절을 사용한다.
* 테스트 클래스 이름은 'Test'로 끝난다.

```Java
// 좋은 예
public class CreatePostTest {}
```

* 메서드 이름은 카멜 표기법(Camel Case)를 사용한다.
* (각 단어의 첫 글자를 대문자로 작성하되, 맨 처음 단어의 첫 글자는 소문자로 작성한다.)
* 테스트 클래스의 메서드이름에는 언더스코어를 허용한다.
* 메서드 이름은 동사나 전치사로 시작한다.

  * 동사 사용: `createPost()`
  * 전환 메서드의 전치사: `toString()`
  * Builder 패턴 적용한 클래스 메서드의 전치사 : `withUserId(String id)`

* 상수는 대문자를 쓰되, 단어 사이의 구분을 위하여 언더스코어(_)를 사용한다.
* 상태를 가지지 않는 자료형이며 `static final`로 선언된 필드를 상수로 간주한다.

```Java
public static final int UNLIMITED = -1;
private static final String JWT_HEADER = "Bearer ";
```

* 변수 이름은 메서드와 같은 카멜 표기법을 사용한다.

```Java
// 좋은 예
private String accessToken;
// 나쁜 예
private String AccessToken;
```

* 한 문자로만 이루어진 변수 이름은 암시적으로만 사용하고 버릴 변수일 경우를 제외하고는 피해야 한다.
* (***ex.*** for 문에서의 인덱스 변수 `i`)
* (알파벳 소문자 `l`과 `o`는 가독성을 이유로 엄격히 금지한다.)

```Java
// 좋은 예
for (int i = 0; i < 10; i++) {
  doSomthing(i);
}
// 나쁜 예
int i = 0;
doSomthing(i);
doSomthingElse(i);
```

* `boolean`형 변수나 반환값을 갖는 메서드의 경우 이름 앞에 `is`를 붙여준다.

```Java
// 좋은 예
private boolean isActiveUser;
private boolean isValidEmail(String email);
// 나쁜 예
private boolean activeUser;
```

[목차로 돌아가기](#목차)

## 1-3. 선언(Declaration)

클래스, 필드, 메서드, 변수값, import문 등의 소스 구성요소를 선언할 때 고려해야할 규칙이다.

* 탑레벨 클래스(Top level class)는 소스 파일에 1개만 존재해야 한다.

```Java
// 나쁜 예
public class PostController {
}

class PostConverter {
}

// 좋은 예
public class PostController {
    // 굳이 한 파일안에 선언해야 한다면 내부 클래스로 선언
    class PostConverter {
    }
}
```

* 클래스/메서드/멤버변수의 제한자는 [Java Language Specification](https://docs.oracle.com/javase/specs/jls/se7/html/jls-18.html)에서 명시한 아래의 순서로 쓴다.

`public protected private abstract static final`

* 클래스, 인터페이스, 메서드, 생성자에 붙는 어노테이션은 선언 후 새줄을 사용한다.

```Java
@RequestMapping("/users")
public void signUp() {}
```

* 문장이 끝나는 `;` 뒤에는 새줄을 삽입한다. 한 줄에 여러 문장을 쓰지 않는다.

```Java
// 좋은 예
int width = 0;
int height = 2;
// 나쁜 예
int width = 0; int height = 2;
```

* 변수 선언문은 한 문장에서 하나의 변수만을 다룬다.

```Java
// 좋은 예
int width;
int height;
// 나쁜 예
int width, height;
```

* 배열 선언에 오는 대괄호`[]`는 타입의 바로 뒤에 붙인다. 변수명 뒤에 붙이지 않는다.

```Java
// 좋은 예
String[] names;
// 나쁜 예
String names[];
```

* `long`형의 숫자에는 마지막에 대문자 `L`을 붙인다. 소문자 `l`보다 숫자 `1`과의 차이가 커서 가독성이 높아진다.

```Java
// 좋은 예
long userId = 1L;
// 나쁜 예
long userId = 1l;
```

* 지역 변수의 경우, 지역 변수를 선언할 때 초기화 하는 것이 좋다. 단, 변수의 초기화 값이 다른 계산에 의해서 결정되는 경우라면 선언할 때 초기화 하지 않아도 괜찮다.

[목차로 돌아가기](#목차)

## 1-4. 들여쓰기(Indentation)

인텔리제이의 기본 설정(코드 서식 재지정: `Ctrl + Alt + L`)을 사용한다. 저장 시 액션에 해당 기능을 체크하면 편하게 사용할 수 있다.

![code-formatting](/img/code-formatting.png)

[목차로 돌아가기](#목차)

## 1-5. 중괄호(Braces)

중괄호`{`,`}` 는 클래스, 메서드, 제어문의 블럭을 구분한다.

* 아래의 키워드는 닫는 중괄호(}) 와 같은 줄에 쓴다.
  * else
  * catch, finaly
  * do-while 문에서의 while

```Java
// 좋은 예
if (post.getUser().isActive()) {
  doSomthing();
} else {
  doSomthingElse();
}
// 나쁜 예
if (post.getUser().isActive()) {
  doSomthing();
}
else {
  doSomthingElse();
}
```

* 내용이 없는 블럭을 선언할 때는 같은 줄에서 중괄호를 닫는 것을 허용한다.

```Java
public void doNothing() {}
```

* 조건, 반복문이 한 줄짜리라도 중괄호를 활용한다.

```Java
// 좋은 예
if (post.getUser().isActive()) {
  doSomthing();
}
// 나쁜 예
if (post.getUser().isActive()) doSomthing();
```

[목차로 돌아가기](#목차)

## 1-6. 줄바꿈(Line-wrapping)

줄바꿈은 작성한 명령어가 줄 너비를 초과했을 경우 코드 가독성을 위해서 강제로 줄을 바꾸는 것을 말한다.

* 한 줄에 80자 이상 쓰는 것은 대부분의 터미널(terminal)과 툴에서 다룰 수 없기 때문에 피해야 한다.

* 가독성을 위해 줄을 바꾸는 위치는 다음 중의 하나로 한다.
  * `extends` 선언 후
  * `implements` 선언 후
  * `throws` 선언 후
  * 시작 소괄호(() 선언 후
  * 콤마`,` 후
  * `.` 전
  * 연산자 전
    * `+`, `-`, `*`, `/`, `%`
    * `==`, `!=`, `>=`, `>`, `<=`, `<`, `&&`, `||`
    * `&`, `|`, `^`, `>>>`, `>>`, `<<`, `?`
    * `instanceof`

```Java
// 좋은 예)
// 들여쓰기가 없는 경우
public void someMethod(int foo, Object bar) { 
    doSomthing();
}
// 들여쓰기가 필요한 경우
public void someMethod(
        // 메서드 본문 시작을 구분하기 위해 들여쓰기는 8개의 빈칸을 사용한다.
        int foo,
        Object bar,
        String fooo,
        Object baar) {
    // 메서드 본문
    doSomthing();
}
// 나쁜 예)
public void someMethod(
    int foo,
    Object bar,
    String fooo,
    Object baar) {
    // 메서드 본문 시작이 명확하지 않다. 
    doSomthing();
}

// 삼항식의 경우
// 들여쓰기가 없는 경우
int foo = (aBooleanExpression) ? foo : bar;
// 들여쓰기가 필요한 경우
int foo = (aLongBooleanExpression)
        ? foo
        : bar;
```

[목차로 돌아가기](#목차)

## 1-7. 빈 줄(Blank lines)

빈 줄은 명령문 그룹의 영역을 표시하기 위하여 사용한다.

다음의 경우 빈 줄을 삽입한다.

* 메서드의 선언이 끝난 후 다음 메서드 선언이 시작되기 전에

```Java
// 좋은 예
public void setId(int id) {
    this.id = id;
}

public void setName(String name) {
    this.name = name;
}
```

* 메서드 안에서의 지역 변수와 그 메서드의 첫 번째 문장 사이에서

```Java
public void foo() {
    int bar = 0;

    doSomthing();
}
```

* 블록의 첫 번째 줄을 제외한 주석 이전에

```Java
public void foo() {
    // 언젠가 사용할 변수
    int bar = 0;

    // 무언가를 하는 메서드
    doSomthing();
}
```

* 가독성을 향상시키기 위한 메서드 내부의 논리적인 섹션들 사이에

```Java
public void foo() {
    int bar = 0;

    doSomthing1();
    doSomthing2();

    doSomthingElse1();
    doSomthingElse2();
}
```

[목차로 돌아가기](#목차)

## 1-8. 주석(Comment)

* 메서드, 자료구조, 알고리즘에 대한 설명을 제공할 때는 블럭 주석을 사용한다.
* 블록 주석은 다른 코드들과 구분하기 위해서 처음 한 줄은 비우고 사용한다.

```Java
/*
 * 이 메서드는 아무 것도 하지 않는다.
 */
public void doNothing() {}
```

* 코드 라인에 대한 설명이 필요할 때 한 줄 주석을 사용한다.
* 한 줄 주석은 뒤따라 오는 코드와 같은 동일한 들여쓰기를 하는 한 줄로 작성할 수 있다.

```Java
public void updatePost(long postId, PostDto postDto) { 

    // repository에서 post를 찾는다.
    var post = postRepository.findById(postId);
    // post 내용을 넣는다.
    post.update(postDto);
}
```

[목차로 돌아가기](#목차)

## 1-9. 기타(etc)

* 클래스(static) 변수와 클래스(static) 메서드는 클래스 이름을 사용하여 호출한다.

```Java
// 객체 생성
Foo foo = new Foo();
// 일반적인 메서드 호출
foo.doSomthing();
// 클래스 메서드(static) 호출
Foo.classMethod();
// 클래스 메서드(static)의 나쁜 사용방법
foo.classMethod();
```

* 숫자는 바로 사용하지 않고 변수로 선언해서 사용한다.
* 숫자 상수는 for루프에 카운트 값으로 나타나는 -1, 0, 1을 제외하고는 숫자 자체를 코드에 사용하지 말자.

```Java
// 좋은 예
final int maxUserCount = 10;
doSomthing(maxUserCount);
// 나쁜 예
doSomthing(10);
```

* 삼항 연산자에서 `?` 이전에 이항 연산자를 포함하는 식이 있는 경우에는, 꼭 괄호를 사용해야 한다.

```Java
(x >= 0) ? x : -x;
```

## 2. DataBase

* 모든 이름은 snake_case를 사용한다. snake_case란 모든 글자가 소문자이고, 언더스코어(_)로 단어를 구분하는 표기법이다.

  ex) First_Name(x) -> first_name(o)

* 축약어를 사용하지 않는다.

* prefix와 postfix(단어의 앞이나 뒤에 설명을 위한 표현을 붙이는 것)는 사용하지 않는다.

  ex) user_TB (X)

* 테이블의 이름은 복수가 아닌 단수를 사용한다.

  ex) users(x) -> user(o)

* 테이블이 하나의 Primary Key를 가진다면 그 속성의 이름은 id로 한다.

  ex) user_id (X) -> id

* Foreign Key는 테이블 이름과 속성 이름을 더해 정한다.

  ex) user 테이블의 id -> user_id

참고:

[How I Write SQL, Part 1: Naming Conventions](https://launchbylunch.com/posts/2014/Feb/16/sql-naming-conventions/)

[목차로 돌아가기](#목차)

## 3. Package Directory Structure

패키지 구조는 계층별(Packages by Layer)이 아닌, 기능별(Packages by Feature)로 생성한다.

### 계층별 구조

```text
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── demo
    │   │               ├── DemoApplication.java
    │   │               ├── config
    │   │               ├── controller
    │   │               ├── dao
    │   │               ├── domain
    │   │               ├── exception
    │   │               └── service
    │   └── resources
    │       └── application.properties

```

### 기능별 구조

```text
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── demo
    │   │               ├── DemoApplication.java
    │   │               ├── coupon
    │   │               │   ├── controller
    │   │               │   ├── domain
    │   │               │   ├── exception
    │   │               │   ├── repository
    │   │               │   └── service
    │   │               ├── member
    │   │               │   ├── controller
    │   │               │   ├── domain
    │   │               │   ├── exception
    │   │               │   ├── repository
    │   │               │   └── service
    │   │               └── order
    │   │                   ├── controller
    │   │                   ├── domain
    │   │                   ├── exception
    │   │                   ├── repository
    │   │                   └── service
    │   └── resources
    │       └── application.properties
```

참고:

[Spring Guide - Directory 패키지 구조 가이드](https://cheese10yun.github.io/spring-guide-directory/)

[지역성의 원칙을 고려한 패키지 구조: 기능별로 나누기](https://ahnheejong.name/articles/package-structure-with-the-principal-of-locality-in-mind/)

[목차로 돌아가기](#목차)

## 4. GitHub

### 4-1. Commit Message Structure

기본 적인 커밋 메시지 구조는 제목, 본문, 꼬리말 3개의 파트로 나누고, 각 파트는 빈 줄을 두어 구분한다.

```text
type: Subject

body

footer
```

[목차로 돌아가기](#목차)

### 4-2. The Type

제목은 `타입: 제목`의 형태이며, 타입은 이런 것들이 있다.

* feat : 새로운 기능 추가
* fix : 버그 수정
* docs : 문서 수정
* style : 코드 서식, 세미콜론 누락, 등; ***코드 변경이 없는 경우***
* refactor : 코드 리펙토링
* test : 테스트 코드, 리펙토링 테스트 코드 추가; ***프로덕션 코드 변경이 없는 경우***
* chore : 빌드 업무 수정, 패키지 매니저 수정; ***프로덕션 코드 변경이 없는 경우***

[목차로 돌아가기](#목차)

### 4-3. The Subject

제목은 최대 50글자가 넘지 않도록 하고 마침표 및 특수기호는 사용하지 않는다.

명령형 문장을 사용해 커밋이 무엇을 하는지 표현한다. 과거 시제를 사용하지 않는다.

문법적으로 완전한 문장보다, 간결하게 요약된 문장을 사용한다.

* Fixed --> Fix
* Added --> Add
* Modified --> Modify

[목차로 돌아가기](#목차)

### 4-4. The Body

본문은 선택사항이며 설명이 필요한 경우 작성한다.

* 본문은 한 줄 당 72자 내로 작성한다.
* 본문 내용은 양에 구애받지 않고 최대한 상세히 작성한다.
* 본문 내용은 어떻게 변경했는지(how) 보다 무엇을 변경했는지(what) 또는 왜 변경했는지(why)를 설명한다.

[목차로 돌아가기](#목차)

### 4-5. The Footer

꼬릿말 역시 선택사항이다.

* issue tracker id를 참조할 때 사용한다.
* 꼬리말은 `유형: #이슈 번호` 형식으로 작성한다.
* 여러 개의 이슈 번호를 적을 때는 쉼표 `,`로 구분한다.

```text
ex)
Resolves: #123
See also: #456, #789
```

[목차로 돌아가기](#목차)

### 4-6. Commit Example

```text
Feat: 회원 가입 기능 구현

SMS, 이메일 중복확인 API 개발

Resolves: #123
Ref: #456
Related to: #48, #45
```

참고:

[Udacity Git Commit Message Style Guide](https://udacity.github.io/git-styleguide/)

[목차로 돌아가기](#목차)
