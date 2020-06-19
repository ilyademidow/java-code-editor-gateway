package ru.idemidov.interviewgateway.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Stores implementation of a Java class and its author name")
public class Code implements Serializable {
    @ApiModelProperty(value = "Spaces not allowed", dataType = "String", example="donald_knutt")
    private String username;
    @ApiModelProperty(value = "Provided API key", dataType = "String", example="demo")
    private String apiKey;
    @ApiModelProperty(value = "Java class implementation", dataType = "String", example="class Main { public static void main(String[] args) { System.out.println(\"Hello world!\"); } }")
    private String code;
}
