package com.github.malahor.forprox;

public class ForbiddenException extends Exception {
    public ForbiddenException(String host) {
        super(host);
    }
}
