package com.example.fleamarketsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.from:support@fleamarket.com}")
	private String fromEmail;

	@Value("${app.mail.from-name:サポートチーム}")
	private String fromName;

	@Value("${app.base-url:http://localhost:8080}")
	private String baseUrl;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendPasswordResetEmail(String email, String token) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(email);
		message.setSubject("パスワードリセットのご案内");
		
		// 環境変数からベースURLを取得（本番環境ではhttps://yourdomain.comなど）
		String resetUrl = baseUrl + "/reset-password?token=" + token;
		String emailBody = String.format(
			"%s 様\n\n" +
			"パスワードリセットのリクエストを受け付けました。\n\n" +
			"以下のリンクをクリックして、新しいパスワードを設定してください。\n" +
			"このリンクは24時間有効です。\n\n" +
			"%s\n\n" +
			"もしこのリクエストをしていない場合は、このメールを無視してください。\n\n" +
			"---\n" +
			"%s",
			email, resetUrl, fromName
		);
		
		message.setText(emailBody);
		
		try {
			mailSender.send(message);
			System.out.println("✓ Password reset email sent successfully to: " + email);
			System.out.println("  Reset URL: " + resetUrl);
		} catch (Exception e) {
			// 開発環境ではログ出力（メール設定が不完全な場合があるため）
			System.err.println("✗ Failed to send password reset email to " + email);
			System.err.println("  Error: " + e.getMessage());
			System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			System.err.println("【開発用】パスワードリセットリンク");
			System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			System.err.println(resetUrl);
			System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			// 開発環境では例外をスローしない（メール設定が不完全でも動作するように）
			// 本番環境では例外を再スローするか、適切なログに記録
		}
	}
}

