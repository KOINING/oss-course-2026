# oss-course-2026

开源软件通识课程小组协作仓库。当前主项目为**面向专业认证的毕业要求达成度统一计算平台**，用于支撑工程教育专业认证场景下的基础数据维护、课程与教学班管理、学生主数据与关联导入、毕业要求指标点配置、课程支撑矩阵维护，以及后续课程级、专业级达成度计算与报表分析。

本仓库同时承担代码托管、课程过程文档、任务分配、测试数据、阶段汇报材料和部署说明的统一管理。

仓库地址：

- [KOINING/oss-course-2026](https://github.com/KOINING/oss-course-2026)

## 1. 项目概览

本项目的业务目标是将“专业认证”相关的关键数据链路整合到一个统一平台中，形成从基础字典到课程支撑关系、再到达成度计算与结果分析的完整闭环。当前已落地的核心方向包括：

- 系统管理员维护基础字典、账号与角色；
- 教务管理人员维护课程、教学班、学生主数据，并执行批量数据导入；
- 专业负责人维护毕业要求、指标点以及课程-指标点支撑矩阵；
- 任课教师面向课程级目标、考核点、成绩与达成度计算开展后续业务。

当前在线访问地址：

- [http://39.104.52.187](http://39.104.52.187)

当前默认测试账号：

- 系统管理员：`admin / 123456`
- 教务管理人员：`academic_wu / 123456`
- 专业负责人：`director_chen / 123456`
- 任课教师：`teacher_zhang / 123456`

初始化账号、课程、教学班、学生等基础数据可参考：

- [graduation-attainment-platform/sql/GRA_db.sql](https://github.com/KOINING/oss-course-2026/blob/main/graduation-attainment-platform/sql/GRA_db.sql)

## 2. 技术栈

### 前端

- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router

对应工程：

- [graduation-attainment-platform/frontend](https://github.com/KOINING/oss-course-2026/tree/main/graduation-attainment-platform/frontend)

### 后端

- Spring Boot 3.5.x
- MyBatis-Plus 3.5.x
- Spring Validation
- Springdoc OpenAPI
- JWT
- Apache POI
- BCrypt

对应工程：

- [graduation-attainment-platform/backend](https://github.com/KOINING/oss-course-2026/tree/main/graduation-attainment-platform/backend)

### 数据库与部署

- MySQL 8.x
- Nginx
- Docker / Docker Compose
- GitHub Actions

部署相关目录：

- [graduation-attainment-platform/deploy](https://github.com/KOINING/oss-course-2026/tree/main/graduation-attainment-platform/deploy)

## 3. 环境要求

建议的本地开发环境：

- JDK 21
- Maven 3.9+
- Node.js 22+
- npm 10+
- MySQL 8.x

服务器运行环境建议：

- Linux（当前线上为 Ubuntu）
- JDK 21
- MySQL 8.x
- Nginx

## 4. 仓库结构

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

各目录职责如下：

- `.github/`：GitHub Actions、仓库协作流程等配置；
- `.claude/`：本地协作工具相关目录；
- `.codex/`：仓库级 AI 协作配置与技能说明；
- `docs/`：课程任务分配、需求分析、测试流程、阶段文档与过程记录；
- `graduation-attainment-platform/`：主应用目录；
- `graduation-attainment-platform/frontend/`：前端管理后台工程；
- `graduation-attainment-platform/backend/`：后端服务工程；
- `graduation-attainment-platform/sql/`：数据库初始化脚本、周任务数据脚本、数据字典与说明；
- `graduation-attainment-platform/deploy/`：部署配置、环境变量模板与部署说明；
- `graduation-attainment-platform/docs/`：应用级联调、接口和设计说明；
- `PPT/`：课程汇报与答辩材料。

## 5. 当前系统已落地的主要功能

基于当前代码与线上系统，已具备或基本具备以下能力。

### 5.1 系统管理

- 登录认证与角色鉴权；
- 账号与角色管理；
- 系统管理员菜单按角色边界收口。

### 5.2 基础数据与主数据

- 学年学期管理；
- 学院管理；
- 专业管理；
- 课程管理；
- 教学班管理；
- 学生基础信息管理。

### 5.3 数据导入

当前已形成三条独立导入链：

- 课程清单导入；
- 学生基础信息导入；
- 教学班学生关联导入。

### 5.4 专业认证配置

- 毕业要求管理；
- 指标点管理；
- 课程-指标点宏观支撑矩阵配置；
- 支撑矩阵列权重校验与界面交互优化。

## 6. 本地运行说明

### 6.1 后端运行

后端配置采用环境变量方式驱动，核心配置文件为：

- [graduation-attainment-platform/backend/src/main/resources/application.yml](https://github.com/KOINING/oss-course-2026/blob/main/graduation-attainment-platform/backend/src/main/resources/application.yml)

关键环境变量包括：

```env
SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/GraduationDB?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=graduation_app
SPRING_DATASOURCE_PASSWORD=你的数据库密码
JWT_SECRET=你的JWT密钥
JWT_EXPIRATION=86400000
```

典型启动步骤：

```bash
cd graduation-attainment-platform/backend
mvn spring-boot:run
```

### 6.2 前端运行

```bash
cd graduation-attainment-platform/frontend
npm install
npm run dev
```

默认情况下，前端通过 `/api` 代理访问后端。

## 7. 数据库初始化说明

数据库主初始化脚本：

- [graduation-attainment-platform/sql/GRA_db.sql](https://github.com/KOINING/oss-course-2026/blob/main/graduation-attainment-platform/sql/GRA_db.sql)

第三周扩展数据脚本：

- [graduation-attainment-platform/sql/week3_init_data.sql](https://github.com/KOINING/oss-course-2026/blob/main/graduation-attainment-platform/sql/week3_init_data.sql)

注意事项：

- `GRA_db.sql` 会重建 `GraduationDB`；
- 执行前需要确认是否允许覆盖现有业务数据；
- 若执行 `week3_init_data.sql`，应先确保它与当前主库结构保持一致；
- 若只需要基础可运行环境，优先以 `GRA_db.sql` 为准。

## 8. 部署说明

部署目录见：

- [graduation-attainment-platform/deploy/README.md](https://github.com/KOINING/oss-course-2026/blob/main/graduation-attainment-platform/deploy/README.md)

当前部署思路：

- 前端构建静态资源；
- 后端构建 Spring Boot 可执行 jar；
- Nginx 对外提供站点入口与 `/api` 代理；
- MySQL 作为服务器独立服务部署，不与前后端容器强绑定。

如果进行服务器部署，建议优先准备：

- 独立 MySQL 实例；
- 应用运行账号与数据库权限；
- 后端 `.env`；
- Nginx 反向代理配置；
- GitHub Actions 或服务器脚本化发布流程。

## 9. 文档入口

推荐首先阅读以下文档：

- 应用级说明：  
  [graduation-attainment-platform/README.md](https://github.com/KOINING/oss-course-2026/blob/main/graduation-attainment-platform/README.md)

- 软件需求规格说明书：  
  [docs/软件需求规格说明书.md](https://github.com/KOINING/oss-course-2026/blob/main/docs/%E8%BD%AF%E4%BB%B6%E9%9C%80%E6%B1%82%E8%A7%84%E6%A0%BC%E8%AF%B4%E6%98%8E%E4%B9%A6.md)


## 10. 许可证

本仓库采用：

- [LICENSE](https://github.com/KOINING/oss-course-2026/blob/main/LICENSE)

如需进一步了解应用内部结构、接口命名与部署细节，请继续阅读 `graduation-attainment-platform` 目录下的应用级 README 和相关文档。
