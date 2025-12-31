# データベーススキーマ定義 (DDL)

このドキュメントには、フリマアプリケーションのデータベーススキーマ定義が含まれています。
要件に合わせて随時修正していきます。

## 目次

1. [役割/権限テーブル](#役割権限テーブル)
2. [ユーザーランクテーブル](#ユーザーランクテーブル)
3. [ユーザーテーブル](#ユーザーテーブル)
4. [管理者テーブル](#管理者テーブル)
5. [カテゴリテーブル](#カテゴリテーブル)
5. [配送方法テーブル](#配送方法テーブル)
6. [商品テーブル](#商品テーブル)
7. [商品画像テーブル](#商品画像テーブル)
8. [注文テーブル](#注文テーブル)
10. [チャット関連テーブル](#チャット関連テーブル)
11. [定型文テーブル](#定型文テーブル)
12. [評価・レビューテーブル](#評価レビューテーブル)
13. [お気に入りリストテーブル](#お気に入りリストテーブル)
14. [違反報告テーブル](#違反報告テーブル)
15. [フォーラム関連テーブル](#フォーラム関連テーブル)
16. [ログ監査テーブル](#ログ監査テーブル)
17. [A/Bテスト関連テーブル](#abテスト関連テーブル)

---

## 役割/権限テーブル

```sql
CREATE TABLE role ( 
    id SERIAL PRIMARY KEY, 
    name VARCHAR(50) UNIQUE NOT NULL -- 役割名 
);
```

**説明**: ユーザーの役割（例: USER, ADMIN）を管理するテーブル。

---

## ユーザーランクテーブル

```sql
CREATE TABLE user_rank (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL, 
    min_transactions INTEGER NOT NULL DEFAULT 0, 
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
```

**説明**: ユーザーのランク（取引数に基づく）を定義するテーブル。

---

## ユーザーテーブル

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id INTEGER REFERENCES role(id) ON DELETE RESTRICT NOT NULL,
    
    -- プロフィールの充実
    introduction TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    avg_response_time_minutes INTEGER,
    
    -- 二段階認証 (2FA)
    is_2fa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- ユーザーランク
    user_rank_id INTEGER REFERENCES user_rank(id) ON DELETE SET NULL,
    
    -- アカウント管理 (UC-10対応)
    account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, BANNED
    
    -- 多言語/通貨対応
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'ja',
    preferred_currency VARCHAR(5) NOT NULL DEFAULT 'JPY',
    
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
```

**説明**: ユーザー情報を管理するメインテーブル。2FA、ランク、多言語対応などの機能を含む。

---

## 管理者テーブル

```sql
CREATE TABLE admin (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    last_login_at TIMESTAMPTZ
);
```

**説明**: 管理者アカウントを管理する専用テーブル。一般ユーザー（usersテーブル）とは完全に分離されており、別々のログインページで認証される。

---

## カテゴリテーブル

```sql
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);
```

**説明**: 商品カテゴリを管理するテーブル。

---

## 配送方法テーブル

```sql
CREATE TABLE shipping_method (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL, -- 配送業者名 (例: ゆうパック, 宅急便)
    base_fee NUMERIC(10, 0) NOT NULL DEFAULT 0, -- 基本料金
    is_seller_burden BOOLEAN NOT NULL DEFAULT FALSE, -- 出品者負担フラグ
    created_at TIMESTAMPTZ DEFAULT now()
);
```

**説明**: 配送方法と料金を管理するテーブル。

---

## 商品テーブル

```sql
CREATE TABLE item (
    id SERIAL PRIMARY KEY,
    seller_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES category(id) ON DELETE SET NULL,
    
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC(10, 0) NOT NULL,
    
    -- 商品の状態
    item_condition VARCHAR(50) NOT NULL, 
    damage_details TEXT, 
    
    -- 配送方法
    shipping_method_id INTEGER REFERENCES shipping_method(id) ON DELETE SET NULL, -- 配送方法テーブルを参照
    
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE', -- 許容値: AVAILABLE, SOLD, DRAFT, SUSPENDED
    
    -- 迷惑行為検知・フィルタ
    is_flagged_auto BOOLEAN NOT NULL DEFAULT FALSE,
    
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
```

**説明**: 商品情報を管理するテーブル。商品の状態、配送方法、自動検知フラグを含む。

---

## 商品画像テーブル

```sql
CREATE TABLE item_image (
    id SERIAL PRIMARY KEY,
    item_id INTEGER REFERENCES item(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);
```

**説明**: 商品の画像を管理するテーブル。1つの商品に複数の画像を紐付け可能。

---

## 注文テーブル

```sql
CREATE TABLE app_order (
   id SERIAL PRIMARY KEY, 
   item_id INTEGER REFERENCES item(id) ON DELETE RESTRICT,
   buyer_id INTEGER REFERENCES users(id) ON DELETE RESTRICT,
   seller_id INTEGER REFERENCES users(id) ON DELETE RESTRICT, -- 追加: 出品者ID
   total_price NUMERIC(10, 0) NOT NULL,
   status VARCHAR(50) NOT NULL, 

   -- 決済情報 
   stripe_charge_id VARCHAR(255), 
   
   -- 配送情報 (UC-07対応)
   shipping_carrier VARCHAR(100), -- 配送業者 (例: Yamato, Yu-Pack)
   tracking_number VARCHAR(100), -- 追跡番号
   
   ordered_at TIMESTAMPTZ DEFAULT now() 
);
```

**説明**: 注文情報を管理するテーブル。決済情報と配送情報を含む。

---

## チャット関連テーブル

### チャットルーム

```sql
CREATE TABLE chat_room ( 
    id SERIAL PRIMARY KEY, 
    room_type VARCHAR(30) NOT NULL, 
    reference_id INTEGER, 
    title VARCHAR(255), 
    is_active BOOLEAN DEFAULT TRUE, 
    created_at TIMESTAMPTZ DEFAULT now(), 
    updated_at TIMESTAMPTZ DEFAULT now() 
);
```

### チャット参加者

```sql
CREATE TABLE chat_participant ( 
    room_id INTEGER REFERENCES chat_room(id) ON DELETE CASCADE, 
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, 
    joined_at TIMESTAMPTZ DEFAULT now(), 
    last_read_at TIMESTAMPTZ, 
    PRIMARY KEY (room_id, user_id) 
);
```

### チャットメッセージ

```sql
CREATE TABLE chat_message ( 
    id SERIAL PRIMARY KEY, 
    room_id INTEGER REFERENCES chat_room(id) ON DELETE CASCADE, 
    sender_id INTEGER REFERENCES users(id) ON DELETE RESTRICT, 
    message TEXT NOT NULL, 
    attachments JSONB, 
    is_system BOOLEAN DEFAULT FALSE, 
    is_flagged_auto BOOLEAN DEFAULT FALSE, 
    created_at TIMESTAMPTZ DEFAULT now() 
);
```

### インデックス (Chat)

```sql
CREATE INDEX idx_chat_room_ref ON chat_room(room_type, reference_id); 
CREATE INDEX idx_chat_message_room_created ON chat_message(room_id, created_at DESC); 
CREATE INDEX idx_chat_message_flagged ON chat_message(is_flagged_auto);
```

**説明**: チャット機能を実現するためのテーブル群。ルーム、参加者、メッセージを分離して管理。

---

## 定型文テーブル

```sql
CREATE TABLE chat_template ( 
    id SERIAL PRIMARY KEY, 
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, 
    template_name VARCHAR(100) NOT NULL, 
    message_text TEXT NOT NULL, 
    created_at TIMESTAMPTZ DEFAULT now() 
);
```

**説明**: ユーザーが作成したチャット用の定型文を管理するテーブル。

---

## 評価・レビューテーブル

```sql
CREATE TABLE review ( 
    id SERIAL PRIMARY KEY, 
    order_id INTEGER REFERENCES app_order(id) ON DELETE CASCADE, 
    user_id INTEGER REFERENCES users(id) ON DELETE RESTRICT, 
    rating INTEGER NOT NULL, 
    comment TEXT, 
    is_auto_suppressed BOOLEAN NOT NULL DEFAULT FALSE, 
    created_at TIMESTAMPTZ DEFAULT now(), 
    updated_at TIMESTAMPTZ DEFAULT now() 
);
```

**説明**: 注文に対する評価・レビューを管理するテーブル。自動抑制機能を含む。

---

## お気に入りリストテーブル

```sql
CREATE TABLE favorite_list ( 
    id SERIAL PRIMARY KEY, 
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, 
    item_id INTEGER REFERENCES item(id) ON DELETE CASCADE, 
    UNIQUE (user_id, item_id), 
    created_at TIMESTAMPTZ DEFAULT now() 
);
```

**説明**: ユーザーがお気に入り登録した商品を管理するテーブル。

---

## 違反報告テーブル

```sql
CREATE TABLE report ( 
    id SERIAL PRIMARY KEY, 
    reporter_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT, 
    target_type VARCHAR(30) NOT NULL, 
    target_id INTEGER NOT NULL, 
    reason_code VARCHAR(50), 
    reason_text TEXT NOT NULL, 
    evidence JSONB, 
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', 
    severity SMALLINT DEFAULT 3, 
    assignee_id INTEGER REFERENCES users(id) ON DELETE SET NULL, 
    created_at TIMESTAMPTZ DEFAULT now(), 
    updated_at TIMESTAMPTZ DEFAULT now(), 
    resolved_at TIMESTAMPTZ, 
    action_taken VARCHAR(50), 
    moderator_note TEXT 
);
```

### インデックス (Report)

```sql
CREATE INDEX idx_report_target ON report(target_type, target_id); 
CREATE INDEX idx_report_status ON report(status); 
CREATE INDEX idx_report_reporter ON report(reporter_user_id);
```

**説明**: 違反行為の報告を管理するテーブル。モデレーターによる対応履歴を含む。

---

## フォーラム関連テーブル

### フォーラム投稿

```sql
CREATE TABLE forum_post ( 
    id SERIAL PRIMARY KEY, 
    user_id INTEGER REFERENCES users(id) ON DELETE RESTRICT, 
    category_id INTEGER REFERENCES category(id) ON DELETE RESTRICT, 
    title VARCHAR(255) NOT NULL, 
    content TEXT NOT NULL, 
    is_public BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at TIMESTAMPTZ DEFAULT now(), 
    updated_at TIMESTAMPTZ DEFAULT now() 
);
```

### フォーラムコメント

```sql
CREATE TABLE forum_comment ( 
    id SERIAL PRIMARY KEY, 
    post_id INTEGER REFERENCES forum_post(id) ON DELETE CASCADE, 
    user_id INTEGER REFERENCES users(id) ON DELETE RESTRICT, 
    content TEXT NOT NULL, 
    created_at TIMESTAMPTZ DEFAULT now() 
);
```

**説明**: 専門カテゴリの質問掲示板機能を実現するテーブル群。

---

## ログ監査テーブル

```sql
CREATE TABLE audit_log ( 
    id SERIAL PRIMARY KEY, 
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL, 
    action_type VARCHAR(50) NOT NULL, 
    details JSONB, 
    ip_address VARCHAR(45), 
    created_at TIMESTAMPTZ DEFAULT now() 
);
```

**説明**: システムの操作ログを記録するテーブル。データ出力元としても使用。

---

## A/Bテスト関連テーブル

### A/Bテストグループ

```sql
CREATE TABLE ab_test_group ( 
    id SERIAL PRIMARY KEY, 
    name VARCHAR(100) UNIQUE NOT NULL, 
    variant VARCHAR(50) NOT NULL, 
    created_at TIMESTAMPTZ DEFAULT now() 
);
```

### ユーザーA/Bテスト割り当て

```sql
CREATE TABLE user_ab_test ( 
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, 
    test_group_id INTEGER REFERENCES ab_test_group(id) ON DELETE CASCADE, 
    assigned_at TIMESTAMPTZ DEFAULT now(), 
    PRIMARY KEY (user_id, test_group_id) 
);
```



