package com.example.fleamarketsystem.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fleamarketsystem.entity.Admin;
import com.example.fleamarketsystem.repository.AdminRepository;
import com.example.fleamarketsystem.validation.PasswordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordValidator passwordValidator;

	/**
	 * すべての管理者を取得
	 */
	public List<Admin> getAllAdmins() {
		return adminRepository.findAll();
	}

	/**
	 * IDで管理者を取得
	 */
	public Optional<Admin> getAdminById(Long id) {
		return adminRepository.findById(id);
	}

	/**
	 * メールアドレスで管理者を取得
	 */
	public Optional<Admin> getAdminByEmail(String email) {
		return adminRepository.findByEmailIgnoreCase(email);
	}

	/**
	 * 管理者を作成
	 * 
	 * @param admin 作成する管理者情報
	 * @throws IllegalArgumentException パスワードが要件を満たさない場合、またはメールアドレスが既に存在する場合
	 */
	@Transactional
	public Admin createAdmin(Admin admin) {
		// メールアドレスの重複チェック
		if (adminRepository.findByEmailIgnoreCase(admin.getEmail()).isPresent()) {
			throw new IllegalArgumentException("このメールアドレスは既に使用されています。");
		}

		// パスワードバリデーション
		if (!passwordValidator.validateAdminPassword(admin.getPassword())) {
			throw new IllegalArgumentException(passwordValidator.getAdminPasswordRequirements());
		}

		// パスワードをエンコード
		admin.setPassword(passwordEncoder.encode(admin.getPassword()));
		admin.setCreatedAt(LocalDateTime.now());
		admin.setUpdatedAt(LocalDateTime.now());

		return adminRepository.save(admin);
	}

	/**
	 * 管理者を更新
	 * 
	 * @param admin 更新する管理者情報
	 * @throws IllegalArgumentException パスワードが要件を満たさない場合
	 */
	@Transactional
	public Admin updateAdmin(Admin admin) {
		Admin existingAdmin = adminRepository.findById(admin.getId())
				.orElseThrow(() -> new IllegalArgumentException("管理者が見つかりません。"));

		// メールアドレスの変更がある場合、重複チェック
		if (!existingAdmin.getEmail().equalsIgnoreCase(admin.getEmail())) {
			if (adminRepository.findByEmailIgnoreCase(admin.getEmail()).isPresent()) {
				throw new IllegalArgumentException("このメールアドレスは既に使用されています。");
			}
			existingAdmin.setEmail(admin.getEmail());
		}

		// パスワードの変更がある場合、バリデーションとエンコード
		if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
			if (!passwordValidator.validateAdminPassword(admin.getPassword())) {
				throw new IllegalArgumentException(passwordValidator.getAdminPasswordRequirements());
			}
			existingAdmin.setPassword(passwordEncoder.encode(admin.getPassword()));
		}

		existingAdmin.setName(admin.getName());
		existingAdmin.setActive(admin.isActive());
		existingAdmin.setUpdatedAt(LocalDateTime.now());

		return adminRepository.save(existingAdmin);
	}

	/**
	 * 管理者を削除
	 */
	@Transactional
	public void deleteAdmin(Long id) {
		adminRepository.deleteById(id);
	}

	/**
	 * 最終ログイン時刻を更新
	 */
	@Transactional
	public void updateLastLogin(Long id) {
		Admin admin = adminRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("管理者が見つかりません。"));
		admin.setLastLoginAt(LocalDateTime.now());
		adminRepository.save(admin);
	}
}

