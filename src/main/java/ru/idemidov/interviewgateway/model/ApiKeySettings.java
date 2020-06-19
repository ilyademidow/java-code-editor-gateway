package ru.idemidov.interviewgateway.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Stores all API key parameters")
@RedisHash("apiKeySettings")
public class ApiKeySettings implements Serializable {
    @Id
    @ApiModelProperty(value = "API key", dataType = "String", example = "0110")
    private String apiKey;
    @ApiModelProperty(value = "username", dataType = "String", example = "donald_knutt")
    private String username;
    @ApiModelProperty(value = "Remained total invocation quantity. It is updated each invocation", dataType = "Long", example = "100")
    private Long remainedInvocationQuantity;
    @ApiModelProperty(value = "Maximum code length", dataType = "Long", example = "1024")
    private Long maxCodeLength;
    @ApiModelProperty(value = "Invocation frequency in minutes. For example if you put 10 so you can invoke " +
            "your code one time for 10 minutes", dataType = "Integer", example = "10")
    private Integer invocationFrequency;
    @ApiModelProperty(value = "It's service value. It is updated each invocation", dataType = "LocalDateTime (ISO-8601)", example = "2007-12-03T10:15:30")
    private LocalDateTime lastInvocation;
}

