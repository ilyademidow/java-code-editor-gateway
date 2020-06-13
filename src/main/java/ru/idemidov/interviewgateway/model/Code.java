package ru.idemidov.interviewgateway.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "Stores implementation of a Java class and its author name")
public class Code implements Serializable {
    @ApiModelProperty(value = "Spaces not allowed", dataType = "String", example="donald_knutt")
    private final String username;
    @ApiModelProperty(value = "Java class implementation", dataType = "String", example="class Main { public static void main(String[] args) { System.out.println(\"Hello world!\"); } }")
    private final String code;
}
