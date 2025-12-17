-- パスワードリセット機能用カラムを追加
ALTER TABLE public.users
  ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255),
  ADD COLUMN IF NOT EXISTS password_reset_token_expiry TIMESTAMP;

