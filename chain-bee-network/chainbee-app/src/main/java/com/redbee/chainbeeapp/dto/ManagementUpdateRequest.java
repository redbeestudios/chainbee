package com.redbee.chainbeeapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class ManagementUpdateRequest {
    @NotNull(message = "'id' field cannot be null")
    private String id;
    @NotEmpty(message = "'seniority' field cannot be null or empty")
    private String seniority;
    @NotEmpty(message = "'manager' field cannot be null or empty")
    private String manager;
    @NotEmpty(message = "'assignment' field cannot be null or empty")
    private String assignment;
}
