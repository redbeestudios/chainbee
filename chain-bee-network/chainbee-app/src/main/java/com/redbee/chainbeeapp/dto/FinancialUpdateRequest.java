package com.redbee.chainbeeapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class FinancialUpdateRequest {
    @NotNull(message = "'id' field cannot be null")
    private String id;
    @NotNull(message = "'mixed_salary' field cannot be null")
    private boolean mixedSalary;
    @NotNull(message = "'salary' field cannot be null")
    @Min(value = 0L, message = "'salary' field cannot be less than 0")
    private Double salary;
}
