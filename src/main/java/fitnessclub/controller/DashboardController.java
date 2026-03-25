package fitnessclub.controller;

import fitnessclub.dto.AdminDashboardResponse;
import fitnessclub.dto.ChangePasswordRequest;
import fitnessclub.dto.CreateAdminRequest;
import fitnessclub.dto.MemberDashboardResponse;
import fitnessclub.model.Role;
import fitnessclub.service.AdminNotificationService;
import fitnessclub.service.AuthService;
import fitnessclub.service.MemberPortalService;
import fitnessclub.service.SecurityAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final SecurityAccessService securityAccessService;
    private final MemberPortalService memberPortalService;
    private final AuthService authService;
    private final AdminNotificationService adminNotificationService;

    @GetMapping("/")
    public String root() {
        try {
            var user = securityAccessService.getCurrentUser();
            return user.getRoles().contains(Role.ADMIN) ? "redirect:/admin/dashboard" : "redirect:/member/dashboard";
        } catch (Exception ignored) {
            return "redirect:/login";
        }
    }

    @GetMapping("/post-login")
    public String postLogin() {
        var user = securityAccessService.getCurrentUser();
        return user.getRoles().contains(Role.ADMIN) ? "redirect:/admin/dashboard" : "redirect:/member/dashboard";
    }

    @GetMapping("/member/dashboard")
    public String memberDashboard(Model model) {
        MemberDashboardResponse dashboard = memberPortalService.getDashboard();
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("changePasswordForm", new ChangePasswordRequest("", "", ""));
        model.addAttribute("contactPhone", "+7 (495) 555-10-10");
        model.addAttribute("contactEmail", "support@fitnessclub.local");
        return "member-dashboard";
    }

    @PostMapping("/member/photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile photo, RedirectAttributes redirectAttributes) {
        try {
            memberPortalService.updatePhoto(photo);
            redirectAttributes.addFlashAttribute("successMessage", "Фото обновлено");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/member/dashboard";
    }

    @PostMapping("/member/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            authService.changeCurrentUserPassword(new ChangePasswordRequest(currentPassword, newPassword, confirmPassword));
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?passwordChanged";
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
            return "redirect:/member/dashboard";
        }
    }

    @PostMapping("/member/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            memberPortalService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("successMessage", "Запись отменена");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/member/dashboard";
    }

    @PostMapping("/member/bookings/{bookingId}/reschedule")
    public String rescheduleBooking(@PathVariable Long bookingId,
                                    @RequestParam Long newLessonId,
                                    RedirectAttributes redirectAttributes) {
        try {
            memberPortalService.rescheduleBooking(bookingId, newLessonId);
            redirectAttributes.addFlashAttribute("successMessage", "Запись перенесена");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/member/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        AdminDashboardResponse dashboard = new AdminDashboardResponse(
                authService.getCurrentUser().login(),
                adminNotificationService.getLatest()
        );
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("createAdminForm", new CreateAdminRequest("", "", "", "", ""));
        return "admin-dashboard";
    }

    @GetMapping("/admin/admins")
    public String adminsPage(Model model) {
        model.addAttribute("admins", authService.getAdmins());
        return "admins";
    }

    @PostMapping("/admin/create")
    public String createAdmin(CreateAdminRequest request, RedirectAttributes redirectAttributes) {
        try {
            authService.createAdmin(request);
            redirectAttributes.addFlashAttribute("successMessage", "Новый администратор создан");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/notifications/clear")
    public String clearNotifications(RedirectAttributes redirectAttributes) {
        adminNotificationService.clearAll();
        redirectAttributes.addFlashAttribute("successMessage", "\u041f\u0430\u043d\u0435\u043b\u044c \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439 \u043e\u0447\u0438\u0449\u0435\u043d\u0430");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/admins/{id}/delete")
    public String deleteAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            authService.deleteAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage", "Администратор удален");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/admin/admins";
    }

    @GetMapping("/member-photos/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> memberPhoto(@PathVariable String filename) {
        Path path = Path.of("uploads", "member-photos", filename);
        Resource resource = new PathResource(path);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String contentType = Files.probeContentType(path);
            if (contentType != null) {
                mediaType = MediaType.parseMediaType(contentType);
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}
