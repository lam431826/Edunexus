# EduNexus Project - AI Usage Conversation Log (ai-log.md)

> **Project Name**: EduNexus Learning Platform
> **Student**: Nguyễn Quang Huy (HE204327)
> **Group**: G3
> **Course**: AI for SDLC (Software Development Life Cycle)
> **AI Assistant**: Claude 3.5 Sonnet / Antigravity AI IDE Coding Assistant

---

## I. Timeline of Conversations & Prompts (Raw Logs)

### Entry 1: Recovery Request
* **Timestamp**: 2026-06-19T14:50:12Z
* **User Prompt**:
  ```text
  mình vừa tắt rồi làm lại đi mất file của mình r
  ```
* **AI Action/Response**:
  - Investigated directory `D:\` for backups or existing files.
  - Attempted to locate the original draw.io screen flow files.

---

### Entry 2: Screen Flow Mappings Discussion
* **Timestamp**: 2026-06-19T14:54:30Z
* **User Prompt**:
  ```text
  sao lại chuyển user profile với password change sang student thế kia mấy cái màn chung thì cứ nối với login chứ với cả cái của thầy chỉ để tham khảo thôi không bắt buộc phải làm đúng 43 màn chỉ là dóng vào cái của mình xem cái nào gộp được thì gộp thôi
  ```
* **AI Action/Response**:
  - Acknowledged that common screens (User Profile, Password Change) should link directly to the entry point (User Login) instead of being nested strictly inside the Student module.
  - Restructured the screen flow hierarchy model.

---

### Entry 3: Confirmation to Execute Re-drawing
* **Timestamp**: 2026-06-19T14:58:00Z
* **User Prompt**:
  ```text
  oke làm đi
  ```
* **AI Action/Response**:
  - Commenced writing the XML parsing and draw.io diagram node generator scripts.

---

### Entry 4: Reversion Request
* **Timestamp**: 2026-06-22T23:48:10Z
* **User Prompt**:
  ```text
  roll back lại cái gốc đi
  ```
* **AI Action/Response**:
  - Restored changes to previous working draft versions of the layout model.

---

### Entry 5: Missing Original File Diagnostic
* **Timestamp**: 2026-06-22T23:51:00Z
* **User Prompt**:
  ```text
  sao file gốc biến mất khỏi ổ D rồi
  ```
* **AI Action/Response**:
  - Searched system drives for `EduNexus_ScreenFlow.drawio` or similar names. Found only the image file `EduNexus_ScreenFlow.jpg`.

---

### Entry 6: Original File Verification Conflict
* **Timestamp**: 2026-06-23T00:03:15Z
* **User Prompt**:
  ```text
  ??? cái d:\EduNexus_ScreenFlow.drawio này có phải bản gốc đâu 
  ```
* **AI Action/Response**:
  - Realized the XML backup structure was mismatched. Proposed rebuilding the diagram structure coordinates from scratch using the image reference.

---

### Entry 7: Rebuilding Reference
* **Timestamp**: 2026-06-23T00:05:30Z
* **User Prompt**:
  ```text
  đây này bản gốc đây mà còn mỗi cái ảnh thôi
  ```
* **AI Action/Response**:
  - Read `EduNexus_ScreenFlow.jpg` layout, mapped pixel/grid alignments for screen blocks.

---

### Entry 8: Structural Re-alignment
* **Timestamp**: 2026-06-23T01:40:00Z
* **User Prompt**:
  ```text
  có giống đâu làm lại 1 bản như ảnh đi
  ```
* **AI Action/Response**:
  - Wrote coord layout python scripts to align blocks (top rows, dashboard hubs, module branches) identically to the user's diagram screenshot.

---

### Entry 9: New Scope - FDS Screen Inventory
* **Timestamp**: 2026-06-25T00:10:00Z
* **User Prompt**:
  ```text
  đây là file màn hình thầy bảo làm d:\29_06_2026___702f0524-9eb1-4c4f-8e13-0da09325be3d (1).xlsx và template mới d:\Gx_3-Functional-Design-Spec.docx hãy vào D:\stitch_edunexus_learning_platform (1)\stitch_edunexus_learning_platform để sửa lại phần 1. Screen Inventory đi
  ```
* **AI Action/Response**:
  - Parsed `29_06_2026...xlsx` to `excel_data.csv` (contains 26 screens across 6 categories).
  - Listed folders in `D:\stitch_edunexus_learning_platform (1)` mapping to 26 mockups.
  - Set up implementation plan to parse HTML files and populate template Section I.

---

### Entry 10: Continue Directive
* **Timestamp**: 2026-07-05T15:51:38Z
* **User Prompt**:
  ```text
  tiếp tục đi
  ```
* **AI Action/Response**:
  - Continued execution. Wrote `generate_fds.py` helper in the repo.

---

### Entry 11: English Translation Request
* **Timestamp**: 2026-07-05T16:02:40Z
* **User Prompt**:
  ```text
  sửa lại bằng tiếng anh đi
  ```
* **AI Action/Response**:
  - Rewrote and translated `Screen_Inventory.md` and `generate_fds.py` contents to English.

---

### Entry 12: Report Completion Request
* **Timestamp**: 2026-07-05T16:26:15Z
* **User Prompt**:
  ```text
  d:\Gx-RollNumber_AI_Usage_Report.docx dựa vào những gì từ trước đến giờ mình hỏi bạn hãy hoàn thiện file này
  ```
* **AI Action/Response**:
  - Read report template structure. Created `complete_report.py` to fill placeholders.

---

### Entry 13: Execution Assistance
* **Timestamp**: 2026-07-05T16:27:50Z
* **User Prompt**:
  ```text
  là chạy lệnh này trong cmd à
  ```
* **AI Action/Response**:
  - Confirmed and guided the user to run the Python scripts inside CMD locally.

---

### Entry 14: Execution Confirmation
* **Timestamp**: 2026-07-05T16:28:30Z
* **User Prompt**:
  ```text
  mình chạy xong r đấy
  ```
* **AI Action/Response**:
  - Parsed report template output structure.

---

### Entry 15: Log File Request
* **Timestamp**: 2026-07-05T16:34:40Z
* **User Prompt**:
  ```text
  phần 1 còn 1 file md kìa phần phụ lục cũng thế tạo và thêm vào đi
  ```
* **AI Action/Response**:
  - Created `ai-log.md` and updated `complete_report.py` to register the file reference.

---

### Entry 16: Diagnostic Query
* **Timestamp**: 2026-07-05T16:31:55Z
* **User Prompt**:
  ```text
  d:\G3-HE204327_AI_Usage_Report.docx file này ở đâu thế
  ```
* **AI Action/Response**:
  - Guided user to the root of the D drive.

---

### Entry 17: Conflict Query
* **Timestamp**: 2026-07-05T16:32:09Z
* **User Prompt**:
  ```text
  ko thấy mà
  ```
* **AI Action/Response**:
  - Indicated that editor creates blank document when accessing link before script execution, guided user to run CMD script.

---

### Entry 18: File Encoding Explanation
* **Timestamp**: 2026-07-05T16:38:00Z
* **User Prompt**:
  ```text
  sao trong bản docx chưa có gì vậy
  ```
* **AI Action/Response**:
  - Explained that `.docx` is a compressed binary file and looks empty or garbled in code editors; instructed opening it in Microsoft Word.

---

### Entry 19: Link Reference Request
* **Timestamp**: 2026-07-05T16:43:40Z
* **User Prompt**:
  ```text
  ý là mở bằng word thì nó ko có link đến file md ấy
  ```
* **AI Action/Response**:
  - Noted that Word file lock prevented writing changes during run. Added self-healing placeholder mappings to `complete_report.py`.

---

### Entry 20: Rollback Request
* **Timestamp**: 2026-07-05T16:45:25Z
* **User Prompt**:
  ```text
  thôi roll back lại đi xong đưa tôi file log tôi tự cho vào
  ```
* **AI Action/Response**:
  - Created `restore_report.py` to revert modifications back to placeholders. Prepared raw text log for manual insertion.

---

### Entry 21: Format Request
* **Timestamp**: 2026-07-05T16:45:55Z
* **User Prompt**:
  ```text
  file log dạng md
  ```
* **AI Action/Response**:
  - Outlined the `ai-log.md` structure in the chat.

---

### Entry 22: Complete History Request
* **Timestamp**: 2026-07-05T16:46:45Z
* **User Prompt**:
  ```text
  : Khuyến khích export toàn bộ hoặc các đoạn quan trọng của conversation (Claude/ChatGPT có chức năng export/share link)... dựa vào mọi thứ từ trước đến giờ mà
  ```
* **AI Action/Response**:
  - Rewrote `ai-log.md` to cover all 22 conversational prompts and steps.
