# GitHub Copilot & AI Instructions for KTX Smart Hub

See full architecture and constraints in [AGENTS.md](../AGENTS.md) and [docs/GIT_WORKFLOW.md](../docs/GIT_WORKFLOW.md).

## Key Guidelines:
1. **Architecture:** 3-tier Jakarta EE 10 (Tomcat 10.1, Servlet 6.0, JSP 3.0, MySQL 8.0, HikariCP).
2. **Business Logic:** Encapsulated in Stored Procedures in `database/01_schema.sql`. Do not bypass them in DAOs.
3. **Hot-reload:** JSP views are mounted via `docker-compose.override.yml`. Refresh browser to see changes.
4. **Git rules:** Branch naming `<type>/<author>-<feature>`, Conventional Commits. Never push to `main` or `develop`.
5. **Language:** Respond in Vietnamese, concise and structured.
