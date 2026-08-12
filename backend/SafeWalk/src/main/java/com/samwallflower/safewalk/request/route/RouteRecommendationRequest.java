package com.samwallflower.safewalk.request.route;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RouteRecommendationRequest {

    @NotNull @DecimalMin(value = "-90", message = "Origin latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Origin latitude must be between -90 and 90")
    private Double originLatitude;

    @NotNull @DecimalMin(value = "-180.0", message = "Origin longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Origin longitude must be between -180 and 180")
    private Double originLongitude;

    @NotNull @DecimalMin(value = "-90.0", message = "Destination latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Destination latitude must be between -90 and 90")
    private Double destinationLatitude;

    @NotNull @DecimalMin(value = "-180.0", message = "Destination longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Destination longitude must be between -180 and 180")
    private Double destinationLongitude;
}
