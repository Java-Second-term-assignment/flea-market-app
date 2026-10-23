package com.example.fleamarketsystem.validation;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

	// 管理者用: 最小12文字、大文字・小文字・数字・記号必須
	private static final Pattern ADMIN_PASSWORD_PATTERN = Pattern
			.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{12,}$");

	// 一般ユーザー用: 最小8文字、大文字・小文字・数字必須
	private static final Pattern USER_PASSWORD_PATTERN = Pattern
			.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$");

	/**
	 * 管理者用パスワードを検証
	 * 
	 * @param password 検証するパスワード
	 * @return 有効な場合はtrue
	 */
	public boolean validateAdminPassword(String password) {
		if (password == null || password.isEmpty()) {
			return false;
		}
		return ADMIN_PASSWORD_PATTERN.matcher(password).matches();
	}

	/**
	 * 一般ユーザー用パスワードを検証
	 * 
	 * @param password 検証するパスワード
	 * @return 有効な場合はtrue
	 */
	public boolean validateUserPassword(String password) {
		if (password == null || password.isEmpty()) {
			return false;
		}
		return USER_PASSWORD_PATTERN.matcher(password).matches();
	}

	/**
	 * 管理者用パスワードの要件を説明するメッセージを取得
	 * 
	 * @return 要件説明メッセージ
	 */
	public String getAdminPasswordRequirements() {
		return "パスワードは12文字以上で、大文字・小文字・数字・記号(@$!%*?&#)を含む必要があります。";
	}

	/**
	 * 一般ユーザー用パスワードの要件を説明するメッセージを取得
	 * 
	 * @return 要件説明メッセージ
	 */
	public String getUserPasswordRequirements() {
		return "パスワードは8文字以上で、大文字・小文字・数字を含む必要があります。";
	}
}

