# Copilot / AI agent instructions for the Underroot project

This file provides guidance to automated coding agents (for example GitHub Copilot or other AI assistants) when editing, refactoring, or adding code in this repository.

Key mapping and shorthand
- When the user or a task references "Client" or "client", operate on the code under the `client/` folder.
- When the user or a task references "Server" or "server", operate on the code under the `server/` folder.

Repository layout (important files and folders)
- `client/` — Java client application (Maven project). Primary entrypoint: `client/src/main/java/com/underroot/latexclient/Main.java`.
  - `client/src/main/java/com/underroot/latexclient/network/ServerConnection.java` — networking client code.
  - `client/src/main/java/com/underroot/latexclient/dto/Message.java` — shared DTO used by client.
  - `client/pom.xml` — Maven build file for the client application.

- `server/` — Java server application (Maven project). Primary entrypoint: `server/src/main/java/com/underroot/latexserver/Main.java`.
  - `server/src/main/java/com/underroot/latexserver/LatexServer.java` — server main logic and listener.
  - `server/src/main/java/com/underroot/latexserver/ClientHandler.java` — per-connection handler.
  - `server/src/main/java/com/underroot/latexserver/dto/Message.java` — shared DTO used by server.
  - `server/pom.xml` — Maven build file for the server application.

High-level goals for agents
- Make minimal, well-tested changes. Prefer small edits and follow existing style.
- Preserve public APIs unless the user asks to change them. If renaming or changing signatures, update all usages in both `client/` and `server/`.
- When adding or editing network or DTO code, ensure compatibility between client and server DTO classes (they should serialize/deserialize consistently).

Build and test guidance
- Each of `client/` and `server/` are separate Maven projects. Typical commands (run from repository root or inside each folder):

  - Build: `mvn -f client/pom.xml -DskipTests package` (or similarly for `server/pom.xml`).
  - Run client: run the `Main` class in `client` (for example via `mvn -f client/pom.xml exec:java -Dexec.mainClass=com.underroot.latexclient.Main` if `exec-maven-plugin` is configured, or run from an IDE).
  - Run server: run the `Main` class in `server` (for example via `mvn -f server/pom.xml exec:java -Dexec.mainClass=com.underroot.latexserver.Main`).

- If you add unit tests, place them under `src/test/java` in the appropriate module and run `mvn -DskipTests=false test` for that module.

Agent behavior rules
- Always reference the mapping above: use `client/` when user says Client, and `server/` when user says Server.
- For refactors or cross-cutting changes, search and update both modules if relevant.
- Before changing public classes, list the files you'll edit and why.
- Run the project's build or unit tests after any change that touches compiled code. Report build/test PASS or FAIL.
- Prefer adding small tests that demonstrate behavior for new or changed logic.

Commit and PR guidance
- Keep commits small and focused. Each commit should compile.
- Include a short commit message describing intent, e.g. `client: fix NPE in ServerConnection` or `server: add graceful shutdown`.
- When creating a PR, include: summary, files changed, build/test results, and any manual steps needed to run.

Safety and compatibility
- Don't add external network calls or credentials. Avoid storing secrets in the repo.
- If a requested change requires non-local resources (databases, real network endpoints, external APIs), describe the required environment and provide a local mock or a test double when possible.

Assumptions the agent may make
- Both modules are Maven Java projects with compatible Java versions. If unsure, inspect each module's `pom.xml` to confirm target Java version and dependencies.
- DTOs under `client` and `server` are expected to be compatible; if not, validate and propose a migration plan.

When to ask the user
- If a change requires changing both `client` and `server` but the user didn't authorize cross-repo edits, ask for confirmation.
- If a build fails because of missing environment variables or external services, report the exact error and ask for guidance.

Delighters (optional helpful actions)
- Add quick README notes in `client/README.md` or `server/README.md` when you add new run instructions or external requirements.
- Add unit tests for critical fixes.

Contact style
- Keep PR descriptions concise and actionable.
- When explaining code changes in comments or PRs, prefer short examples and concrete reasoning.


-- End of instructions --
