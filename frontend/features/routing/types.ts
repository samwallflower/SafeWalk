/** Mirrors backend-contract/java/dto/RouteDto.java */
export interface RouteDto {
  id: number;
  polyline: string;
  actualDistanceMeters: number;
  safetyPenaltyMeters: number;
  virtualDistanceMeters: number;
  /** 1 = best (lowest virtual distance). */
  rank: number;
  routeRequestId: string;
}

/** Mirrors RouteRecommendationRequest.java */
export interface RouteRecommendationBody {
  originLatitude: number;
  originLongitude: number;
  destinationLatitude: number;
  destinationLongitude: number;
}
