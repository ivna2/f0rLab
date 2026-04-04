package fitnessclub.controller;

import fitnessclub.dto.RegisterRequest;
import fitnessclub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private final AuthService authService;

    @GetMapping("/auth")
    public String authRedirect() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String passwordChanged,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u043b\u043e\u0433\u0438\u043d \u0438\u043b\u0438 \u043f\u0430\u0440\u043e\u043b\u044c");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "\u0412\u044b \u0432\u044b\u0448\u043b\u0438 \u0438\u0437 \u0441\u0438\u0441\u0442\u0435\u043c\u044b");
        }
        if (passwordChanged != null) {
            model.addAttribute("successMessage", "\u041f\u0430\u0440\u043e\u043b\u044c \u0438\u0437\u043c\u0435\u043d\u0435\u043d. \u0412\u043e\u0439\u0434\u0438\u0442\u0435 \u0432 \u0441\u0438\u0441\u0442\u0435\u043c\u0443 \u0441 \u043d\u043e\u0432\u044b\u043c \u043f\u0430\u0440\u043e\u043b\u0435\u043c");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(@ModelAttribute("registerForm") RegisterRequest registerRequest, Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerForm") RegisterRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044f \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430. \u0422\u0435\u043f\u0435\u0440\u044c \u0432\u043e\u0439\u0434\u0438\u0442\u0435 \u043f\u043e\u0434 \u0441\u0432\u043e\u0438\u043c \u043b\u043e\u0433\u0438\u043d\u043e\u043c \u0438 \u043f\u0430\u0440\u043e\u043b\u0435\u043c.");
            return "redirect:/login";
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
            return "redirect:/register";
        }
    }
}
