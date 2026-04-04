package fitnessclub.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@ControllerAdvice(assignableTypes = {PageController.class, MemberController.class, AuthPageController.class})
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return buildRedirect(request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleValidationError(IllegalStateException ex,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return buildRedirect(request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                               RedirectAttributes redirectAttributes,
                                               HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c: \u043e\u043d\u0430 \u0435\u0449\u0435 \u0441\u0432\u044f\u0437\u0430\u043d\u0430 \u0441 \u0434\u0440\u0443\u0433\u0438\u043c\u0438 \u0434\u0430\u043d\u043d\u044b\u043c\u0438");
        return buildRedirect(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericError(Exception ex,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "\u041f\u0440\u043e\u0438\u0437\u043e\u0448\u043b\u0430 \u0441\u0438\u0441\u0442\u0435\u043c\u043d\u0430\u044f \u043e\u0448\u0438\u0431\u043a\u0430: " + ex.getMessage());
        return buildRedirect(request);
    }

    private String buildRedirect(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "redirect:/";
        }

        try {
            URI uri = URI.create(referer);
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return "redirect:/";
            }
            String query = uri.getQuery();
            return query == null || query.isBlank()
                    ? "redirect:" + path
                    : "redirect:" + path + "?" + query;
        } catch (IllegalArgumentException ex) {
            return "redirect:/";
        }
    }
}
