package com.example.fleamarketsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.fleamarketsystem.security.AdminUserDetailsService;
import com.example.fleamarketsystem.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final CustomUserDetailsService customUserDetailsService;
	private final AdminUserDetailsService adminUserDetailsService;

	public SecurityConfig(CustomUserDetailsService customUserDetailsService,
			AdminUserDetailsService adminUserDetailsService) {
		this.customUserDetailsService = customUserDetailsService;
		this.adminUserDetailsService = adminUserDetailsService;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// {bcrypt},{noop} など委譲エンコーダ
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	/**
	 * 管理者用のSecurityFilterChain
	 * /admin/** へのアクセスを管理者認証のみで許可
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/admin/**")
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/admin/login").permitAll()
						.requestMatchers("/admin/**").authenticated())
				.userDetailsService(adminUserDetailsService)
				.formLogin(form -> form
						.loginPage("/admin/login")
						.loginProcessingUrl("/admin/login")
						.defaultSuccessUrl("/admin/users", true)
						.failureUrl("/admin/login?error")
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/admin/logout")
						.logoutSuccessUrl("/admin/login?logout")
						.permitAll())
				.csrf(Customizer.withDefaults());

		return http.build();
	}

	/**
	 * 一般ユーザー用のSecurityFilterChain
	 * 管理者以外のすべてのリクエストを処理
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/",
								"/login",
								"/register",
								"/forgot-password",
								"/reset-password",
								"/css/**", "/js/**", "/images/**", "/webjars/**")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/items", "/items/**").permitAll()
						.anyRequest().authenticated())
				.userDetailsService(customUserDetailsService)
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/items", true)
						.permitAll())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, authException) -> {
							// 未認証時のアクセス拒否を商品一覧にリダイレクト
							response.sendRedirect("/items");
						}))
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/items")
						.permitAll())
				.csrf(Customizer.withDefaults());

		return http.build();
	}
}
