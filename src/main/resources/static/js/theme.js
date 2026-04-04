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

<<<<<<< HEAD
    function initSimpleEditor(config) {
        const form = document.querySelector(config.formSelector);
        if (!form) {
            return;
        }

        const fields = Object.entries(config.fields).map(([key, selector]) => [key, document.querySelector(selector)]);

        function resetForm() {
            fields.forEach(([_, node]) => {
                if (!node) {
                    return;
                }
                if (node.tagName === "SELECT") {
                    node.value = "";
                } else {
                    node.value = "";
                }
            });
            if (typeof config.onReset === "function") {
                config.onReset(form);
            }
        }

        document.querySelectorAll(config.buttonSelector).forEach((button) => {
            button.addEventListener("click", function () {
                fields.forEach(([key, node]) => {
                    if (!node) {
                        return;
                    }
                    node.value = button.getAttribute(config.dataMap[key]) || "";
                });
                if (typeof config.onFill === "function") {
                    config.onFill(form, button);
                }
                const firstField = fields.find(([key]) => key !== "id");
                if (firstField && firstField[1]) {
                    firstField[1].focus();
                }
            });
        });

        const resetButton = document.querySelector(config.resetSelector);
        if (resetButton) {
            resetButton.addEventListener("click", resetForm);
        }
    }

=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
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
<<<<<<< HEAD
        initSimpleEditor({
            formSelector: "[data-trainer-edit-form]",
            buttonSelector: "[data-trainer-edit]",
            resetSelector: "[data-trainer-edit-reset]",
            fields: {
                id: "#trainer-edit-id",
                name: "#trainer-edit-name",
                specialization: "#trainer-edit-specialization"
            },
            dataMap: {
                id: "data-trainer-id",
                name: "data-trainer-name",
                specialization: "data-trainer-specialization"
            }
        });
        initSimpleEditor({
            formSelector: "[data-lesson-edit-form]",
            buttonSelector: "[data-lesson-edit]",
            resetSelector: "[data-lesson-edit-reset]",
            fields: {
                id: "#lesson-edit-id",
                name: "#lesson-edit-name",
                capacity: "#lesson-edit-capacity",
                dateTime: "#lesson-edit-dateTime",
                trainerId: "#lesson-edit-trainer"
            },
            dataMap: {
                id: "data-lesson-id",
                name: "data-lesson-name",
                capacity: "data-lesson-capacity",
                dateTime: "data-lesson-datetime",
                trainerId: "data-lesson-trainer-id"
            }
        });
        initSimpleEditor({
            formSelector: "[data-subscription-edit-form]",
            buttonSelector: "[data-subscription-edit]",
            resetSelector: "[data-subscription-edit-reset]",
            fields: {
                id: "#subscription-edit-id",
                memberId: "#subscription-edit-member",
                startDate: "#subscription-edit-start",
                endDate: "#subscription-edit-end"
            },
            dataMap: {
                id: "data-subscription-id",
                memberId: "data-subscription-member-id",
                startDate: "data-subscription-start-date",
                endDate: "data-subscription-end-date"
            }
        });
        initSimpleEditor({
            formSelector: "[data-booking-edit-form]",
            buttonSelector: "[data-booking-edit]",
            resetSelector: "[data-booking-edit-reset]",
            fields: {
                id: "#booking-edit-id",
                memberId: "#booking-edit-member",
                trainerId: "#booking-edit-trainer",
                lessonId: "#booking-edit-lesson"
            },
            dataMap: {
                id: "data-booking-id",
                memberId: "data-booking-member-id",
                trainerId: "data-booking-trainer-id",
                lessonId: "data-booking-lesson-id"
            },
            onFill: function () {
                document.getElementById("booking-edit-trainer").dispatchEvent(new Event("change"));
            }
        });
        initSimpleEditor({
            formSelector: "[data-admin-edit-form]",
            buttonSelector: "[data-admin-edit]",
            resetSelector: "[data-admin-edit-reset]",
            fields: {
                id: "#admin-edit-id",
                login: "#admin-edit-login",
                name: "#admin-edit-name",
                email: "#admin-edit-email",
                phone: "#admin-edit-phone",
                password: "#admin-edit-password"
            },
            dataMap: {
                id: "data-admin-id",
                login: "data-admin-login",
                name: "data-admin-name",
                email: "data-admin-email",
                phone: "data-admin-phone",
                password: "data-admin-password"
            },
            onFill: function (form, button) {
                const adminId = button.getAttribute("data-admin-id") || "0";
                form.action = "/admin/admins/" + adminId + "/update";
            },
            onReset: function (form) {
                form.action = "/admin/admins/0/update";
            }
        });
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
    });
})();
