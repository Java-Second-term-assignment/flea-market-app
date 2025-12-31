// src/main/java/com/example/fleamarketsystem/controller/HomeController.java
package com.example.fleamarketsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String home() {
		// 常に商品一覧にリダイレクト
		// 管理者は /admin/login からログインするため、ここでは判定不要
		return "redirect:/items";
	}
}
