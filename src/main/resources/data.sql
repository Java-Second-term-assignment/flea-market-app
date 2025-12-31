-- users (一般ユーザー)
INSERT INTO users (name, email, password, enabled)
VALUES
  ('出品者A', 'sellerA@example.com', '{noop}password', TRUE),
  ('購入者B', 'xyz@example.com', '{noop}password', TRUE);

-- admin (管理者)
-- パスワード: Admin123!@# (12文字以上、大文字・小文字・数字・記号を含む)
INSERT INTO admin (email, password, name, is_active, created_at, updated_at)
VALUES
  ('admin@example.com', '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理者', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- category
INSERT INTO category (name) VALUES
  ('本'), ('家電'), ('ファッション'), ('おもちゃ'),('文房具');

-- item
INSERT INTO item (user_id, name, description, price, category_id, status)
VALUES
  ((SELECT id FROM users WHERE email='sellerA@example.com'),
   'Javaプログラミング入門','初心者向けのJava入門書です。',1500.00,
   (SELECT id FROM category WHERE name='本'),'出品中'),

  ((SELECT id FROM users WHERE email='sellerA@example.com'),
   'ワイヤレスイヤホン','ノイズキャンセリング機能付き。',8000.00,
   (SELECT id FROM category WHERE name='家電'),'出品中');
