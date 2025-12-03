# UofT Course Explorer & Planner (Team 8)

## Application Summary
The **UofT Course Explorer & Planner** is a Java Swing application designed to help University of Toronto students discover and plan their academic path. Unlike standard course finders, our tool leverages **Artificial Intelligence** to analyze a student's vague or specific interests and recommend valid UofT courses that match their goals.

Key features include:
- **Smart Recommendations:** Uses LLMs to find courses based on natural language interests (e.g., "I like building things and sustainability") rather than just keywords.
- **Personalized Filtering:** Excludes courses the student has already taken.
- **Deep Insights:** Provides AI-generated rationales explaining *why* a specific course was recommended for that specific user.
- **Local Persistence:** Securely saves user profiles, course history, and API Keys.

## 📝 User Stories & Responsibilities

| Team Member | User Story | Implementation |
| :--- | :--- | :--- |
| **Andrew** | *As a user, I want the app to securely save my API Key, ‘courses taken’, and the last submitted ‘interests’.* | Implemented local data persistence using `AppStateStore` and JSON/Properties files to save state across sessions. |
| **Izay** | *As a student who is unsure of my interests, I want the app to have an interest survey that will recommend me keywords to use.* | Created the "Not Sure..." feature which prompts the LLM to suggest academic keywords based on broad user input. |
| **Hongyi** | *As a user, I want the app to recommend courses based on my courses taken and the submitted interests.* | Developed the core `RecommendCoursesUseCase` and the connection to the Gemini API to fetch and parse valid UofT course recommendations. |
| **Harry** | *As a student, I want to be able to click on a recommended course to see the course description.* | Implemented the **Accordion View** (`CourseResultPanel`) in the UI, allowing users to expand/collapse course details dynamically without cluttering the screen. |
| **Arthur** | *As a user, I want to be able to see why a course was recommended for me.* | Integrated the "Relevance" field into the Entity and View, displaying an AI-generated explanation connecting the course content to the user's specific interests. |

## API Usage

This project utilizes the **Google Gemini Developer API** (Model: `gemini-2.5-flash`) to power its recommendation engine.

- **Role:** The API acts as an "Academic Advisor" agent.
- **Functionality:**
  - It receives a prompt containing the student's interests and completed courses.
  - It performs a web search (grounding) to validate that recommended course codes exist in the 2024-2025 UofT Calendar.
  - It returns a structured JSON response containing:
    - Valid Course Codes (e.g., `CSC207H1`)
    - Summarized Descriptions
    - Prerequisites
    - Personalized "Relevance" explanations
- **Privacy:** The API Key is stored securely on the user's local machine and is never hardcoded into the application.

## Screenshots

### 1. Interest Survey & Settings
*The main input area where users enter interests, manage their history, and securely set their API key.*

<img width="295" height="617" alt="Right panel" src="https://github.com/user-attachments/assets/f77b12a7-9155-40b4-bfa0-6a87996b4a0d" />


### 2. Course Recommendations (Accordion View)
*The results panel showing a list of recommended courses. Clicking a course expands it to reveal details, prerequisites, and the AI's relevance explanation.*

<img width="689" height="610" alt="Left Panel" src="https://github.com/user-attachments/assets/ea3d1bda-1624-4f8e-bb2e-ed2cccc88a56" />

---

## Architecture & Design

This project strictly follows **Clean Architecture** principles to ensure modularity and testability.

- **Entity Layer:** Contains the `Course` object and business rules. Independent of all other layers.
- **Use Case Layer:** Contains `RecommendCoursesInteractor` which orchestrates the logic (fetching data, processing it) without knowing about the UI or Database.
- **Interface Adapters:** Contains `Controllers`, `Presenters`, and `ViewModels` that convert data between the UI and the Use Cases.
- **Frameworks & Drivers:** Contains the Swing UI (`MainFrame`, `CourseResultPanel`) and Data Access Objects (`GeminiCourseDataAccessObject`).

### Key Design Patterns Used:
- **Observer Pattern:** The View observes the `ViewModel` for state changes.
- **Dependency Injection:** Dependencies are injected via the Main class, allowing us to easily swap the real API for a dummy DAO during testing.
- **Single Responsibility Principle:** Each class has a distinct role (e.g., `ProfileController` handles storage, `RecommendController` handles recommendations).
