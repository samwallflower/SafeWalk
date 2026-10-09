Read ../frontend/docs/00_SAFEWALK_API_CONTRACT.md and ../frontend/docs/02_SAFEWALK_FLUTTER_MASTER_PLAN.md before any work. The Java contract is in ../frontend/backend-contract/java. Never invent endpoints.
Work one phase (M0..M9) at a time; stop after each phase, run `flutter analyze` and `flutter test`, and report.
Feature-first folders: lib/features/<name>/{data,domain,presentation}. One responsibility per file.
No dynamic: type everything. Models are hand-written Dart classes (no code generation).
Never log tokens or coordinates. userId always comes from the stored session.
Do not run a production build or server without asking the user.
