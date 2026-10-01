# GitHub Copilot & AI Instructions for KTX Smart Hub

See architecture in [Spring Boot design](../docs/demo/SPRING_BOOT.md) and development rules in [Git workflow](../docs/main/GIT_WORKFLOW.md).

## Key Guidelines:
1. **Architecture:** Target Spring Boot/Java17, Thymeleaf, Spring Security, Spring JDBC and PostgreSQL16; see [Spring Boot design](../docs/demo/SPRING_BOOT.md). Current runtime remains Servlet/JSP WAR with Tomcat10.1 and HikariCP.
2. **Business Logic:** Encapsulated in Stored Procedures in `database/01_schema.sql`. Do not bypass them in DAOs.
3. **Hot-reload:** JSP views are mounted via `docker-compose.override.yml`. Refresh browser to see changes.
4. **Git rules:** Branch naming `<type>/<author>-<feature>`, Conventional Commits. Never push to `main` or `develop`.
5. **Language:** Respond in Vietnamese, concise and structured.
