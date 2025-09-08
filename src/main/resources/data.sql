INSERT INTO users (name, email, password, role) VALUES
    ('出品者A', 'sellerA@example.com', 'password', 'USER'),
    ('購入者B', 'z', 'password', 'USER'),
    ('運営者C', 'adminC@example.com', 'adminpass', 'ADMIN');

INSERT INTO category (name) VALUES
    ('本'),
    ('家電'),
    ('ファッション'),
    ('おもちゃ');

INSERT INTO item (user_id, name, description, price, category_id, status) VALUES
    ((SELECT id FROM users WHERE email = 'sellerA@example.com'), 'Javaプログラミング入門', '初心者向けのJava入門書です。', 1500.00, (SELECT id FROM category WHERE name = '本'), '出品中'),
    ((SELECT id FROM users WHERE email = 'sellerA@example.com'), 'ワイヤレスイヤホン', 'ノイズキャンセリング機能付き。', 8000.00, (SELECT id FROM category WHERE name = '家電'), '出品中');

-- INSERT INTO app_order (item_id, buyer_id, price, status) VALUES
--     ((SELECT id FROM item WHERE name = 'Javaプログラミング入門'), (SELECT id FROM users WHERE email = 'buyerB@example.com'), 1500.00, '購入済');