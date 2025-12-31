package com.example.fleamarketsystem.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.service.UserService;

@Controller
@RequestMapping("/register")
public class RegisterController {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public RegisterController(UserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping
	public String registerForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping
	public String register(@ModelAttribute User user, Model model) {
		// メールアドレスの重複チェック
		if (userService.getUserByEmail(user.getEmail()).isPresent()) {
			model.addAttribute("error", "このメールアドレスは既に登録されています。");
			model.addAttribute("user", user);
			return "register";
		}

		// パスワードをエンコード（{noop}プレフィックスを使用）
		String encodedPassword = "{noop}" + user.getPassword();
		user.setPassword(encodedPassword);

		// デフォルト値を設定
		user.setEnabled(true);
		user.setBanned(false);

		// ユーザーを保存
		userService.saveUser(user);

		// 登録成功後、ログインページにリダイレクト
		return "redirect:/login?registered";
	}
}

