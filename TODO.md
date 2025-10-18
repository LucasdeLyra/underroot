# Project TODO List

This file tracks the high-level tasks for building the real-time cooperative LaTeX editor.

## Phase 1: Foundation & Core Protocol

-   [x] **1. Review codebase & build:**
    -   Run a local build for both `client/` and `server/`.
    -   Inspect `Main.java`, `ServerConnection.java`, `ClientHandler.java`, and `Message.java` DTOs.
    -   **Acceptance:** Both modules build successfully, or any existing errors are documented.

-   [ ] **2. Define message protocol:**
    -   Create a clear protocol specification in `docs/protocol.md` for all message types (`JOIN_DOCUMENT`, `INSERT_TEXT`, `COMPILE_RESULT`, etc.).
    -   Define the payload structure for each message.
    -   **Acceptance:** A single source-of-truth protocol document exists.

-   [x] **3. Harmonize DTOs:**
    -   Update `client/.../dto/Message.java` and `server/.../dto/Message.java` to be identical and match the protocol specification.
    -   Consider adding a version field for future compatibility.
    -   **Acceptance:** DTOs in both client and server are compatible for serialization/deserialization.

-   [x] **4. Choose sync strategy:**
    -   Decide on a concurrency model (e.g., simple server-authoritative log vs. CRDT/OT).
    -   Document the decision and trade-offs in `docs/sync.md`.
    -   **Acceptance:** A synchronization strategy is chosen and documented. For MVP, a server-authoritative model is recommended.

## Phase 2: MVP Implementation

-   [x] **5. Server: Implement document model & op handling:**
    -   Create an in-memory document model on the server.
    -   Implement logic to apply incoming operations (`INSERT_TEXT`, etc.).
    -   Broadcast `UPDATE_DOCUMENT` messages to connected clients.
    -   **Acceptance:** The server can manage a document's state based on client operations.

-   [x] **6. Client: Capture edits & send ops:**
    -   Implement logic to capture user edits in the client.
    -   Translate edits into protocol messages and send them to the server via `ServerConnection`.
    -   Handle incoming `UPDATE_DOCUMENT` messages to update the local view.
    -   **Acceptance:** The client can send edits and receive updates.

-   [x] **7. Implement compile pipeline:**
    -   Handle `REQUEST_COMPILE` on the server.
    -   Run the LaTeX compiler in a sandboxed environment.
    -   Return the `COMPILE_RESULT` to the requesting client.
    -   **Acceptance:** Users can trigger and receive results from a LaTeX compile.

-   [x] **8. Session and user management:**
    -   Implement session tracking and unique user IDs.
    -   Handle `USER_JOINED` and `USER_LEFT` notifications.
    -   **Acceptance:** The server is aware of connected users for each document.

## Phase 3: Testing & Hardening

-   [ ] **9. Integration tests & small harness:**
    -   Create integration tests simulating multiple clients joining, editing, and compiling.
    -   Assert that all clients eventually reach a consistent state.
    -   **Acceptance:** Automated tests verify the core collaborative functionality.

-   [ ] **10. Logging, error handling & graceful shutdown:**
    -   Add structured logging to both client and server.
    -   Implement retry logic for temporary network errors.
    -   Ensure graceful shutdown to prevent data loss.
    -   **Acceptance:** The system is robust and observable.

-   [ ] **11. Docs, run instructions & READMEs:**
    -   Create `README.md` files for the root, client, and server directories.
    -   Include instructions on how to build, run, and test the project.
    -   **Acceptance:** A new developer can easily get the project running.

-   [ ] **12. CI and code quality:**
    -   Set up a GitHub Actions workflow to build and test the project on every pull request.
    -   **Acceptance:** CI enforces code quality and prevents regressions.

## Phase 4: Security & Future Work

-   [ ] **13. Security & sandboxing for compiles:**
    -   Design and implement a secure sandbox for running the LaTeX compiler (e.g., using Docker).
    -   Document security risks in `docs/security.md`.
    -   **Acceptance:** The compilation step cannot compromise the server.

-   [x] **14. Roadmap: Advanced sync (CRDT/OT):**
    -   Plan the migration from the simple server-authoritative model to a more advanced CRDT or OT model.
    -   **Acceptance:** A clear path for future improvements to the sync logic is documented.

-   [ ] **15. Polish & release checklist:**
    -   Create a checklist for releasing a new version (bumping versions, tagging, building artifacts, etc.).
    -   **Acceptance:** The project is ready for repeatable, reliable releases.
