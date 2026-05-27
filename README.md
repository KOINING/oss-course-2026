# oss-course-2026

开源软件通识课程小组协作仓库。当前主项目为“面向专业认证的毕业要求达成度统一计算平台”。

## 项目简介

本仓库用于课程小组的协同开发、文档编写、任务分配、分支开发、Pull Request 合并与阶段验收。

当前核心应用位于：

- [graduation-attainment-platform](C:/Users/32484/Desktop/开源软件通识/oss-course-2026/graduation-attainment-platform)

应用级说明可见：

- [graduation-attainment-platform/README.md](C:/Users/32484/Desktop/开源软件通识/oss-course-2026/graduation-attainment-platform/README.md)

小组成员信息可见：

- [docs/team.md](C:/Users/32484/Desktop/开源软件通识/oss-course-2026/docs/team.md)

## 最新仓库结构

```text
oss-course-2026/
├─ README.md
├─ LICENSE
├─ .gitignore
├─ .github/
├─ .claude/
├─ .codex/
├─ docs/
├─ graduation-attainment-platform/
│  ├─ README.md
│  ├─ AGENTS.md
│  ├─ frontend/
│  ├─ backend/
│  ├─ sql/
│  ├─ deploy/
│  └─ docs/
└─ PPT/
```

## 各目录职责

- `.github/`：GitHub Actions、Issue/PR 模板等仓库协作配置。
- `.claude/`：本地协作工具相关配置目录。
- `.codex/`：仓库级 AI 协作配置与技能目录。
- `docs/`：课程协作、任务分配、验收、测试等项目管理文档。
- `graduation-attainment-platform/`：主应用目录，包含前端、后端、数据库脚本、部署配置和应用文档。
- `graduation-attainment-platform/frontend/`：Vue 3 + Vite + Element Plus 前端工程。
- `graduation-attainment-platform/backend/`：Spring Boot + MyBatis-Plus 后端工程。
- `graduation-attainment-platform/sql/`：数据库交付物，包括建库建表脚本、数据字典、ER 图说明等。
- `graduation-attainment-platform/deploy/`：部署配置目录，包括 `docker-compose.yml`、`nginx.conf`、环境变量模板等。
- `graduation-attainment-platform/docs/`：应用级技术文档、联调记录、接口与设计说明。
- `PPT/`：课程汇报与展示材料。

## 项目使用流程

### 1. 项目访问地址

当前部署访问地址：

- [http://39.104.52.187](http://39.104.52.187)

### 2. 默认测试账号

测试账号如下：

- 系统管理员：`admin / 123456`
- 教务管理员：`academic_wu / 123456`
- 专业负责人：`director_chen / 123456`
- 课程主讲教师：`teacher_zhang / 123456`

如需查看更多初始化账号，可检查：

- [graduation-attainment-platform/sql/GRA_db.sql](C:/Users/32484/Desktop/开源软件通识/oss-course-2026/graduation-attainment-platform/sql/GRA_db.sql)

### 3. 使用步骤

1. 打开项目访问地址：[http://39.104.52.187](http://39.104.52.187)
2. 使用对应角色账号登录系统。
3. 系统管理员可进入“账号与角色管理”“基础数据”页面。
4. 教务管理员可维护基础数据并参与课程/班级相关业务。
5. 专业负责人可进入“毕业要求与指标点”等专业级配置页面。
6. 课程主讲教师可进入课程目标、考核点、成绩录入等课程级业务页面。
