package fitnessclub.controller;

import fitnessclub.dto.BookingFull;
import fitnessclub.dto.BookingRequest;
import fitnessclub.dto.LessonFull;
import fitnessclub.dto.LessonRequest;
import fitnessclub.dto.SubscriptionFull;
import fitnessclub.dto.SubscriptionRequest;
import fitnessclub.dto.TrainerRequest;
import fitnessclub.model.Lesson;
import fitnessclub.model.Member;
import fitnessclub.service.BookingService;
import fitnessclub.service.LessonService;
import fitnessclub.service.MemberService;
import fitnessclub.service.SubscriptionService;
import fitnessclub.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final LessonService lessonService;
    private final SubscriptionService subscriptionService;
    private final BookingService bookingService;

    @GetMapping("/members-page")
    public String membersPage(Model model) {
        model.addAttribute("members", memberService.getVisibleMembers());
        return "members";
    }

    @GetMapping("/trainers-page")
    public String trainers(Model model) {
        model.addAttribute("trainers", trainerService.getAll());
        return "trainers";
    }

    @PostMapping("/trainers/add")
    public String addTrainer(@RequestParam String name,
                             @RequestParam String specialization,
                             RedirectAttributes redirectAttributes) {
        try {
            trainerService.add(new TrainerRequest(name, specialization));
            redirectAttributes.addFlashAttribute("successMessage", "Тренер добавлен");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/trainers-page";
    }

    @PostMapping("/trainers/delete/{id}")
    public String deleteTrainer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            trainerService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Тренер удален");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/trainers-page";
    }

    @GetMapping("/lessons-page")
    public String lessons(Model model) {
        List<LessonFull> lessons = lessonService.getAll().stream()
                .map(l -> new LessonFull(l.getId(), l.getName(), l.getTrainer(), l.getDateTime(), l.getCapacity()))
                .toList();
        model.addAttribute("lessons", lessons);
        model.addAttribute("trainers", trainerService.getAll());
        return "lessons";
    }

    @PostMapping("/lessons/add")
    public String addLesson(@RequestParam String name,
                            @RequestParam Long trainerId,
                            @RequestParam int capacity,
                            @RequestParam String dateTime,
                            RedirectAttributes redirectAttributes) {
        try {
            lessonService.add(new LessonRequest(name, trainerId, LocalDateTime.parse(dateTime), capacity));
            redirectAttributes.addFlashAttribute("successMessage", "Занятие добавлено");
        } catch (DateTimeParseException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректная дата или время занятия");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/lessons-page";
    }

    @PostMapping("/lessons/delete/{id}")
    public String deleteLesson(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            lessonService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Занятие удалено");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/lessons-page";
    }

    @GetMapping("/bookings-page")
    public String bookings(Model model) {
        List<BookingFull> bookings = bookingService.getAll().stream()
                .map(b -> {
                    Member member = b.getMember();
                    Lesson lesson = b.getLesson();
                    LessonFull lessonFull = lesson == null
                            ? null
                            : new LessonFull(lesson.getId(), lesson.getName(), lesson.getTrainer(), lesson.getDateTime(), lesson.getCapacity());
                    return new BookingFull(b.getId(), member, lessonFull, b.getDate());
                })
                .toList();

        List<LessonFull> lessons = lessonService.getAll().stream()
                .map(l -> new LessonFull(l.getId(), l.getName(), l.getTrainer(), l.getDateTime(), l.getCapacity()))
                .toList();

        model.addAttribute("bookings", bookings);
        model.addAttribute("members", memberService.getVisibleMembers());
        model.addAttribute("trainers", trainerService.getAll());
        model.addAttribute("lessons", lessons);
        return "bookings";
    }

    @PostMapping("/bookings/add")
    public String addBooking(@RequestParam Long memberId,
                             @RequestParam Long lessonId,
                             RedirectAttributes redirectAttributes) {
        try {
            bookingService.add(new BookingRequest(memberId, lessonId, null));
            redirectAttributes.addFlashAttribute("successMessage", "Бронирование добавлено");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/bookings-page";
    }

    @PostMapping("/bookings/delete/{id}")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Бронирование отменено");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/bookings-page";
    }

    @GetMapping("/subscriptions-page")
    public String subscriptions(Model model) {
        List<SubscriptionFull> subscriptions = subscriptionService.getAll().stream()
                .map(s -> new SubscriptionFull(s.getId(), s.getMember(), s.getStartDate(), s.getEndDate()))
                .toList();
        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("members", memberService.getVisibleMembers());
        return "subscriptions";
    }

    @PostMapping("/subscriptions/add")
    public String addSubscription(@RequestParam Long memberId,
                                  @RequestParam String startDate,
                                  @RequestParam String endDate,
                                  RedirectAttributes redirectAttributes) {
        try {
            subscriptionService.add(new SubscriptionRequest(memberId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
            redirectAttributes.addFlashAttribute("successMessage", "Абонемент добавлен");
        } catch (DateTimeParseException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректная дата начала или окончания абонемента");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/subscriptions-page";
    }

    @PostMapping("/subscriptions/delete/{id}")
    public String deleteSubscription(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            subscriptionService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Абонемент удален");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        }
        return "redirect:/subscriptions-page";
    }
}
