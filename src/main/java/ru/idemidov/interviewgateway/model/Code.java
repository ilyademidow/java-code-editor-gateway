package ru.idemidov.interviewgateway.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Code implements Serializable {
    private final String username;
    private final String code;
}
