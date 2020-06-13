package ru.idemidov.interviewgateway.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel(description = "Stores error or success of execution of a code. Only one of this properties could be " +
        "filled in a same time. Indeed no sense to return anything to Error field in success case")
public class Result {
    @ApiModelProperty(example = "Exit code 0\nHello world!")
    private final String success;
    @ApiModelProperty(example = "null")
    private final String error;
}
