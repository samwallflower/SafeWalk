package com.samwallflower.safewalk.request.walksession;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWalkSession {
    @NotNull @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    @NotNull @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
}
