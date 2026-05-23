# 미니프로젝트

##  프로젝트명

**바나프레소 카페 POS 시스템 만들기**

---

## �� 팀원

| 이름 |
|---|
| 김태중 |
| 윤태형 |
| 정윤서 |
| 최민혁 |

---

## 프로젝트 기간

**2026-05-18 ~ 2026-05-22**

---

## 프로젝트 소개

본 프로젝트는 바나프레소 카페 운영 환경을 가정하여 제작한 **Java Swing 기반 카페 POS 시스템**입니다.

카페 직원이 메뉴를 선택하고, 장바구니에 상품을 담고, 회원 정보를 확인한 뒤 결제를 처리할 수 있도록 구현했습니다.  
또한 MySQL 데이터베이스와 JDBC를 연동하여 상품, 회원, 주문, 주문 상세 정보를 저장하고 조회할 수 있도록 설계했습니다.

관리자 모드에서는 매출 통계, 시간대별 주문 건수, 월별 매출, 재고 수정 기능을 제공하여 POS 시스템의 기본적인 운영 흐름을 경험할 수 있도록 구성했습니다.

---

## ��️ 사용 기술

### Language
- Java

### GUI
- Java Swing

### Database
- MySQL
- JDBC

### Tool
- Eclipse
- MySQL Workbench
- Git / GitHub

### Library
- MySQL Connector/J

---

##  주요 기능

### 1. 메뉴 조회 및 주문 기능
- 상품 목록 조회
- 메뉴 이미지 표시
- HOT / ICE 옵션 선택
- 샷 조절 옵션 선택
- 디카페인 옵션 선택
- 수량 선택
- 장바구니 추가
- 품절 상품 표시

### 2. 장바구니 기능
- 같은 메뉴, 같은 온도, 같은 옵션이면 수량 누적
- 장바구니 전체 금액 계산
- 전체 취소 기능

### 3. 회원 기능
- 전화번호 기반 회원 조회
- 신규 회원 등록
- 회원 스탬프 조회
- 결제 후 스탬프 적립

### 4. 결제 기능
- 회원 / 비회원 결제 처리
- 스탬프 할인 적용
- 주문 생성
- 주문 상세 저장
- 상품 재고 차감
- 결제 실패 시 트랜잭션 rollback 처리

### 5. 주문 내역 조회 기능
- 날짜별 주문 내역 조회
- 주문번호별 주문 상세 표시
- 회원 / 비회원 구분
- 총 주문 금액, 할인 금액, 최종 결제 금액 표시

### 6. 관리자 기능
- 오늘 / 어제 매출 비교
- 이번 주 누적 매출 조회
- 오늘 판매량 TOP 3 메뉴 조회
- 시간대별 주문 건수 조회
- 월별 매출 조회
- 상품 재고 수정

---

##  데이터베이스 설계

본 프로젝트는 MySQL 데이터베이스 `cafe_pos`를 사용합니다.  
카페 POS 시스템의 주문 흐름에 맞춰 상품, 회원, 주문, 주문 상세 정보를 분리하여 관리합니다.

### ERD 개념 구조

```text
member ───< orders ───< order_item >─── product
```

### 테이블 설명

| 테이블명 | 설명 |
|---|---|
| `product` | 상품명, 가격, 커피 여부, 온도 옵션, 재고 수량 관리 |
| `member` | 회원 전화번호, 이름, 스탬프 수 관리 |
| `orders` | 주문 한 건의 주문 일시, 회원 정보, 최종 결제 금액 관리 |
| `order_item` | 주문에 포함된 개별 메뉴, 온도, 옵션, 수량, 단가 관리 |

### 테이블 관계

- `orders.mem_phone` → `member.mem_phone`
- `order_item.order_id` → `orders.order_id`
- `order_item.prod_id` → `product.prod_id`
- 비회원 주문은 `orders.mem_phone`을 `NULL`로 저장합니다.
- 하나의 주문은 여러 개의 주문 상세 항목을 가질 수 있습니다.
- 하나의 상품은 여러 주문 상세 항목에서 참조될 수 있습니다.

### 주요 컬럼

#### `product`

| 컬럼명 | 설명 |
|---|---|
| `prod_id` | 상품 고유 번호, PK |
| `prod_name` | 상품명 |
| `prod_price` | 기본 가격 |
| `is_coffee` | 커피 여부, 1이면 커피, 0이면 비커피 |
| `temperature_type` | 온도 옵션 타입 |
| `stock` | 현재 재고 수량 |

`temperature_type` 값의 의미는 다음과 같습니다.

| 값 | 의미 |
|---|---|
| `0` | HOT / ICE 모두 가능 |
| `1` | HOT ONLY |
| `2` | ICE ONLY |

#### `member`

| 컬럼명 | 설명 |
|---|---|
| `mem_phone` | 회원 전화번호, PK |
| `mem_name` | 회원 이름 |
| `stamp_cnt` | 보유 스탬프 수 |

#### `orders`

| 컬럼명 | 설명 |
|---|---|
| `order_id` | 주문 번호, PK |
| `mem_phone` | 회원 전화번호, FK, 비회원 주문이면 NULL |
| `order_date` | 주문 일시 |
| `total_price` | 최종 결제 금액 |

#### `order_item`

| 컬럼명 | 설명 |
|---|---|
| `order_item_id` | 주문 상세 번호, PK |
| `order_id` | 주문 번호, FK |
| `prod_id` | 상품 번호, FK |
| `prod_option` | 샷 추가, 디카페인 등 옵션 |
| `temperature` | HOT 또는 ICE |
| `quantity` | 주문 수량 |
| `unit_price` | 옵션가가 반영된 주문 당시 단가 |

---

##  프로젝트 구조

```text
cafe_pos_prjt/
├── .classpath
├── .project
├── lib/
│   └── mysql-connector-j-8.4.0.jar
├── img/
│   ├── 1.jpg
│   ├── 2.jpg
│   └── ...
├── src/
│   └── cafe/
│       ├── dao/
│       │   └── CafeDAO.java
│       ├── model/
│       │   ├── BasketItem.java
│       │   └── ProductDTO.java
│       ├── service/
│       │   ├── BasketService.java
│       │   ├── MemberService.java
│       │   └── PaymentService.java
│       └── ui/
│           ├── MainFrame.java
│           └── AdminFrame.java
└── README.md
```

> `bin/` 폴더는 Java 컴파일 결과물이 생성되는 폴더이므로 GitHub에는 올리지 않는 것이 일반적입니다.

---

## 패키지 역할

| 패키지 | 역할 |
|---|---|
| `cafe.ui` | POS 메인 화면과 관리자 화면 등 사용자 인터페이스 담당 |
| `cafe.dao` | MySQL 연결 및 SQL 실행 담당 |
| `cafe.model` | 상품, 장바구니 항목 등 데이터 객체 관리 |
| `cafe.service` | 장바구니, 회원, 결제 관련 비즈니스 로직 담당 |

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `MainFrame.java` | POS 메인 화면, 메뉴 선택, 장바구니, 회원조회, 결제, 주문내역 기능 |
| `AdminFrame.java` | 관리자 모드, 매출 통계, 시간대별 주문 건수, 월별 매출, 재고 수정 기능 |
| `CafeDAO.java` | DB 연결, 상품 조회, 회원 관리, 결제 처리, 주문 내역 및 매출 통계 조회 |
| `ProductDTO.java` | 상품 정보 DTO |
| `BasketItem.java` | 장바구니 항목 데이터 관리 |
| `BasketService.java` | 장바구니 추가, 초기화, 총액 계산 |
| `MemberService.java` | 회원 조회, 신규 회원 등록, 현재 회원 상태 관리 |
| `PaymentService.java` | 스탬프 할인 계산 및 결제 처리 |

---

##  주요 처리 흐름

### 주문 및 결제 흐름

```text
메뉴 선택
→ 온도 / 옵션 / 수량 선택
→ 장바구니 추가
→ 회원 조회 또는 비회원 주문
→ 스탬프 할인 선택
→ 결제 처리
→ orders 생성
→ order_item 저장
→ product 재고 차감
→ member 스탬프 갱신
```

### 관리자 기능 흐름

```text
관리자 모드 진입
→ 매출 통계 조회
→ 시간대별 주문 건수 조회
→ 월별 매출 조회
→ 상품 재고 수정
→ 메인 메뉴판 재로드
```

---

##  실행 방법

### 1. 프로젝트 가져오기

GitHub 레포지토리를 clone합니다.

```bash
git clone https://github.com/MinhyeokChoi99/cafe_pos_prjt.git
```

Eclipse에서 Java Project로 import합니다.

```text
File → Import → Existing Projects into Workspace
```

### 2. MySQL 데이터베이스 생성

MySQL Workbench에서 프로젝트용 SQL 파일을 실행하여 `cafe_pos` 데이터베이스와 테이블을 생성합니다.

```sql
CREATE DATABASE IF NOT EXISTS cafe_pos;
USE cafe_pos;
```

### 3. JDBC 드라이버 확인

프로젝트의 `lib` 폴더에 MySQL JDBC 드라이버가 있어야 합니다.

```text
mysql-connector-j-8.4.0.jar
```

Eclipse에서 Build Path에 해당 JAR 파일이 연결되어 있는지 확인합니다.

```text
Project 우클릭
→ Build Path
→ Configure Build Path
→ Libraries
```

### 4. DB 접속 정보 확인

`CafeDAO.java`에서 MySQL 접속 정보를 본인 환경에 맞게 확인합니다.

```java
private final String url = "jdbc:mysql://localhost:3306/cafe_pos";
private final String user = "root";
private final String password = "root";
```

MySQL 비밀번호가 다르면 `password` 값을 수정해야 합니다.

### 5. 프로그램 실행

`MainFrame.java` 파일을 실행합니다.

```text
Run As → Java Application
```

---

## ✅ 기대 효과

이 프로젝트를 통해 Java Swing을 활용한 GUI 개발, JDBC 기반 데이터베이스 연동, 그리고 실제 POS 시스템의 주문 처리 흐름을 구현해볼 수 있었습니다.

특히 상품, 회원, 주문, 주문 상세 데이터를 분리하여 관리하면서 관계형 데이터베이스 설계와 외래키 관계를 이해할 수 있었습니다.  
또한 주문 저장, 주문 상세 저장, 재고 차감, 회원 스탬프 갱신을 하나의 흐름으로 처리하면서 트랜잭션 관리의 중요성을 경험했습니다.

---

## 프로젝트 회고

이번 미니프로젝트를 통해 단순히 화면을 구성하는 것을 넘어서, 사용자가 메뉴를 선택하고 결제한 정보가 실제 데이터베이스에 저장되고 다시 조회되는 전체 흐름을 구현했습니다.

주문, 주문 상세, 회원, 상품 테이블 간의 관계를 직접 설계하면서 데이터가 어떤 순서로 저장되고 참조되는지 이해할 수 있었습니다.  
또한 기능이 많아질수록 UI 코드와 비즈니스 로직을 분리하는 것이 중요하다는 점을 배웠습니다.

향후에는 다음과 같은 기능을 추가로 개선할 수 있습니다.

- 주문 취소 및 환불 기능
- 관리자 로그인 보안 강화
- 상품 이미지 관리 기능
- 일별 / 월별 매출 그래프 시각화
- 영수증 출력 기능
- 더 명확한 예외 처리 및 사용자 안내 메시지 개선
