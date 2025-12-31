// src/main/java/com/example/fleamarketsystem/entity/User.java
package com.example.fleamarketsystem.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name; // 表示名

	@Column(unique = true, nullable = false)
	private String email;

	@Column(name = "real_name")
	private String realName; // 本名

	@Column(name = "furigana")
	private String furigana; // フリガナ

	@Column(name = "phone_number")
	private String phoneNumber; // 電話番号

	@Column(nullable = false)
	private String password;

	@Column(name = "line_notify_token")
	private String lineNotifyToken;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "address")
	private String address;

	@Column(name = "age")
	private Integer age;

	@Column(name = "gender")
	private String gender; // "MALE", "FEMALE", "OTHER", or null

	@Column(nullable = false)
	private boolean enabled = true;

	@Column(nullable = false)
	private boolean banned = false;

	@Column(name = "ban_reason")
	private String banReason;

	@Column(name = "banned_at")
	private LocalDateTime bannedAt;

	@Column(name = "banned_by_admin_id")
	private Integer bannedByAdminId;

	@Column(name = "password_reset_token")
	private String passwordResetToken;

	@Column(name = "password_reset_token_expiry")
	private LocalDateTime passwordResetTokenExpiry;
}
