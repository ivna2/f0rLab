(function () {
    const root = document.documentElement;
    const storageKey = "fitnessclub-theme";

    function getStoredTheme() {
        const saved = localStorage.getItem(storageKey);
        if (saved === "light" || saved === "dark") {
            return saved;
        }
        return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    }

    function applyTheme(theme) {
        root.setAttribute("data-theme", theme);
        root.style.colorScheme = theme;
        document.querySelectorAll("[data-theme-toggle]").forEach((toggle) => {
            toggle.setAttribute("aria-label", theme === "dark" ? "Переключить на светлую тему" : "Переключить на темную тему");
        });
    }

    function applyPhoneMask(input) {
        const digits = input.value.replace(/\D/g, "");
        let normalized = digits;

        if (!normalized.length) {
            input.value = "+7";
            return;
        }
        if (normalized.startsWith("8")) {
            normalized = "7" + normalized.slice(1);
        }
        if (!normalized.startsWith("7")) {
            normalized = "7" + normalized;
        }
        normalized = normalized.slice(0, 11);

        let result = "+7";
        if (normalized.length > 1) {
            result += " (" + normalized.slice(1, 4);
        }
        if (normalized.length >= 4) {
            result += ")";
        }
        if (normalized.length > 4) {
            result += " " + normalized.slice(4, 7);
        }
        if (normalized.length > 7) {
            result += "-" + normalized.slice(7, 9);
        }
        if (normalized.length > 9) {
            result += "-" + normalized.slice(9, 11);
        }
        input.value = result;
    }

    function initPhoneInputs() {
        document.querySelectorAll("[data-phone-input]").forEach((input) => {
            input.addEventListener("focus", function () {
                if (!input.value.trim()) {
                    input.value = "+7";
                }
            });
            input.addEventListener("input", function () {
                applyPhoneMask(input);
            });
            input.addEventListener("blur", function () {
                if (input.value === "+7") {
                    input.value = "";
                }
            });
            if (input.value.trim()) {
                applyPhoneMask(input);
            }
        });
    }

    function initLessonFilter() {
        document.querySelectorAll("[data-lessons-source]").forEach((trainerSelect) => {
            const lessonSelect = document.getElementById(trainerSelect.getAttribute("data-lessons-source"));
            if (!lessonSelect) {
                return;
            }

            const placeholder = lessonSelect.querySelector("option[value='']");
            const options = Array.from(lessonSelect.querySelectorAll("option[data-trainer-id]"));

            function refresh() {
                const trainerId = trainerSelect.value;
                let visible = 0;
                options.forEach((option) => {
                    const match = trainerId && option.getAttribute("data-trainer-id") === trainerId;
                    option.hidden = !match;
                    if (match) {
                        visible += 1;
                    }
                });

                if (placeholder) {
                    placeholder.textContent = trainerId
                        ? (visible ? "Выберите слот" : "У этого тренера пока нет доступных слотов")
                        : "Сначала выберите тренера";
                }

                const selectedOption = lessonSelect.options[lessonSelect.selectedIndex];
                if (selectedOption && selectedOption.hidden) {
                    lessonSelect.value = "";
                }
            }

            trainerSelect.addEventListener("change", refresh);
            refresh();
        });
    }

    function initMemberEditor() {
        const form = document.querySelector("[data-member-edit-form]");
        if (!form) {
            return;
        }

        const idInput = document.getElementById("edit-id");
        const nameInput = document.getElementById("edit-name");
        const emailInput = document.getElementById("edit-email");
        const phoneInput = document.getElementById("edit-phone");

        function resetForm() {
            idInput.value = "";
            nameInput.value = "";
            emailInput.value = "";
            phoneInput.value = "";
        }

        document.querySelectorAll("[data-member-edit]").forEach((button) => {
            button.addEventListener("click", function () {
                idInput.value = button.getAttribute("data-member-id") || "";
                nameInput.value = button.getAttribute("data-member-name") || "";
                emailInput.value = button.getAttribute("data-member-email") || "";
                phoneInput.value = button.getAttribute("data-member-phone") || "";
                if (phoneInput.value) {
                    applyPhoneMask(phoneInput);
                }
                nameInput.focus();
            });
        });

        const resetButton = document.querySelector("[data-member-edit-reset]");
        if (resetButton) {
            resetButton.addEventListener("click", resetForm);
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        applyTheme(getStoredTheme());

        document.querySelectorAll("[data-theme-toggle]").forEach((toggle) => {
            toggle.addEventListener("click", function () {
                const nextTheme = root.getAttribute("data-theme") === "dark" ? "light" : "dark";
                localStorage.setItem(storageKey, nextTheme);
                applyTheme(nextTheme);
            });
        });

        document.querySelectorAll("[data-auto-dismiss]").forEach((node) => {
            window.setTimeout(() => node.remove(), 4500);
        });

        document.querySelectorAll("[data-confirm-message]").forEach((form) => {
            form.addEventListener("submit", function (event) {
                if (!window.confirm(form.getAttribute("data-confirm-message"))) {
                    event.preventDefault();
                }
            });
        });

        document.querySelectorAll("[data-password-toggle]").forEach((button) => {
            button.addEventListener("click", function () {
                const input = document.getElementById(button.getAttribute("data-password-toggle"));
                if (!input) {
                    return;
                }
                const reveal = input.type === "password";
                input.type = reveal ? "text" : "password";
                button.textContent = reveal ? "Спрятать пароль" : "Показать пароль";
            });
        });

        initPhoneInputs();
        initLessonFilter();
        initMemberEditor();
    });
})();
