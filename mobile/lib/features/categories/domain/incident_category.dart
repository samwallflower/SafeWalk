/// Mirrors IncidentCategoryDto.java
class IncidentCategory {
  const IncidentCategory({
    required this.id,
    required this.name,
    required this.severityWeight,
    this.description,
  });

  factory IncidentCategory.fromJson(Map<String, Object?> json) =>
      IncidentCategory(
        id: (json['id'] as num).toInt(),
        name: json['name'] as String,
        severityWeight: (json['severityWeight'] as num?)?.toDouble() ?? 0,
        description: json['description'] as String?,
      );

  final int id;
  final String name;
  final double severityWeight;
  final String? description;
}
