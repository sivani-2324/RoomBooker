package com.roombooker.roombooker;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("userName") == null) {
            return "redirect:/";
        }

        return "dashboard";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        User user = userRepository.findByEmail(email);
        boolean passwordMatches = false;

        if (user != null && user.getPassword() != null) {
            String storedPassword = user.getPassword();

            if (storedPassword.startsWith("$2a$")
                    || storedPassword.startsWith("$2b$")
                    || storedPassword.startsWith("$2y$")) {

                passwordMatches =
                        passwordEncoder.matches(password, storedPassword);

            } else if (storedPassword.equals(password)) {

                String encodedPassword =
                        passwordEncoder.encode(password);

                int updated = userRepository.updatePasswordByEmail(
                        email, encodedPassword);

                passwordMatches = updated > 0;
            }
        }

        if (user != null && passwordMatches) {
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());

            if ("Admin".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin-dashboard";
            }

            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    @GetMapping("/add-user")
    public String addUserPage() {
        return "add-user";
    }

    @PostMapping("/add-user")
    public String addUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Email already registered.");
            return "add-user";
        }

        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("Student");
        user.setStatus("Active");

        userRepository.saveAndFlush(user);

        model.addAttribute(
                "success",
                "Account created successfully! Please login.");

        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}