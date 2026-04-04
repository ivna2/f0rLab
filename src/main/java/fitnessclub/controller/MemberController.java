package fitnessclub.controller;

import fitnessclub.dto.MemberRequest;
import fitnessclub.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public String listMembers(Model model) {
        return "redirect:/members-page";
    }

    @PostMapping("/add")
    public String addMember(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam(required = false, defaultValue = "") String phone,
                            RedirectAttributes redirectAttributes) {
        try {
            memberService.add(new MemberRequest(name, email, phone));
            redirectAttributes.addFlashAttribute("successMessage", "Участник добавлен");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/members-page";
    }

    @PostMapping("/update")
    public String updateMember(@RequestParam Long id,
                               @RequestParam String name,
                               @RequestParam String email,
                               @RequestParam(required = false, defaultValue = "") String phone,
                               RedirectAttributes redirectAttributes) {
        try {
            memberService.update(id, new MemberRequest(name, email, phone));
            redirectAttributes.addFlashAttribute("successMessage", "Участник обновлен");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/members-page";
    }

    @PostMapping("/delete")
    public String deleteMember(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            memberService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Участник удален");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/members-page";
    }
}
