package com.redbee.chainbeeapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bee {
    private boolean isActive;
    private String name;
    private String id;
    private String seniority;
    private String manager;
    private String assignment;
    private boolean hasMixedSalary;
    private Double salary;
}
