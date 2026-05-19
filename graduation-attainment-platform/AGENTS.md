# 项目级 AGENTS 说明

本文档用于约束 AI agent 在 `graduation-attainment-platform/` 目录中的工作方式，只服务于应用本体开发区，不覆盖仓库根目录下的课程协作文档、`.github/` 配置和 `PPT/` 材料。

## 当前阶段

- 当前处于第一周基础建设阶段。
- 优先处理项目骨架、ER 与 DDL 产物、登录联调链路规划、部署目录预留。
- 除非用户明确要求，否则不要提前展开完整业务模块实现。

## 技术栈约束

- 前端：Vue 3 + Element Plus
- 后端：Spring Boot 3 + MyBatis-Plus
- 数据库：MySQL
- 部署预留：Docker Compose + Nginx

## 目录边界

- `frontend/`：仅放前端应用相关内容。
- `backend/`：仅放后端应用相关内容。
- `sql/`：放 ER、DDL、初始化脚本、数据字典等数据库产物。
- `deploy/`：放部署配置、环境变量模板、部署记录等内容。
- `docs/`：放应用设计文档、接口约定、模块说明、联调记录、验收记录。

## 文档落点规则

- 仓库根目录 `docs/`：用于课程协作、任务分配、GitHub 流程、项目管理类文档。
- 项目目录 `graduation-attainment-platform/docs/`：用于应用本身的技术设计与开发记录。
- 如果任务属于应用实现或联调，不要把内容误写到仓库根 `docs/`。

## AI 协作规则

- 保持当前“中等粒度骨架”优先，不要擅自替不同组员补齐过深的内部工程结构。
- 做结构性调整前，先检查现有目录职责，避免把前端、后端、数据库、部署内容混放。
- 优先添加可追踪、可解释的最小改动，避免堆叠大量推测性的模板代码。
- 当任务横跨多个目录时，先按边界拆分，再分别落到正确目录。

## 技能目录说明

- 项目内 repo-local skill 统一放在 `.codex/skills/`。
- 每个 skill 使用独立目录组织，目录名即 skill 名称，至少包含 `SKILL.md`。
- 如需额外 agent 配置，放在对应 skill 目录下的 `agents/` 子目录。
- 当前已落地 skill：`action-api-design`。
- 后续如果需要扩展具体 skill，可优先按以下规划名称创建：
  - `project-orchestrator`
  - `frontend-vue-admin`
  - `backend-spring-auth`
  - `database-er-ddl`
  - `deploy-compose-nginx`
  - `project-docs-collab`

## 工作方式

- 先阅读现有项目文档，再做结构性判断。
- 输出内容要对齐当前课程阶段目标和小组分工。
- 涉及 `/health`、`/login`、登录联调、ER、DDL、部署预留等第一周目标时，优先保证边界清晰和落点正确。
