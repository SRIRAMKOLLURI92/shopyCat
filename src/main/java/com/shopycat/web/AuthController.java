package com.shopycat.web;

import com.shopycat.model.AppUser;
import com.shopycat.model.Role;
import com.shopycat.repo.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class AuthController {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    AuthController(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/register")
    String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    String register(@RequestParam String email,
                    @RequestParam String password,
                    @RequestParam String displayName,
                    @RequestParam(required = false) String phone,
                    @RequestParam Role role,
                    Model model) {
        String error = null;
        if (Forms.blank(email) || !email.contains("@")) {
            error = "Please enter a valid email.";
        } else if (password == null || password.length() < 8) {
            error = "Password must be at least 8 characters.";
        } else if (Forms.blank(displayName)) {
            error = "Please choose a display name. This is the only name other people see.";
        } else if (users.existsByEmailIgnoreCase(email.trim())) {
            error = "An account with this email already exists.";
        }
        if (error != null) {
            model.addAttribute("error", error);
            return "register";
        }
        users.save(new AppUser(email.trim().toLowerCase(), passwordEncoder.encode(password),
                displayName.trim(), Forms.trimOrNull(phone), role));
        return "redirect:/login?registered";
    }
}
