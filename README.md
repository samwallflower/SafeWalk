# SafeWalk

<!-- BADGES GO HERE -->
[![codecov](https://codecov.io/github/samwallflower/SafeWalk/graph/badge.svg?token=QA6NHJVQXR)](https://codecov.io/github/samwallflower/SafeWalk)
[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=samwallflower_SafeWalk&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=samwallflower_SafeWalk)
## 🛡️ About The Project
SafeWalk is a secure routing and incident reporting API designed to provide safe navigation recommendations and track localized incidents. 

### Key Features
*   **Routing Service:** Calculates optimized and secure route recommendations.
*   **Incident Reporting:** Allows users to submit, upvote, and track localized incidents (e.g., theft, hazards).
*   **Heatmap Generation:** Aggregates nearby incidents to visualize high-risk areas based on severity weights.

---

## 💻 Tech Stack
*   **Language:** Java 17
*   **Framework:** Spring Boot 4.0.5
*   **Database:** PostgreSQL (Production) / H2 (Testing)
*   **Build Tool:** Maven
*   **CI/CD:** GitHub Actions, SonarCloud, Codecov
*   **Testing:** JUnit 5, Mockito, JaCoCo

---

## 🚀 Getting Started

### Prerequisites
*   Java 17 JDK
*   Maven
*   PostgreSQL running locally

### Installation & Running Locally
1. Clone the repository:
   git clone https://github.com/your-github-username/SafeWalk.git
   
2. Navigate to the backend directory (if applicable):
   cd SafeWalk
   
3. Update your application.properties with your local PostgreSQL credentials and any required API keys (e.g., Google Maps).

4. Run the application:
   mvn spring-boot:run
   
### Running Tests
The test suite utilizes an in-memory H2 database, so no external database connection is required to run the tests.

mvn clean verify

This will compile the application, execute all slice and integration tests, and generate a JaCoCo code coverage report in target/site/jacoco.
