package com.example.fleamarketsystem.controller;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;
import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.ItemService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final ItemService itemService;
    private final AppOrderService appOrderService;

    public DashboardController(UserRepository userRepository, ItemService itemService, AppOrderService appOrderService) {
        this.userRepository = userRepository;
        this.itemService = itemService;
        this.appOrderService = appOrderService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole().equals("ADMIN")) {
            model.addAttribute("recentItems", itemService.getAllItems()); // Get all items for admin dashboard
            model.addAttribute("recentOrders", appOrderService.getAllOrders()); // Get all orders for admin dashboard
            return "admin_dashboard";
        } else { // USER (seller/buyer)
            // For now, just redirect to item list for regular users
            return "redirect:/items";
        }
    }
}