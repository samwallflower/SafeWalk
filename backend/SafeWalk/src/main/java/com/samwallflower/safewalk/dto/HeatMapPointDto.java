package com.samwallflower.safewalk.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HeatMapPointDto implements Serializable {
    private double latitude;
    private double longitude;
    private Integer severityWeight;
}
