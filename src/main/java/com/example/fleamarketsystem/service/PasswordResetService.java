package com.example.fleamarketsystem.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;

@Service
public class PasswordResetService {

	private final UserRepository userRepository;
	private final EmailService emailService;

	public PasswordResetService(UserRepository userRepository, EmailService emailService) {
		this.userRepository = userRepository;
		this.emailService = emailService;
	}

	@Transactional
	public void requestPasswordReset(String email) {
		Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
		
		// セキュリティのため、存在しないメールアドレスでも成功メッセージを返す
		if (userOpt.isEmpty()) {
			return;
		}

		User user = userOpt.get();
		
		// トークンを生成
		String token = UUID.randomUUID().toString();
		LocalDateTime expiry = LocalDateTime.now().plusHours(24);

		// トークンと有効期限を保存
		user.setPasswordResetToken(token);
		user.setPasswordResetTokenExpiry(expiry);
		userRepository.save(user);

		// メール送信（開発環境ではメール設定が不完全でも動作するように例外をスローしない）
		emailService.sendPasswordResetEmail(email, token);
	}

	public boolean validateToken(String token) {
		Optional<User> userOpt = userRepository.findByPasswordResetToken(token);
		
		if (userOpt.isEmpty()) {
			return false;
		}

		User user = userOpt.get();
		
		// トークンの有効期限をチェック
		if (user.getPasswordResetTokenExpiry() == null || 
			user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
			return false;
		}

		return true;
	}

	@Transactional
	public void resetPassword(String token, String newPassword) {
		Optional<User> userOpt = userRepository.findByPasswordResetToken(token);
		
		if (userOpt.isEmpty()) {
			throw new IllegalArgumentException("Invalid or expired token");
		}

		User user = userOpt.get();
		
		// トークンの有効期限をチェック
		if (user.getPasswordResetTokenExpiry() == null || 
			user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Token has expired");
		}

		// パスワードをエンコード（{noop}プレフィックスを使用）
		String encodedPassword = "{noop}" + newPassword;
		user.setPassword(encodedPassword);

		// トークンをクリア
		user.setPasswordResetToken(null);
		user.setPasswordResetTokenExpiry(null);
		
		userRepository.save(user);
	}
}

