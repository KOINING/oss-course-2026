# 面向专业认证的毕业要求达成度统一计算平台

## 项目目标

本项目面向专业认证场景，目标是建设一套用于毕业要求达成度统一计算与支撑分析的平台。  

## 技术栈约定

- 前端：Vue 3 + Element Plus
- 后端：Spring Boot 3 + MyBatis-Plus
- 数据库：MySQL
- 部署预留：Docker Compose + Nginx

建议参考课程当前约定的版本矩阵推进实现：

- JDK 21
- Spring Boot 3.5.x
- MyBatis-Plus 3.5.x
- Node.js 22 LTS
- Vue 3.5.x
- Vite 7.x
- Element Plus 2.11.x
- MySQL 8.x

## 实际目录结构树

```text
graduation-attainment-platform/
├── README.md
├── AGENTS.md
├── .agents/
│   └── skills/
│       └── README.md
├── frontend/
│   ├── Dockerfile
│   └── README.md
├── backend/
│   ├── Dockerfile
│   └── README.md
├── sql/
│   └── README.md
├── deploy/
│   ├── docker-compose.yml
│   ├── nginx.conf
│   ├── .env.example
│   └── README.md
└── docs/
    └── README.md
```

## 各目录职责

- `frontend/`：前端工程目录，后续放置 Vue 3 管理后台代码。
- `backend/`：后端工程目录，后续放置 Spring Boot 3 服务代码。
- `sql/`：数据库相关交付物目录，后续放置 ER、DDL、初始化脚本、数据字典等。
- `deploy/`：部署相关目录，当前已补充 Docker Compose、Nginx、环境变量模板和部署说明。
- `docs/`：应用本身的技术文档目录，存放接口约定、模块说明、联调记录、验收记录等。
- `AGENTS.md`：项目级 AI 协作规则说明文件。
- `.agents/skills/README.md`：项目内 AI skill 规划说明。

## AI 协作说明

如果使用 AI agent 参与本目录下的开发，请优先阅读：

- AGENTS.md
- .agents/skills/README.md

其中：

- `AGENTS.md` 用于说明本项目目录中的 AI 协作边界和工作规则。
- `.agents/skills/README.md` 用于记录项目内 skill 与适用场景。
