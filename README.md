# oss-course-2026

开源软件通识课程小组协作项目。

## 项目简介

本仓库用于开源软件通识课程的小组协作开发实践。小组成员将通过 GitHub 完成项目文档编写、任务分配、分支开发、Pull Request 提交、代码 Review 和版本发布等开源协作流程。

本项目旨在帮助小组成员熟悉开源软件开发的基本流程，包括：

- 使用 GitHub 管理项目代码和文档
- 使用 Issue 进行任务分配与进度跟踪
- 使用分支进行独立开发
- 使用 Pull Request 进行代码合并申请
- 使用 Code Review 提高项目质量
- 使用 Markdown 编写项目文档

## 小组成员

详细的小组成员信息、学号、组长信息和 GitHub 账号请查看：

[小组成员信息](docs/team.md)

## 项目文档入口

项目本身的技术设计与开发文档统一放在 `graduation-attainment-platform/docs/` 下。

当前可直接查看：

- [系统总体架构设计](graduation-attainment-platform/docs/系统总体架构设计.md)


## 仓库结构

```text
oss-course-2026/
├── README.md                                  # 仓库总说明
├── LICENSE                                    # 开源许可证
├── .gitignore                                 # Git 忽略规则
├── .github/                                   # GitHub 协作模板与 Actions 工作流
│   ├── ISSUE_TEMPLATE/
│   ├── pull_request_template.md
│   └── workflows/
├── docs/                                      # 课程协作与项目管理文档
├── graduation-attainment-platform/            # 应用主目录
│   ├── README.md                              # 应用级说明文档
│   ├── AGENTS.md                              # 项目级 AI 协作规则
│   ├── .codex/                                # 项目内 AI 协作配置目录
│   ├── frontend/                              # 前端工程目录
│   ├── backend/                               # 后端工程目录
│   ├── sql/                                   # 数据库交付物目录
│   ├── deploy/                                # 部署配置目录
│   └── docs/                                  # 应用设计与开发文档
└── PPT/                                       # 课程汇报材料
```

## 各目录职责

- `.github/`：存放 GitHub Issue、Pull Request 模板和 Actions 工作流配置。
- `docs/`：存放课程协作、任务分配、GitHub 流程和项目管理相关文档。
- `graduation-attainment-platform/`：应用主目录，集中管理系统实现、部署配置和技术文档。
- `graduation-attainment-platform/.codex/skills/`：存放项目内 repo-local AI skills 和相关协作配置。
- `PPT/`：存放课程汇报材料与演示文档。

