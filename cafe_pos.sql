-- 새로운 데이터베이스 생성 
USE cafe_pos;

-- 기존 테이블 삭제
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS product;


CREATE TABLE product (
    prod_id INT AUTO_INCREMENT PRIMARY KEY,
    prod_name VARCHAR(50) UNIQUE NOT NULL,
    prod_price INT NOT NULL DEFAULT 0,
    is_coffee TINYINT(1) NOT NULL DEFAULT 1,
    temperature_type INT NOT NULL DEFAULT 0,
    stock INT NOT NULL DEFAULT 0,

    CHECK (prod_price >= 0),
    CHECK (is_coffee IN (0, 1)),
    CHECK (temperature_type IN (0, 1, 2)),
    CHECK (stock >= 0)
);

-- temperature_type 의미
-- 0: HOT / ICE 둘 다 가능
-- 1: HOT ONLY
-- 2: ICE ONLY

-- is_coffee 의미
-- 1: 커피
-- 0: 비커피

-- stock 의미
-- stock > 0 : 판매 가능
-- stock = 0 : 품절

-- ========================================================
-- 2. member : 회원 / 스탬프 관리 테이블
-- ========================================================

CREATE TABLE member (
    mem_phone VARCHAR(13) PRIMARY KEY,
    mem_name VARCHAR(20) NOT NULL,
    stamp_cnt INT NOT NULL DEFAULT 0,

    CHECK (stamp_cnt >= 0)
);

-- mem_phone 예시: 010-1111-2222
-- 비회원 주문은 orders.mem_phone을 NULL로 처리

-- ========================================================
-- 3. orders : 주문 한 건 전체 정보
-- ========================================================

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    mem_phone VARCHAR(13),
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_price INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_orders_member
        FOREIGN KEY (mem_phone)
        REFERENCES member(mem_phone),

    CHECK (total_price >= 0)
);

-- orders = 영수증 한 장
-- total_price = 주문 전체 금액

-- ========================================================
-- 4. order_item : 주문 상세 메뉴 정보
-- ========================================================

CREATE TABLE order_item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    prod_id INT NOT NULL,
    prod_option VARCHAR(150),
    temperature VARCHAR(10),
    quantity INT NOT NULL DEFAULT 1,
    unit_price INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_order_item_orders
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id),

    CONSTRAINT fk_order_item_product
        FOREIGN KEY (prod_id)
        REFERENCES product(prod_id),

    CHECK (quantity > 0),
    CHECK (unit_price >= 0),
    CHECK (temperature IN ('HOT', 'ICE') OR temperature IS NULL)
);

-- order_item = 영수증 안의 메뉴 한 줄
-- unit_price = 주문 당시 메뉴 1개 가격

-- ========================================================
-- A. 기본 메뉴 세팅
-- ========================================================

INSERT INTO product 
(prod_name, prod_price, is_coffee, temperature_type, stock)
VALUES 
('아메리카노', 2000, 1, 0, 100),  --  1 HOT, ICE 가능
('카페라떼', 3300, 1, 0, 100),    -- 2 HOT, ICE 가능
('바닐라라떼', 3900, 1, 0, 100),   -- 3 HOT, ICE 가능
('에스프레소', 1800, 1, 1, 200),    -- 4 HOT ONLY
('딸기스무디', 4000, 0, 2, 50),    -- 5 비커피 / ICE ONLY
('피스타치오라떼', 3800, 1, 0, 50),    -- 6 커피/ HOT, ICE 가능
('문경오미자티', 3800, 0, 0, 50),    -- 7 비커피 /  HOT, ICE 가능
('오곡미숫가루라떼', 3500, 0, 0, 50);  -- 8 비커피 /  HOT, ICE 가능

-- ========================================================
-- B. 기본 회원 데이터
-- ========================================================

INSERT INTO member
(mem_phone, mem_name, stamp_cnt)
VALUES
('010-4065-6271', '최민혁', 3);

-- ========================================================
-- 테스트 주문 데이터
-- 아메리카노 2잔 + 카페라떼 1잔 + 딸기스무디 1잔 + 생강차 1잔
-- 총액 = 18000원
-- ========================================================

INSERT INTO orders
(mem_phone, order_date, total_price)
VALUES
(NULL, '2026-05-17 14:00:00', 18000);

INSERT INTO order_item
(order_id, prod_id, prod_option, temperature, quantity, unit_price)
VALUES
(1, 1, NULL, 'ICE', 2, 3000),  -- 아메리카노 2잔
(1, 2, NULL, 'HOT', 1, 3500),  -- 카페라떼 1잔
(1, 3, NULL, 'ICE', 1, 4500),  -- 딸기스무디 1잔
(1, 4, NULL, 'HOT', 1, 4000);  -- 생강차 1잔



