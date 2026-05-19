-- 새로운 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS cafe_pos;
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

CREATE TABLE member (
    mem_phone VARCHAR(13) PRIMARY KEY,
    mem_name VARCHAR(20) NOT NULL,
    stamp_cnt INT NOT NULL DEFAULT 0,

    CHECK (stamp_cnt >= 0)
);

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

CREATE TABLE order_item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    prod_id INT NOT NULL,
    temperature VARCHAR(10) NOT NULL,
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
    CHECK (temperature IN ('HOT', 'ICE'))
);

-- 기본 메뉴 데이터
INSERT INTO product 
(prod_name, prod_price, is_coffee, temperature_type, stock)
VALUES 
('아메리카노', 3000, 1, 0, 100),
('카페라떼', 3500, 1, 0, 100),
('딸기스무디', 4500, 0, 2, 50),
('생강차', 4000, 0, 1, 30);

-- 기본 회원 데이터
INSERT INTO member
(mem_phone, mem_name, stamp_cnt)
VALUES
('010-4065-6271', '최민혁', 3);

-- 테스트 주문 데이터
-- 아메리카노 2잔 + 카페라떼 1잔 + 딸기스무디 1잔 + 생강차 1잔 = 18,000원
INSERT INTO orders
(mem_phone, order_date, total_price)
VALUES
(NULL, '2026-05-17 14:00:00', 18000);

INSERT INTO order_item
(order_id, prod_id, temperature, quantity, unit_price)
VALUES
(1, 1, 'ICE', 2, 3000),
(1, 2, 'HOT', 1, 3500),
(1, 3, 'ICE', 1, 4500),
(1, 4, 'HOT', 1, 4000);

-- 확인용 조회
SELECT * FROM product;
SELECT * FROM member;
SELECT * FROM orders;
SELECT * FROM order_item;
