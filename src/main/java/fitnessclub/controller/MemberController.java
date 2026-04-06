package fitnessclub.controller;

import fitnessclub.dto.EnrollMemberRequest;
import fitnessclub.dto.MemberRequest;
import fitnessclub.service.MemberService;
import fitnessclub.service.OperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final OperationsService operationsService;

    @GetMapping
    public String listMembers(Model model) {
        return "redirect:/members-page";
    }

    @PostMapping("/add")
    public String addMember(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam(required = false, defaultValue = "") String phone,
                            @RequestParam String subscriptionStartDate,
                            @RequestParam String subscriptionEndDate,
                            RedirectAttributes redirectAttributes) {
        try {
            operationsService.enrollMember(new EnrollMemberRequest(
                    name,
                    email,
                    phone,
                    LocalDate.parse(subscriptionStartDate),
                    LocalDate.parse(subscriptionEndDate)
            ));
            redirectAttributes.addFlashAttribute("successMessage", "Участник и абонемент добавлены");
        } catch (DateTimeParseException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректная дата начала или окончания абонемента");
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
