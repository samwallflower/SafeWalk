package com.samwallflower.safewalk.exception;

public class RateLimitExceedeedException extends RuntimeException {
    public RateLimitExceedeedException(String message) {
        super(message);
    }
}
