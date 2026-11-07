package com.shopycat.web;

import com.shopycat.model.*;
import com.shopycat.security.CurrentUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Values every page can use. */
@ControllerAdvice
class GlobalModel {

    private final CurrentUser currentUser;

    GlobalModel(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @ModelAttribute("me")
    AppUser me() {
        return currentUser.getOrNull();
    }

    @ModelAttribute("categories")
    Category[] categories() {
        return Category.values();
    }

    @ModelAttribute("audiences")
    Audience[] audiences() {
        return Audience.values();
    }

    @ModelAttribute("conditions")
    ItemCondition[] conditions() {
        return ItemCondition.values();
    }
}
