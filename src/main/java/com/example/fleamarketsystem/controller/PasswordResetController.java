package com.example.fleamarketsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fleamarketsystem.service.PasswordResetService;

@Controller
@RequestMapping
public class PasswordResetController {

	private final PasswordResetService passwordResetService;

	public PasswordResetController(PasswordResetService passwordResetService) {
		this.passwordResetService = passwordResetService;
	}

	@GetMapping("/forgot-password")
	public String forgotPasswordForm() {
		return "forgot-password";
	}

	@PostMapping("/forgot-password")
	public String requestPasswordReset(@RequestParam String email, Model model) {
		passwordResetService.requestPasswordReset(email);
		// セキュリティのため、存在しないメールアドレスでも成功メッセージを表示
		return "redirect:/forgot-password?success";
	}

	@GetMapping("/reset-password")
	public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
		if (token == null || token.isEmpty()) {
			model.addAttribute("error", "リセットトークンが指定されていません。");
			return "reset-password";
		}

		if (!passwordResetService.validateToken(token)) {
			model.addAttribute("error", "無効または期限切れのトークンです。");
			return "reset-password";
		}

		model.addAttribute("token", token);
		return "reset-password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password,
			@RequestParam String confirmPassword, Model model) {
		
		if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "パスワードが一致しません。");
			model.addAttribute("token", token);
			return "reset-password";
		}

		if (password.length() < 4) {
			model.addAttribute("error", "パスワードは4文字以上で入力してください。");
			model.addAttribute("token", token);
			return "reset-password";
		}

		try {
			passwordResetService.resetPassword(token, password);
			return "redirect:/login?passwordReset";
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "reset-password";
		}
	}
}

