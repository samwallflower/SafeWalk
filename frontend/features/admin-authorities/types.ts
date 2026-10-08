/** Mirrors backend-contract/java/dto/EmergencyAuthorityDto.java */
export interface EmergencyAuthority {
  id: number;
  countryCode: string;
  countryName: string;
  policeNumber: string;
  ambulanceNumber: string;
  generalEmergencyNumber: string | null;
}
