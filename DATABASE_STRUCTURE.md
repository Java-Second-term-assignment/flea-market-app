# データベース構造図

## テーブル一覧

### users テーブル
| カラム名              | 型          | 説明                   |
|----------------------|-------------|------------------------|
| id                   | int4 (PK)   | ユーザーID             |
| name                 | varchar(50) | 表示名                 |
| email                | varchar(255) | メールアドレス（UNIQUE） |
| password             | varchar(255) | パスワード             |
| role                 | varchar(20) | ロール（USER/ADMIN）   |
| line_notify_token    | varchar(255) | LINE Notifyトークン    |
| enabled              | bool        | 有効/無効（DEFAULT TRUE） |
| real_name            | varchar(100) | 本名                   |
| furigana             | varchar(100) | フリガナ               |
| phone_number         | varchar(20) | 電話番号               |
| postal_code          | varchar(10) | 郵便番号               |
| address              | text        | 住所                   |
| age                  | int4        | 年齢                   |
| gender               | varchar(10) | 性別（MALE/FEMALE/OTHER） |
| banned               | bool        | BAN状態（DEFAULT FALSE） |
| ban_reason           | text        | BAN理由                |
| banned_at            | timestamp   | BAN日時                |
| banned_by_admin_id   | int4        | BAN実行管理者ID        |
| password_reset_token | varchar(255) | パスワードリセットトークン（UUID） |
| password_reset_token_expiry | timestamp | パスワードリセットトークン有効期限（24時間） |

### category テーブル
| カラム名        | 型          | 説明                   |
|----------------|-------------|------------------------|
| id             | int4 (PK)   | カテゴリID             |
| name           | varchar(50) | カテゴリ名（UNIQUE）   |

### item テーブル
| カラム名        | 型          | 説明                   |
|----------------|-------------|------------------------|
| id             | int4 (PK)   | 商品ID                 |
| user_id        | int4 (FK)   | 出品者ID（users.id）   |
| name           | varchar(255) | 商品名                 |
| description    | text        | 説明                   |
| price          | numeric(10,2) | 価格                   |
| category_id    | int4 (FK)   | カテゴリID（category.id） |
| status         | varchar(20) | 状態（DEFAULT '出品中'） |
| image_url      | text        | 画像URL                |
| created_at     | timestamp   | 作成日時（DEFAULT CURRENT_TIMESTAMP） |

### app_order テーブル
| カラム名        | 型          | 説明                   |
|----------------|-------------|------------------------|
| id             | int4 (PK)   | 注文ID                 |
| item_id        | int4 (FK)   | 商品ID（item.id）      |
| buyer_id       | int4 (FK)   | 購入者ID（users.id）   |
| price          | numeric(10,2) | 購入価格               |
| status         | varchar(20) | 注文状態（DEFAULT '購入済'） |
| payment_intent_id | varchar(128) | Stripe決済ID（UNIQUE） |
| created_at     | timestamp   | 作成日時（DEFAULT CURRENT_TIMESTAMP） |

### chat テーブル
| カラム名        | 型          | 説明                   |
|----------------|-------------|------------------------|
| id             | int4 (PK)   | チャットID             |
| item_id        | int4 (FK)   | 商品ID（item.id）      |
| sender_id      | int4 (FK)   | 送信者ID（users.id）   |
| message        | text        | メッセージ内容         |
| created_at     | timestamp   | 送信日時（DEFAULT CURRENT_TIMESTAMP） |

### favorite_item テーブル
| カラム名        | 型          | 説明                   |
|----------------|-------------|------------------------|
| id             | int4 (PK)   | お気に入りID           |
| user_id        | int4 (FK)   | ユーザーID（users.id） |
| item_id        | int4 (FK)   | 商品ID（item.id）      |
| created_at     | timestamp   | 登録日時（DEFAULT CURRENT_TIMESTAMP） |
| UNIQUE (user_id, item_id) | | ユーザーと商品の組み合わせは一意 |

### review テーブル
| カラム名        | 型          | 説明                   |
|----------------|-------------|------------------------|
| id             | int4 (PK)   | レビューID             |
| order_id       | int4 (FK)   | 注文ID（app_order.id、UNIQUE） |
| reviewer_id    | int4 (FK)   | レビュー投稿者ID（users.id） |
| seller_id      | int4 (FK)   | 出品者ID（users.id）   |
| item_id        | int4 (FK)   | 商品ID（item.id）      |
| rating         | int4        | 評価（1-5）           |
| comment        | text        | コメント               |
| created_at     | timestamp   | 作成日時（DEFAULT CURRENT_TIMESTAMP） |

### user_complaint テーブル
| カラム名        | 型          | 説明                   |
|----------------|-------------|------------------------|
| id             | int4 (PK)   | 通報ID                 |
| reported_user_id | int4 (FK) | 通報されるユーザーID（users.id） |
| reporter_user_id | int4 (FK) | 通報者ID（users.id）   |
| reason         | text        | 通報理由               |
| created_at     | timestamp   | 作成日時（DEFAULT CURRENT_TIMESTAMP） |

## インデックス

### users テーブル
- `idx_users_banned`: `banned`カラム
- `idx_users_banned_by`: `banned_by_admin_id`カラム

### item テーブル
- `idx_item_user_id`: `user_id`カラム
- `idx_item_category_id`: `category_id`カラム

### app_order テーブル
- `idx_order_item_id`: `item_id`カラム
- `idx_order_buyer_id`: `buyer_id`カラム
- `ux_order_pi`: `payment_intent_id`カラム（UNIQUE）

### chat テーブル
- `idx_chat_item_id`: `item_id`カラム
- `idx_chat_sender_id`: `sender_id`カラム

### favorite_item テーブル
- `idx_fav_user_id`: `user_id`カラム
- `idx_fav_item_id`: `item_id`カラム

### review テーブル
- `idx_review_order_id`: `order_id`カラム

### user_complaint テーブル
- `idx_uc_reported`: `reported_user_id`カラム
- `idx_uc_reporter`: `reporter_user_id`カラム

## リレーションシップ

1. **users → item**: 1対多（1人のユーザーが複数の商品を出品可能）
2. **users → app_order**: 1対多（1人のユーザーが複数の注文を購入可能）
3. **users → chat**: 1対多（1人のユーザーが複数のメッセージを送信可能）
4. **users → favorite_item**: 1対多（1人のユーザーが複数の商品をお気に入り登録可能）
5. **users → review**: 1対多（1人のユーザーが複数のレビューを投稿可能）
6. **users → user_complaint**: 1対多（通報者と通報されるユーザーの両方）
7. **category → item**: 1対多（1つのカテゴリに複数の商品が属する）
8. **item → app_order**: 1対多（1つの商品が複数の注文に含まれる可能性、ただし通常は1対1）
9. **item → chat**: 1対多（1つの商品に対して複数のチャットメッセージ）
10. **item → favorite_item**: 1対多（1つの商品が複数のユーザーにお気に入り登録される）
11. **item → review**: 1対多（1つの商品が複数のレビューを受ける可能性）
12. **app_order → review**: 1対1（1つの注文に対して1つのレビュー）
