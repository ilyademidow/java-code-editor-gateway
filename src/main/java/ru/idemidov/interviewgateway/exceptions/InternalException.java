package ru.idemidov.interviewgateway.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InternalException extends RuntimeException {
    private final String message;
    private final Exception e;
}
