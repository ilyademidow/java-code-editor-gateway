package ru.idemidov.interviewgateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    private final String success;
    private final String error;
}
