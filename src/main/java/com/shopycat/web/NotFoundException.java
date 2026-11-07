package com.shopycat.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Also used when a post exists but the viewer is not allowed to see it, so its existence is not revealed. */
@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException {
    NotFoundException() {
        super("Not found");
    }
}
