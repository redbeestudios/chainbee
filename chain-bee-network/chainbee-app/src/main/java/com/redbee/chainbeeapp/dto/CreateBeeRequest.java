package com.redbee.chainbeeapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class CreateBeeRequest {
    @NotNull(message = "'id' field cannot be null")
    Integer id;
    @NotEmpty(message = "'name' field cannot be null or empty")
    String name;
    @NotEmpty(message = "'seniority' cannot be null or empty")
    String seniority;
}
