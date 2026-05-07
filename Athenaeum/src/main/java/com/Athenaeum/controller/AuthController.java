package com.Athenaeum.controller;

import com.Athenaeum.entity.User;
import com.Athenaeum.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  private AuthService authService;

  // Spring Security handles POST /auth/login; this only renders the page
  @GetMapping("/login")
  public String loginPage(Model model,
                          @RequestParam(required = false) String error,
                          @RequestParam(required = false) String logout,
                          @RequestParam(required = false) String registered) {
    if (error != null) {
      model.addAttribute("error", "Invalid username or password");
    }
    if (logout != null) {
      model.addAttribute("message", "Logged out successfully");
    }
    if (registered != null) {
      model.addAttribute("message", "Registration successful! Please sign in.");
    }
    return "login";
  }

  @GetMapping("/register")
  public String registerPage() {
    return "register";
  }

  @PostMapping("/register")
  public String register(@RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String firstName,
                         @RequestParam String lastName,
                         Model model) {
    try {
      User user = authService.register(username, email, password, firstName, lastName);
      // Redirect to login so Spring Security processes authentication later
      return "redirect:/auth/login?registered=true";
    } catch (Exception e) {
      model.addAttribute("error", "Registration failed: " + e.getMessage());
      return "register";
    }
  }
}
