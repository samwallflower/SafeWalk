/** Mirrors backend-contract/java/dto/IncidentCategoryDto.java */
export interface IncidentCategory {
  id: number;
  name: string;
  severityWeight: number;
  description: string | null;
}
