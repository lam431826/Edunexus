# EduNexus - Screen Inventory Specification (FDS Section 1)

> **Source Documents**: Excel Screen List & HTML Interface Mockups.
> **Version**: v1.0 (July 2026)

---

## I. Feature Group Index

- **1. Common Features**
  - [SCR-01 - User Login](#scr-01---user-login)
  - [SCR-02 - Student Dashboard](#scr-02---student-dashboard)
  - [SCR-03 - Personal Progress](#scr-03---personal-progress)
  - [SCR-04 - Course List](#scr-04---course-list)
  - [SCR-05 - Course Structure](#scr-05---course-structure)
- **2. Lesson Features**
  - [SCR-06 - Lesson Editor](#scr-06---lesson-editor)
  - [SCR-07 - AI Lesson Staging](#scr-07---ai-lesson-staging)
  - [SCR-08 - Lesson Text Extract](#scr-08---lesson-text-extract)
  - [SCR-09 - Lesson View](#scr-09---lesson-view)
- **3. Assignment Features**
  - [SCR-10 - Assignment List](#scr-10---assignment-list)
  - [SCR-11 - Assignment Detail](#scr-11---assignment-detail)
  - [SCR-12 - Assignment Submit](#scr-12---assignment-submit)
  - [SCR-13 - Assignment Result](#scr-13---assignment-result)
- **4. Flashcard Features**
  - [SCR-14 - Flashcard Editor](#scr-14---flashcard-editor)
  - [SCR-15 - AI Flashcard Staging](#scr-15---ai-flashcard-staging)
  - [SCR-16 - Flashcard Library](#scr-16---flashcard-library)
  - [SCR-17 - Flashcard Practice](#scr-17---flashcard-practice)
- **5. Question Features**
  - [SCR-18 - Question Bank](#scr-18---question-bank)
  - [SCR-19 - Question Detail](#scr-19---question-detail)
  - [SCR-20 - AI Question Staging](#scr-20---ai-question-staging)
  - [SCR-21 - Question Import](#scr-21---question-import)
- **6. Quiz Features**
  - [SCR-22 - Quiz History](#scr-22---quiz-history)
  - [SCR-23 - New Quiz](#scr-23---new-quiz)
  - [SCR-24 - Quiz Taking](#scr-24---quiz-taking)
  - [SCR-25 - Quiz Results](#scr-25---quiz-results)
  - [SCR-26 - Quiz Review](#scr-26---quiz-review)

---

## II. Detailed Screen Specifications

### 1. Common Features

#### SCR-01 - User Login
- **Purpose:** Google Login and credentials login for Students and SMEs.
- **Role:** `Student / SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Page title | H2 | "Đăng nhập" (Sign In) |
| Email | Input email | Input field for user's email address (Required) |
| Password | Input password | Input field for user's password (Required) |
| Toggle password visibility | Button | Eye icon to toggle password visibility |
| Remember me | Input checkbox | Remembers user login session |
| Forgot password? | Text link | Link to send password reset email |
| Sign In | Button (Submit) | Submits credentials |
| Sign in with Google | Button | Fast authentication via Google OAuth |
| Register now | Text link | Link to redirect to registration page |

##### b. Navigation Flow
- Successful login as Student → Redirect to Student Dashboard (SCR-02).
- Successful login as SME → Redirect to Course List (SCR-04).
- Click "Register now" → Redirect to registration page (if available).
- Click "Forgot password?" → Display reset password confirmation.

##### c. Display Conditions
- If the user is already authenticated, automatically redirect to the respective role-specific dashboard.
- Password input text is masked by default, toggle button reveals/hides text.
- Show error banner when credentials are invalid or if the account is locked.

---

#### SCR-02 - Student Dashboard
- **Purpose:** Student main homepage displaying registered courses, classes, and content packages.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Sidebar Menu | Nav Container | Links to: Dashboard, Courses, History, Profile, Logout |
| Search bar | Input text | Search bar for course lookups |
| Notifications | Button | Opens recent notifications |
| Settings | Button | Redirects to account settings |
| User Profile Card | Section | Displays avatar, student name, role |
| Welcome Banner | Section | Personal greeting containing study progress details |
| Continue learning | Button | Resumes the most recently active lesson |
| Course Progress Gauge | Circular Progress | Graphical representation of overall completion percentage (%) |
| Registered Courses List | Grid Container | Grid of cards displaying enrolled courses |
| Course Card | Card | Shows course cover, instructor, and completion percentage |

##### b. Navigation Flow
- Click on any course card → Redirect to Lesson View (SCR-09).
- Click on learning progress → Redirect to Personal Progress (SCR-03).
- Click on Flashcards option → Redirect to Flashcard Library (SCR-16).
- Click "Quick Practice Test" → Redirect to New Quiz (SCR-23).

##### c. Display Conditions
- Requires role: Student.
- Enrolled courses list is automatically populated based on registered or paid courses of the active student.

---

#### SCR-03 - Personal Progress
- **Purpose:** Displays detailed study metrics, completed lessons, assignments, and test history.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Back to Dashboard | Button | Redirects back to Student Dashboard (SCR-02) |
| Metric Card: Study Time | Card | Accumulation of total study hours |
| Metric Card: Completed Lessons | Card | Number of parsed and completed lessons |
| Metric Card: Average Score | Card | Combined average score of assignments & quizzes |
| Chart: Module Progress | Bar Chart | Completion rates mapped per module |
| Recent Activities | List | Chronological log timeline of recent learning actions |

##### b. Navigation Flow
- Click "Back to Dashboard" → Redirect to Student Dashboard (SCR-02).
- Click "Continue Learning" → Redirect to Lesson View (SCR-09) of the first uncompleted lesson.

##### c. Display Conditions
- Display metrics as percentages (%).
- Data is loaded dynamically based on real-time activity tracking of the authenticated student.

---

#### SCR-04 - Course List
- **Purpose:** SME dashboard containing courses assigned for design and content management.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Sidebar Menu | Nav Container | Management options: Course, Question Bank, Assignments, Flashcards, Settings, Logout |
| Search bar | Input text | Searches courses by name |
| Create New Course | Button | Add a new course structure |
| Course Grid | Container | Displays list of courses assigned to current SME |
| Course Card | Card | Contains: Cover, name, and publication status (Draft/Published) |
| Edit Structure | Button | Redirects to Course Structure (SCR-05) |
| Preview | Button | View course content from Student's perspective |

##### b. Navigation Flow
- Click on a course name or "Edit Structure" → Redirect to Course Structure (SCR-05).
- Click logout → Redirect to Login (SCR-01).

##### c. Display Conditions
- Requires role: SME.
- Only displays courses where the active SME is assigned authoring/management rights.

---

#### SCR-05 - Course Structure
- **Purpose:** Course syllabus tree builder where SME configures chapters, lessons, quizzes, assignments, and flashcards.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Header metadata | Section | Course name, code, description, and status |
| Add Module | Button | Inserts a new primary course module |
| Module Accordion | Container | Collapsible block containing lessons and activities |
| Add Lesson | Button | Inserts a new lesson under the module |
| Add Flashcard Deck | Button | Inserts a new flashcard deck under the module |
| Add Assignment | Button | Inserts a new essay assignment under the module |
| Add Quiz | Button | Inserts a new quiz activity under the module |
| Reorder Controls | Button Group | Moves selected items up or down within structure |

##### b. Navigation Flow
- Click on any lesson name → Open Lesson Editor (SCR-06).
- Click "AI Gen" option → Redirect to AI Lesson Staging (SCR-07) or AI Flashcard Staging (SCR-15).
- Click on any assignment name → Open Assignment Detail (SCR-11).
- Click on Question Bank → Open Question Bank (SCR-18).

##### c. Display Conditions
- Requires role: SME.
- Syllabus is displayed as a hierarchical tree structure (Modules > Chapters > Activities).
- Save button activates only when structural changes are detected.

---

### 2. Lesson Features

#### SCR-06 - Lesson Editor
- **Purpose:** Workspace for SME to compose lesson content, add lecture video links, and upload resource documents.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Lesson Title | Input text | Title of the lesson (Required) |
| Video URL | Input URL | Embedded lecture video link (YouTube, Vimeo) |
| Attached Files | Input file | Resource files uploaded for download (PDF, DOCX) |
| Markdown Editor | Textarea | Textarea with rich Markdown formatting utilities |
| Preview Panel | Tab / Pane | Live rendering preview of the composed markdown |
| Save Draft | Button | Saves progress as a draft |
| Publish | Button | Makes the lesson visible to students |
| AI Extraction | Button | Initiates automated transcript summarization flow |

##### b. Navigation Flow
- Click "Back to Course" → Redirect to Course Structure (SCR-05).
- Click "Extract from YouTube" → Redirect to Lesson Text Extract (SCR-08).

##### c. Display Conditions
- Requires role: SME.
- Built-in editor supports live split-pane side-by-side Markdown rendering.
- Autosaves content drafts every 30 seconds.

---

#### SCR-07 - AI Lesson Staging
- **Purpose:** Staging console where SME reviews, edits, and approves AI-generated lesson content.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Comparison Panel | Grid Container | Parallel split-screen interface |
| AI Content Column | Rich Text View | Read-only pane showing the raw AI draft content |
| Editor Column | Textarea | Editable text container initialized with the AI text |
| Refine Prompt | Input text | Custom text input to request revisions from AI |
| Regenerate | Button | Submits prompt modifications to AI for a new draft |
| Approve & Save | Button | Overwrites lesson content with edited staging draft |
| Cancel | Button | Discards the staging session |

##### b. Navigation Flow
- Click "Approve & Save" → Save and redirect to Course Structure (SCR-05).
- Click "Cancel" → Return to Course Structure (SCR-05) without changes.

##### c. Display Conditions
- Requires role: SME.
- Screen contains split-column views comparing original AI draft and editable workspace side-by-side.
- Approve action is disabled if the editor content is empty.

---

#### SCR-08 - Lesson Text Extract
- **Purpose:** YouTube video scraper that extracts transcripts and leverages GenAI to formulate lesson summaries.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| YouTube URL | Input URL | Field to input video link for transcript scraping |
| Language | Select | Output summary language (Vietnamese, English) |
| Prompt Template | Select | Pick predefined summary styles (Concise, Detailed) |
| Extract & Generate | Button | Triggers scraping and summaries generations |
| Progress Bar | ProgressBar | Displays operation progress (%) |
| Output Preview | Rich Text View | Shows extracted summary results |

##### b. Navigation Flow
- Successful extraction → Automatically redirects and populates the text in AI Lesson Staging (SCR-07).

##### c. Display Conditions
- Requires role: SME.
- Input string must be a valid YouTube URL. Shows error alert if scraping fails or video lacks subtitles.

---

#### SCR-09 - Lesson View
- **Purpose:** Student viewport for lessons, offering video playback, reading content, and resource downloads.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Course Navigation | Sidebar List | Interactive tree outline of modules and lessons |
| Completion Checkbox | Icon | Visual indicator for completed lessons |
| Video Player | Player Component | Built-in player for video lecture streaming |
| Markdown Reader | Rich Text Container | Renders the text content of the lesson |
| Attachments Download | Button / Links | Download anchors for related resource files |
| Discussion Panel | Comment Section | Interactive forum for questions and comments |
| Mark Completed & Next | Button | Marks lesson as read and proceeds to next item |

##### b. Navigation Flow
- Click "Next Lesson" → Redirect to next lesson in sequence.
- Click attached activity → Redirect to Assignment Submit (SCR-12) or Quiz Taking (SCR-24).

##### c. Display Conditions
- Requires role: Student.
- Marks completion status automatically when the video finishes or reader scrolls to bottom of content.

---

### 3. Assignment Features

#### SCR-10 - Assignment List
- **Purpose:** Listing dashboard for SME to monitor, grade, and organize essay assignments.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Tab Filter | Tab Bar | Filter list by: Active, Closed, All |
| Add Assignment | Button | Create a new assignment structure |
| Assignment Table | Table | Crows: Title, Course, Due Date, Submissions, Actions |
| Edit Assignment | Button | Open configuration editor |

##### b. Navigation Flow
- Click on assignment title or "Edit" → Redirect to Assignment Detail (SCR-11).
- Click "Add New" → Redirect to Assignment Detail (SCR-11) with empty form fields.

##### c. Display Conditions
- Requires role: SME.
- Lists all assignments with counts of submitted and graded essays.

---

#### SCR-11 - Assignment Detail
- **Purpose:** Setting details and defining evaluation rubrics for essay assignments.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Assignment Title | Input text | Title of the assignment (Required) |
| Deadline Picker | Input datetime | Datetime selector for assignment deadline |
| Rubric Designer | Dynamic Grid | Table template to define rubric details |
| Add Rubric Criterion | Button | Adds a scoring criteria row |
| Criterion Name | Input text | Criteria name (e.g. Critical Thinking, Formatting) |
| Weight (%) | Input number | Scoring weight contribution (Percentage) |
| Descriptor | Input text | Description of performance expectations |
| Save Assignment | Button | Saves details and rubric parameters |

##### b. Navigation Flow
- Click "Save" → Save configurations and redirect to Assignment List (SCR-10).

##### c. Display Conditions
- Requires role: SME.
- Grid supports adding multi-level evaluation criteria. Combined rubric weight parameters must equal 100%.

---

#### SCR-12 - Assignment Submit
- **Purpose:** Submitting text essays or uploading file attachments for assignments.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Rubric View | Details Grid | Grid showing criteria levels to guide the student |
| Online Text | Textarea | Rich text area for typing answers directly |
| File Upload Area | File Drag-Drop | Drag and drop files upload zone (PDF, DOCX, ZIP) |
| Submit Button | Button | Submit essay (Shows confirmation alert dialog) |
| Status Indicator | Badge | Status display (Not Submitted, Submitted) |

##### b. Navigation Flow
- Click "Submit" → Submits work and redirects to Assignment Result (SCR-13) for AI grading.

##### c. Display Conditions
- Requires role: Student.
- Submit options block automatically when the deadline expires.
- File attachment upload size is limited to 10MB per file.

---

#### SCR-13 - Assignment Result
- **Purpose:** Displaying grading details, scored criteria, and AI feedback.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Final Grade | Big Badge | Overall score out of 100 points |
| Rubric Feedback Table | Table | Table displaying scores and remarks for each criterion |
| AI Comments | Rich Text | Overall suggestions and evaluations generated by AI |
| Re-submit | Button | Reopen submit workspace (If limits permit) |
| Back to Dashboard | Button | Return to Student Dashboard (SCR-02) |

##### b. Navigation Flow
- Click "Back to Course" → Return to Lesson View (SCR-09) or Course Structure.

##### c. Display Conditions
- Requires role: Student.
- Render final score and rubric table containing detailed score mappings clearly.
- Render AI evaluation text in a readable rich-text format.

---

### 4. Flashcard Features

#### SCR-14 - Flashcard Editor
- **Purpose:** SMEs build vocabulary or concept cards by typing Side A and Side B text.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Deck Name | Input text | Title of the flashcard deck |
| Description | Input text | Brief summary of the deck concepts |
| Card Row List | Container | List of card elements |
| Side A Input | Input text | Card Front: Word, phrase or concept (Required) |
| Side B Input | Input text | Card Back: Definition, explanation (Required) |
| Add New Card | Button | Append a card row to list |
| Save Deck | Button | Save deck details |

##### b. Navigation Flow
- Click "Save" → Redirect back to Course Structure (SCR-05).

##### c. Display Conditions
- Requires role: SME.
- Decks support unlimited card capacities.

---

#### SCR-15 - AI Flashcard Staging
- **Purpose:** Editing and filtering flashcard decks generated automatically by AI from lessons.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Card Count Slider | Input range | Select number of flashcards to generate |
| Cards Grid | Grid Container | Grid of AI proposed card suggestions |
| Select Checkbox | Input checkbox | Select cards to save |
| Editable Side A | Input text | Quick edit Front content of flashcard |
| Editable Side B | Input text | Quick edit Back content of flashcard |
| Regenerate | Button | Regene cards that are not checked |
| Save Selected | Button | Export selected cards into deck |

##### b. Navigation Flow
- Click "Save Selected" → Adds checked items to course deck and returns to Course Structure.

##### c. Display Conditions
- Requires role: SME.
- Renders AI generated terms, checkbox inputs allow bulk select.

---

#### SCR-16 - Flashcard Library
- **Purpose:** Collection of student's personal decks showing completion and mastery status.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Search bar | Input text | Look up decks by topic |
| Tab Filter | Tab Bar | Filter options: My Decks, Favorites, All |
| Deck Card | Card | Cards showing deck metadata, progress bar (%) |
| Practice Now | Button | Starts practice session on selected deck |

##### b. Navigation Flow
- Click "Practice Now" → Redirect to Flashcard Practice (SCR-17).

##### c. Display Conditions
- Requires role: Student.
- Displays indicators for cards mastered, learning, and remaining to study.

---

#### SCR-17 - Flashcard Practice
- **Purpose:** Interactive slide deck showing cards with 3D rotation and spaced repetition buttons.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Flashcard Viewport | Interactive Card | Card element that flips when clicked |
| Progress Bar | ProgressBar | Visual progress bar of cards reviewed |
| Show Answer | Button | Flips card to display Side B details |
| Easy | Button | Spaced repetition: Easy (Schedule card review for much later) |
| Medium | Button | Spaced repetition: Medium (Schedule standard recall interval) |
| Hard | Button | Spaced repetition: Hard (Repeat immediately) |
| Back to Library | Button | Exit to Flashcard Library (SCR-16) |

##### b. Navigation Flow
- Click "Back to Library" → Redirect to Flashcard Library (SCR-16).

##### c. Display Conditions
- Requires role: Student.
- Cards flip 3D animation when clicking anywhere on the viewport.
- Recording button feedback updates internal spaced repetition database schema.

---

### 5. Question Features

#### SCR-18 - Question Bank
- **Purpose:** Centralized index where SME coordinates course test questions.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Add Question | Button | Direct manually question creator |
| Import from File | Button | Upload bulk files parser |
| AI Generate | Button | Trigger AI questions generator console |
| Search bar | Input text | Search questions content |
| Difficulty Filter | Select | Filter by: Easy, Medium, Hard |
| Questions List Table | Table | Fields: Question snippet, Type, Difficulty, Action |

##### b. Navigation Flow
- Click "Add Question" → Open Question Detail (SCR-19).
- Click "Import from File" → Open Question Import (SCR-21).
- Click "AI Generate" → Open AI Question Staging (SCR-20).

##### c. Display Conditions
- Requires role: SME.
- Filter controls support sorting lists by difficulty status and topic tags.

---

#### SCR-19 - Question Detail
- **Purpose:** Formatting questions and options while specifying the correct solution and explanations.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Question Text | Textarea | Title question box editor (Required) |
| Difficulty | Select | Pick level: Easy, Medium, Hard |
| Options Section | Container | List of choices inputs |
| Option text | Input text | Options contents |
| Correct Answer Radio | Input radio | Checkbox to designate correct choices |
| Explanation | Textarea | Rationale/explanation text |
| Save | Button | Commits question details |

##### b. Navigation Flow
- Click "Save" → Saves details and returns to Question Bank (SCR-18).

##### c. Display Conditions
- Requires role: SME.
- Requires designating at least one correct choice.
- Exposes explanation details to students during subsequent reviews.

---

#### SCR-20 - AI Question Staging
- **Purpose:** Editing and filtering multiple choice test questions generated by AI.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Lesson Source | Select | Target lesson reference source for generator |
| Question count | Input number | Total question items selector |
| Staging List | Container | Holds list of AI generated questions |
| Editable Question | Textarea | Sửa đề bài câu hỏi (Edit AI question content) |
| Editable Answers | Input text | Edit choice options strings |
| Approve & Save | Button | Commit approved entries to database |

##### b. Navigation Flow
- Click "Approve & Save" → Save checked items to Question Bank (SCR-18).

##### c. Display Conditions
- Requires role: SME.
- SME holds full editing control over choices, questions, and indicators before committing to database.

---

#### SCR-21 - Question Import
- **Purpose:** Bulk importing test questions using template Excel formatting.
- **Role:** `SME`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Download Template | Link | Links to download sample file format |
| File Drag Area | File Upload | Dropzone for Excel files |
| Parse & Preview | Button | Previews file contents |
| Validation Console | Container | Displays parsed validation errors in red |
| Confirm Import | Button | Insert validated questions to database |

##### b. Navigation Flow
- Click "Confirm Import" → Imports valid lines and redirects to Question Bank (SCR-18).

##### c. Display Conditions
- Requires role: SME.
- Accepts standard template structure parameters only, flagging mismatches in real time.

---

### 6. Quiz Features

#### SCR-22 - Quiz History
- **Purpose:** Student summary page highlighting test records and metrics.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Metric Card: Avg Score | Card | Average score achieved across all quizzes |
| Metric Card: Total Quizzes | Card | Accumulation count of tests finished |
| History Table | Table | Crows: Test name, Date, Score, Time Spent, Action |
| Review Answers | Button | Links to Review dashboard |
| Take New Quiz | Button | Redirects to new quiz builder |

##### b. Navigation Flow
- Click "Take New Quiz" → Redirect to New Quiz (SCR-23).
- Click "Review Answers" → Redirect to Quiz Review (SCR-26).

##### c. Display Conditions
- Requires role: Student.
- Displays lists detailing completed trắc nghiệm items, grades, and pass/fail badges.

---

#### SCR-23 - New Quiz
- **Purpose:** Configuring and starting quiz practice sessions.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Page Title | H1 | "Tạo bài kiểm tra trắc nghiệm" (New Quiz Builder) |
| Module Selection | Select / Checkboxes | Pick target module for evaluation |
| Topic Tags | Tag Selection | Filter by specific card/question tags |
| Question Count | Input select | Choose sizing options (10, 20, 45 items) |
| Mode Select | Radio Group | Pick mode: Practice (Un-timed) / Test (Timed) |
| Start Quiz | Button | Initiates testing screen |

##### b. Navigation Flow
- Click "Start Quiz" → Redirect to Quiz Taking (SCR-24).

##### c. Display Conditions
- Requires role: Student.
- Allows students to dynamically filter and customize testing constraints.

---

#### SCR-24 - Quiz Taking
- **Purpose:** Timer-based testing workspace with question navigation list.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Timer Countdown | Display Widget | Active timer countdown |
| Question Navigator | Grid Panel | Index panel of question items |
| Flag Icon | Button | Bookmarks active question for follow-up |
| Question Text | Rich Text Container | Displays active question parameters |
| Answer Option Group | Radio Group | Options layout |
| Next Question | Button | Navigate forward |
| Previous Question | Button | Navigate backward |
| Submit Quiz | Button | Submits test (Triggers confirmation dialog) |

##### b. Navigation Flow
- Click "Submit Quiz" → Submits responses and redirects to Quiz Results (SCR-25).

##### c. Display Conditions
- Requires role: Student.
- Timer expiration locks the UI and triggers automatic submission.
- Unsaved selections cache locally to prevent data loss on network dropouts.

---

#### SCR-25 - Quiz Results
- **Purpose:** Quiz summary statistics detailing scores, time elapsed, and correct counts.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Final Score Gauge | Chart Component | Displays score in circular chart representation |
| Correct Count Badge | Badge | Total count of correct questions |
| Incorrect Count Badge | Badge | Total count of incorrect questions |
| Duration text | Text | Total time spent |
| Review Answers | Button | Redirects to detailed review page |
| Back to History | Button | Returns to history listings |

##### b. Navigation Flow
- Click "Review Answers" → Redirect to Quiz Review (SCR-26).
- Click "Back to History" → Redirect to Quiz History (SCR-22).

##### c. Display Conditions
- Requires role: Student.
- Displays summary statistics detailing counts correct, incorrect, and skipped.

---

#### SCR-26 - Quiz Review
- **Purpose:** Question-by-question response review displaying correct answers and explanations.
- **Role:** `Student`

##### a. UI Components
| Component | Type | Description |
| --- | --- | --- |
| Question Navigator | Grid Panel | Index panel (Color-coded: Green = Correct, Red = Wrong) |
| Question Display | Rich Text Container | Displays the question text |
| Option Result Group | Radio Group | Options displaying incorrect/correct icons |
| Explanation Card | Section | Renders explanation details added by SME |
| Exit Review | Button | Closes review screen |

##### b. Navigation Flow
- Click "Exit" → Redirect to Quiz History (SCR-22).

##### c. Display Conditions
- Requires role: Student.
- Color codes correct submissions as green, mistakes as red.
- Renders SME-designed explanation fields under each item.
