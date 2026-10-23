package com.example.fleamarketsystem.security;

import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.fleamarketsystem.entity.Admin;
import com.example.fleamarketsystem.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

	private final AdminRepository adminRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// usernameはメールアドレス
		Admin admin = adminRepository.findByEmailIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));

		if (!admin.isActive()) {
			throw new DisabledException("Admin account is disabled");
		}

		// 管理者は固定でROLE_ADMINを持つ
		return new org.springframework.security.core.userdetails.User(
				admin.getEmail(),
				admin.getPassword(),
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}
}

