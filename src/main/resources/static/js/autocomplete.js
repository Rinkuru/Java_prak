(function () {
    "use strict";

    var MIN_QUERY_LENGTH = 2;
    var DEBOUNCE_DELAY_MS = 250;

    function buildSuggestionUrl(endpoint, query) {
        var separator = endpoint.indexOf("?") === -1 ? "?" : "&";
        return endpoint + separator + "q=" + encodeURIComponent(query);
    }

    function dispatchFieldEvent(input, eventName) {
        var event;
        if (typeof Event === "function") {
            event = new Event(eventName, {bubbles: true});
        } else {
            event = document.createEvent("Event");
            event.initEvent(eventName, true, false);
        }
        input.dispatchEvent(event);
    }

    function initAutocomplete(input, index) {
        var endpoint = input.getAttribute("data-suggestions-url");
        var wrapper = document.createElement("div");
        var menu = document.createElement("div");
        var timerId = null;
        var activeRequest = null;
        var activeIndex = -1;
        var lastRequestId = 0;
        var skipOwnInputEvent = false;

        wrapper.className = "autocomplete-wrapper";
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        menu.className = "autocomplete-menu";
        menu.id = (input.id || "autocomplete-" + index) + "-suggestions";
        menu.setAttribute("role", "listbox");
        menu.hidden = true;
        wrapper.appendChild(menu);

        input.setAttribute("aria-autocomplete", "list");
        input.setAttribute("aria-expanded", "false");
        input.setAttribute("aria-controls", menu.id);

        function hideMenu() {
            menu.hidden = true;
            menu.innerHTML = "";
            activeIndex = -1;
            input.setAttribute("aria-expanded", "false");
            input.removeAttribute("aria-activedescendant");
        }

        function optionButtons() {
            return menu.querySelectorAll(".autocomplete-option");
        }

        function updateActiveOption() {
            var buttons = optionButtons();
            Array.prototype.forEach.call(buttons, function (button, buttonIndex) {
                var isActive = buttonIndex === activeIndex;
                button.classList.toggle("is-active", isActive);
                button.setAttribute("aria-selected", isActive ? "true" : "false");
                if (isActive) {
                    input.setAttribute("aria-activedescendant", button.id);
                }
            });

            if (activeIndex < 0) {
                input.removeAttribute("aria-activedescendant");
            }
        }

        function chooseSuggestion(value) {
            input.value = value;
            hideMenu();

            skipOwnInputEvent = true;
            dispatchFieldEvent(input, "input");
            dispatchFieldEvent(input, "change");
            input.focus();
        }

        function showSuggestions(suggestions) {
            hideMenu();

            if (!suggestions || suggestions.length === 0) {
                return;
            }

            suggestions.forEach(function (suggestion, suggestionIndex) {
                var option = document.createElement("button");
                option.className = "autocomplete-option";
                option.id = menu.id + "-option-" + suggestionIndex;
                option.type = "button";
                option.setAttribute("role", "option");
                option.setAttribute("aria-selected", "false");
                option.textContent = suggestion;
                option.addEventListener("mousedown", function (event) {
                    event.preventDefault();
                    chooseSuggestion(suggestion);
                });
                option.addEventListener("click", function (event) {
                    event.preventDefault();
                    chooseSuggestion(suggestion);
                });
                menu.appendChild(option);
            });

            menu.hidden = false;
            input.setAttribute("aria-expanded", "true");
        }

        function requestSuggestions(query) {
            var requestId = ++lastRequestId;
            var request = new XMLHttpRequest();

            if (activeRequest && activeRequest.readyState !== XMLHttpRequest.DONE) {
                activeRequest.abort();
            }
            activeRequest = request;

            request.open("GET", buildSuggestionUrl(endpoint, query), true);
            request.setRequestHeader("Accept", "application/json");
            request.onreadystatechange = function () {
                if (request.readyState !== XMLHttpRequest.DONE || requestId !== lastRequestId) {
                    return;
                }

                if (request.status !== 200) {
                    hideMenu();
                    return;
                }

                try {
                    showSuggestions(JSON.parse(request.responseText));
                } catch (error) {
                    hideMenu();
                }
            };
            request.send();
        }

        function scheduleSuggestions() {
            var query = input.value.trim();

            if (timerId) {
                window.clearTimeout(timerId);
            }

            if (query.length < MIN_QUERY_LENGTH) {
                hideMenu();
                return;
            }

            timerId = window.setTimeout(function () {
                requestSuggestions(query);
            }, DEBOUNCE_DELAY_MS);
        }

        input.addEventListener("input", function () {
            if (skipOwnInputEvent) {
                skipOwnInputEvent = false;
                return;
            }
            scheduleSuggestions();
        });

        input.addEventListener("focus", scheduleSuggestions);

        input.addEventListener("blur", function () {
            window.setTimeout(hideMenu, 120);
        });

        input.addEventListener("keydown", function (event) {
            var buttons = optionButtons();

            if (menu.hidden || buttons.length === 0) {
                if (event.key === "Escape") {
                    hideMenu();
                }
                return;
            }

            if (event.key === "ArrowDown") {
                event.preventDefault();
                activeIndex = (activeIndex + 1) % buttons.length;
                updateActiveOption();
            } else if (event.key === "ArrowUp") {
                event.preventDefault();
                activeIndex = activeIndex <= 0 ? buttons.length - 1 : activeIndex - 1;
                updateActiveOption();
            } else if (event.key === "Enter" && activeIndex >= 0) {
                event.preventDefault();
                chooseSuggestion(buttons[activeIndex].textContent);
            } else if (event.key === "Escape") {
                event.preventDefault();
                hideMenu();
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        var fields = document.querySelectorAll("input[data-suggestions-url]");
        Array.prototype.forEach.call(fields, initAutocomplete);
    });
})();
