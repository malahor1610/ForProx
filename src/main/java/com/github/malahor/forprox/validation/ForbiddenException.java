package com.github.malahor.forprox.validation;

public class ForbiddenException extends Exception {
    public ForbiddenException(String host) {
        super(host);
    }
}
