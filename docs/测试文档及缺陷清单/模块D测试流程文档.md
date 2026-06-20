# 模块 D 测试流程文档

> **测试人员**: 待填写  
> **测试日期**: 2026-06-20  
> **测试分支**: `member/ygp`  
> **对应角色**: 课程主讲教师、专业负责人、教务管理员  
> **测试依据**: 《软件需求规格说明书》、模块 D Issue #170、第五周确认结果表、模块 C 计算结果  

---

## 目录

1. [测试环境与前置条件](#1-测试环境与前置条件)
2. [测试1：课程级评价报表查询与导出功能](#2-测试1课程级评价报表查询与导出功能)
3. [测试2：专业级评价报告查询与导出功能](#3-测试2专业级评价报告查询与导出功能)
4. [测试3：专业级雷达图数据展示功能](#4-测试3专业级雷达图数据展示功能)
5. [测试4：穿透式台账查询、逐层下钻和导出功能](#5-测试4穿透式台账查询逐层下钻和导出功能)
6. [测试5：直接读取结果表验证](#6-测试5直接读取结果表验证)
7. [测试6：双专业、双年级样例不串线验证](#7-测试6双专业双年级样例不串线验证)
8. [测试总结](#8-测试总结)

---

## 1. 测试环境与前置条件

### 1.1 测试环境

| 项目 | 配置 |
|------|------|
| 操作系统 | Windows 10 / Windows 11 |
| JDK | 21 |
| 后端框架 | Spring Boot 3.5.x + MyBatis-Plus 3.5.x |
| 前端框架 | Vue 3.5.x + Element Plus 2.11.x + Vite 7.x |
| 数据库 | MySQL 8.x，库名 `GraduationDB` |
| 浏览器 | Chrome / Edge 最新版 |
| 测试工具 | Apifox / Swagger / 浏览器 |
| 后端地址 | `http://localhost:8080` |
| 前端地址 | `http://localhost:5173` |
| Swagger | `http://localhost:8080/swagger-ui/index.html` |

### 1.2 测试账号

| 角色 | 账号类型 | 用途 |
|------|----------|------|
| 课程主讲教师 | `instructor` | 课程级评价报表查询与导出 |
| 专业负责人 | `program_director` | 专业级评价报告、雷达图、穿透台账查询与导出 |
| 教务管理员 | `academic_affairs` | 专业级评价报告、雷达图、穿透台账查询与导出 |

### 1.3 前置数据准备

测试需要以下数据已通过模块 A/B/C 配置或计算完成：

| 序号 | 数据项 | 状态 |
|------|--------|------|
| 1 | 至少 2 个专业，例如计算机科学与技术、软件工程 | 需已创建 |
| 2 | 至少 2 个年级，例如 2022 级、2023 级 | 需已创建 |
| 3 | 课程与专业年级绑定关系 | 需已配置 |
| 4 | 教学班、任课教师、学生名单 | 需已配置 |
| 5 | 课程目标、考核点、内部权重 `w` | 需已配置 |
| 6 | 课程-毕业要求指标点支撑矩阵、宏观权重 `W` | 需已配置 |
| 7 | 学生原始成绩 | 需已导入 |
| 8 | 课程级计算结果 `Cj`、`Ek` | 需已生成 |
| 9 | 专业级计算结果 `Gk` | 需已生成 |

### 1.4 模块 D 涉及核心数据表

| 表名 | 用途 | 数据来源 |
|------|------|----------|
| `course_objective_achievement` | 课程目标达成度结果 | 模块 C 课程级计算 |
| `course_indicator_achievement` | 课程级指标点达成度结果 `Ek` | 模块 C 课程级计算 |
| `major_indicator_achievement` | 专业级指标点达成度结果 `Gk` | 模块 C 专业级计算 |
| `student_assessment_score` | 学生原始成绩 | 模块 C 成绩导入 |
| `course_indicator_support` | 课程到指标点的宏观权重 `W` | 支撑矩阵配置 |
| `objective_indicator_contribution` | 课程目标到指标点的内部权重 `w` | 模块 B 配置 |
| `teaching_class` | 教学班、专业年级、计算状态 | 模块 A/C |

### 1.5 通用请求头

Apifox 或 Swagger 测试时统一携带：

```http
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json
```

---

## 2. 测试1：课程级评价报表查询与导出功能

**测试角色**: 课程主讲教师、专业负责人、教务管理员  
**对应接口**: `POST /api/teacher/report/data`、`POST /api/teacher/report/export/excel`、`POST /api/teacher/report/export/pdf`  
**前端页面**: `CourseReportView.vue`

### 2.1 测试目标

验证课程级评价报表能够按 `课程 + 专业 + 年级` 维度查询并导出，报表应包含：

- 课程基本信息
- 专业、年级信息
- 教学班明细
- 考核点平均分
- 课程目标达成度
- 课程级指标点达成度 `Ek`
- Excel / PDF 导出文件

### 2.2 测试用例

#### TC1.1：正常查询课程级评价报表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 以课程主讲教师身份登录 | 登录成功 |
| 2 | 进入课程级评价报表页面 | 页面正常加载 |
| 3 | 选择课程、专业、年级 | 查询条件可正常选择 |
| 4 | 点击"查询报表" | 返回课程级报表数据 |
| 5 | 检查报表内容 | 展示教学班、考核点平均分、课程目标达成度、课程级指标点达成度 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/teacher/report/data \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "courseId": 1,
    "majorId": 1,
    "gradeYear": 2022
  }'
```

#### TC1.2：导出课程级评价报表 Excel

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 在课程级评价报表页面点击"导出 Excel" | 触发浏览器下载 |
| 2 | 打开下载的 Excel 文件 | 文件可正常打开 |
| 3 | 检查文件内容 | 包含课程信息、教学班明细、考核点平均分、课程目标达成度、指标点达成度 |
| 4 | 对比页面数据 | 导出数据与页面查询结果一致 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/teacher/report/export/excel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o course-report.xlsx \
  -d '{
    "courseId": 1,
    "majorId": 1,
    "gradeYear": 2022
  }'
```

#### TC1.3：导出课程级评价报表 PDF

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 在课程级评价报表页面点击"导出 PDF" | 触发浏览器下载 |
| 2 | 打开下载的 PDF 文件 | 文件可正常打开 |
| 3 | 检查中文内容 | 无乱码、无缺字 |
| 4 | 对比页面数据 | PDF 内容与页面查询结果一致 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/teacher/report/export/pdf \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o course-report.pdf \
  -d '{
    "courseId": 1,
    "majorId": 1,
    "gradeYear": 2022
  }'
```

#### TC1.4：课程级报表权限校验

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 使用无关角色账号调用课程级报表接口 | 返回权限不足 |
| 2 | 使用课程主讲教师账号调用自己课程报表 | 正常返回 |
| 3 | 使用专业负责人或教务管理员调用 | 正常返回 |

### 2.3 测试结论

| 状态 | 说明 |
|------|------|
| ⬜ 待验证 | 课程级报表查询、Excel 导出、PDF 导出需要在联调环境逐项验证 |

---

## 3. 测试2：专业级评价报告查询与导出功能

**测试角色**: 专业负责人、教务管理员  
**对应接口**: `POST /api/report/majorReport`、`POST /api/report/majorReport/export`  
**前端页面**: `ResultViewEntry.vue`、专业级报告相关页面

### 3.1 测试目标

验证专业级评价报告能够按 `专业 + 年级` 查询和导出，报告应直接读取 `major_indicator_achievement` 结果表，并展示：

- 专业基本信息
- 年级信息
- 专业级指标点达成度 `Gk`
- 支撑课程贡献明细
- 数据源说明

### 3.2 测试用例

#### TC2.1：正常查询专业级评价报告

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 以专业负责人身份登录 | 登录成功 |
| 2 | 进入专业级结果页面 | 页面正常加载 |
| 3 | 选择专业和年级 | 查询条件可正常选择 |
| 4 | 点击查询 | 返回专业级评价报告数据 |
| 5 | 检查指标点结果 | 展示每个指标点的 `Gk` |
| 6 | 检查支撑课程明细 | 展示课程、教学班、`Ek`、`W`、加权贡献 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/report/majorReport \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "majorId": 1,
    "gradeYear": 2022
  }'
```

#### TC2.2：导出专业级评价报告 Excel

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 在专业级结果页面点击"导出专业级报告" | 触发浏览器下载 |
| 2 | 打开下载的 Excel 文件 | 文件可正常打开 |
| 3 | 检查指标点汇总 | 包含 `Gk` 汇总结果 |
| 4 | 检查支撑课程明细 | 包含支撑课程贡献明细 |
| 5 | 对比页面数据 | 导出数据与页面查询结果一致 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/report/majorReport/export \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o major-report.xlsx \
  -d '{
    "majorId": 1,
    "gradeYear": 2022
  }'
```

#### TC2.3：未生成专业级结果时提示

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 选择未完成专业级计算的专业和年级 | — |
| 2 | 查询专业级报告 | 返回 `resultReady=false` 或明确提示 |
| 3 | 尝试导出专业级报告 | 应拒绝导出，提示先完成课程级锁定和专业级计算 |

### 3.3 测试结论

| 状态 | 说明 |
|------|------|
| ⬜ 待验证 | 专业级报告查询和导出需要在具备专业级结果的样例数据下验证 |

---

## 4. 测试3：专业级雷达图数据展示功能

**测试角色**: 专业负责人、教务管理员  
**对应接口**: `POST /api/report/majorRadar`  
**前端页面**: `ProfessionalRadarChart.vue`、`ResultViewEntry.vue`

### 4.1 测试目标

验证专业级雷达图能够从专业级评价报告统一结果源中读取数据，按指标点展示 `Gk`。

雷达图应包含：

- 指标点轴标签
- 指标点描述
- 各指标点 `Gk` 数值
- 参考线配置
- 图片导出能力（前端页面）

### 4.2 测试用例

#### TC3.1：查询雷达图数据

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 以专业负责人身份登录 | 登录成功 |
| 2 | 选择专业和年级 | 查询条件可正常选择 |
| 3 | 调用雷达图接口 | 返回雷达图数据 |
| 4 | 检查 `indicators` | 包含指标点编码和描述 |
| 5 | 检查 `series.data` | 包含各指标点 `Gk` 数值 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/report/majorRadar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "majorId": 1,
    "gradeYear": 2022
  }'
```

#### TC3.2：前端雷达图展示

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 进入专业级结果页面 | 页面正常加载 |
| 2 | 查询已生成专业级结果的专业年级 | 雷达图区域出现 |
| 3 | 检查雷达图轴 | 指标点编码正确显示 |
| 4 | 鼠标悬停或查看标签 | 可查看指标点达成度 |
| 5 | 点击导出雷达图 | 可下载图片 |

### 4.3 测试结论

| 状态 | 说明 |
|------|------|
| ⬜ 待验证 | 雷达图接口与前端展示需要结合真实专业级结果验证 |

---

## 5. 测试4：穿透式台账查询、逐层下钻和导出功能

**测试角色**: 专业负责人、教务管理员  
**对应接口**: `POST /api/achievementTrace/getMajorToCourseTrace`、`POST /api/achievementTrace/getCourseToObjectiveTrace`、`POST /api/achievementTrace/getObjectiveToScoreTrace`、`POST /api/achievementTrace/exportAchievementLedger`  
**前端页面**: `DrillDownLedgerView.vue`

### 5.1 测试目标

验证系统能够从专业级指标点 `Gk` 逐层追溯到课程级指标点 `Ek`、课程目标、考核点和学生原始成绩，并支持导出穿透式台账 Excel。

追溯链路：

```
专业级 Gk
  -> 课程级 Ek
  -> 课程目标达成度 Cj
  -> 考核点
  -> 学生原始成绩 actualScore
```

### 5.2 测试用例

#### TC4.1：专业级 Gk 下钻到课程级 Ek

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 以专业负责人身份登录 | 登录成功 |
| 2 | 选择专业、年级、指标点 | 查询条件正确 |
| 3 | 调用第一层下钻接口 | 返回 `Gk` 和支撑课程列表 |
| 4 | 检查课程贡献 | 每门课程包含 `Ek`、`W`、加权贡献 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/achievementTrace/getMajorToCourseTrace \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "majorId": 1,
    "gradeYear": 2022,
    "ipId": 1
  }'
```

#### TC4.2：课程级 Ek 下钻到课程目标

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 从 TC4.1 返回结果中选择一个 `classId` | 获得有效教学班 |
| 2 | 调用第二层下钻接口 | 返回课程目标贡献明细 |
| 3 | 检查课程目标数据 | 包含课程目标达成度、内部权重 `w`、加权贡献 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/achievementTrace/getCourseToObjectiveTrace \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "classId": 1,
    "ipId": 1
  }'
```

#### TC4.3：课程目标下钻到考核点和原始成绩

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 从 TC4.2 返回结果中选择一个 `coId` | 获得有效课程目标 |
| 2 | 调用第三层下钻接口 | 返回考核点和学生成绩 |
| 3 | 检查考核点 | 包含考核点名称、满分、平均分 |
| 4 | 检查学生成绩 | 包含学号、姓名、原始成绩 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/achievementTrace/getObjectiveToScoreTrace \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "classId": 1,
    "coId": 1
  }'
```

#### TC4.4：导出穿透式台账 Excel

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 在穿透式台账页面选择专业和年级 | 查询条件正确 |
| 2 | 点击"导出台账" | 触发 Excel 下载 |
| 3 | 打开 Excel 文件 | 文件可正常打开 |
| 4 | 检查字段 | 包含专业、年级、指标点、课程、课程目标、考核点、学生、原始成绩 |
| 5 | 对比页面数据 | 导出内容与下钻查询结果一致 |

接口请求：

```bash
curl -X POST http://localhost:8080/api/achievementTrace/exportAchievementLedger \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o achievement-ledger.xlsx \
  -d '{
    "majorId": 1,
    "gradeYear": 2022
  }'
```

### 5.3 测试结论

| 状态 | 说明 |
|------|------|
| ⬜ 待验证 | 穿透式台账查询、逐层下钻和导出需按真实结果数据验证 |

---

## 6. 测试5：直接读取结果表验证

### 6.1 测试目标

验证模块 D 的查询和导出接口只是读取模块 C 已经确认的结果表，不在报表查询、报告导出、雷达图展示、台账查询时重新计算或改写结果。

### 6.2 验证表

| 功能 | 应读取表 |
|------|----------|
| 课程目标达成度 | `course_objective_achievement` |
| 课程级指标点达成度 | `course_indicator_achievement` |
| 专业级指标点达成度 | `major_indicator_achievement` |
| 原始成绩追溯 | `student_assessment_score` |

### 6.3 测试用例

#### TC5.1：课程级报表读取课程级结果表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 查询 `course_objective_achievement` 和 `course_indicator_achievement` 中某教学班结果 | 记录原始值 |
| 2 | 调用课程级报表查询接口 | 接口返回值与结果表一致 |
| 3 | 调用课程级报表导出接口 | 导出值与结果表一致 |
| 4 | 再次查询结果表 | 结果表值未被改写 |

示例 SQL：

```sql
SELECT class_id, co_id, average_achievement
FROM course_objective_achievement
WHERE class_id = 1;

SELECT class_id, ip_id, achievement
FROM course_indicator_achievement
WHERE class_id = 1;
```

#### TC5.2：专业级报告和雷达图读取专业级结果表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 查询 `major_indicator_achievement` 中某专业年级结果 | 记录 `Gk` 原始值 |
| 2 | 调用专业级报告查询接口 | 接口返回值与结果表一致 |
| 3 | 调用雷达图接口 | 雷达图值与结果表一致 |
| 4 | 调用专业级报告导出接口 | 导出值与结果表一致 |
| 5 | 再次查询结果表 | 结果表值未被改写 |

示例 SQL：

```sql
SELECT major_id, grade_year, term_id, ip_id, final_achievement
FROM major_indicator_achievement
WHERE major_id = 1 AND grade_year = 2022;
```

#### TC5.3：穿透台账读取结果表和原始成绩表

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 查询 `student_assessment_score` 中某教学班原始成绩 | 记录原始分数 |
| 2 | 调用穿透式台账下钻接口 | 返回原始成绩与表中一致 |
| 3 | 调用台账导出接口 | Excel 中原始成绩与表中一致 |
| 4 | 再次查询结果表和原始成绩表 | 表数据未被改写 |

### 6.4 测试结论

| 状态 | 说明 |
|------|------|
| ⬜ 待验证 | 需通过数据库查询前后对比确认无临时重算和无结果表改写 |

---

## 7. 测试6：双专业、双年级样例不串线验证

### 7.1 测试目标

验证模块 D 在双专业、双年级样例下按 `majorId + gradeYear` 精确过滤，避免课程级报表、专业级报告、雷达图和穿透台账混入其他专业或其他年级数据。

### 7.2 验证组合

| 编号 | 专业 | 年级 | 预期结果 |
|------|------|------|----------|
| TC6.1 | 计算机科学与技术 | 2022 | 只返回计科 2022 数据 |
| TC6.2 | 计算机科学与技术 | 2023 | 只返回计科 2023 数据 |
| TC6.3 | 软件工程 | 2022 | 只返回软工 2022 数据 |
| TC6.4 | 软件工程 | 2023 | 只返回软工 2023 数据 |

### 7.3 测试用例

#### TC6.1：课程级报表不串线

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 使用同一课程分别查询不同专业年级 | 每组请求都返回对应专业年级数据 |
| 2 | 检查教学班 | 不出现其他专业或年级教学班 |
| 3 | 检查学生人数和成绩 | 不出现其他专业或年级学生 |

#### TC6.2：专业级报告和雷达图不串线

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 分别查询四组专业年级组合 | 均能按条件返回 |
| 2 | 检查 `Gk` 列表 | 只属于目标专业年级 |
| 3 | 检查雷达图轴和值 | 与目标专业年级指标点一致 |

#### TC6.3：穿透式台账不串线

| 步骤 | 操作 | 预期结果 |
|------|------|----------|
| 1 | 分别导出四组专业年级台账 | 均能正常导出 |
| 2 | 检查 Excel 中专业、年级列 | 与请求条件一致 |
| 3 | 检查学生学号和姓名 | 不包含其他专业或年级学生 |
| 4 | 检查课程和指标点 | 与目标专业年级支撑矩阵一致 |

### 7.4 测试结论

| 状态 | 说明 |
|------|------|
| ⬜ 待验证 | 需要用双专业、双年级样例逐组验证导出结果不串线 |

---

## 8. 测试总结

### 8.1 测试统计

| 测试项 | 用例数 | 状态 | 备注 |
|--------|--------|------|------|
| 课程级评价报表查询与导出 | 4 | ⬜ 待验证 | 查询、Excel、PDF、权限 |
| 专业级评价报告查询与导出 | 3 | ⬜ 待验证 | 查询、导出、未就绪提示 |
| 专业级雷达图展示 | 2 | ⬜ 待验证 | 接口数据、前端展示 |
| 穿透式台账查询和导出 | 4 | ⬜ 待验证 | 三层下钻、Excel 导出 |
| 直接读取结果表验证 | 3 | ⬜ 待验证 | 结果表前后值不变 |
| 双专业双年级不串线 | 3 | ⬜ 待验证 | 专业年级隔离 |
| **合计** | **19** | ⬜ 待验证 | — |

### 8.2 关键检查点

| 编号 | 检查点 | 通过标准 |
|------|--------|----------|
| D-CHECK-01 | 课程级报表导出可用 | Excel/PDF 可下载、可打开、内容完整 |
| D-CHECK-02 | 专业级报告导出可用 | Excel 可下载、可打开、包含 `Gk` 和支撑课程明细 |
| D-CHECK-03 | 雷达图可用 | 雷达图数据和页面展示正常 |
| D-CHECK-04 | 穿透台账可用 | 可从 `Gk` 逐层下钻到原始成绩，可导出 |
| D-CHECK-05 | 直接读取结果表 | 查询和导出不改写结果表 |
| D-CHECK-06 | 双专业双年级不串线 | 查询和导出结果严格匹配 `majorId + gradeYear` |

### 8.3 总体评估

模块 D 的测试重点不在重新验证计算公式，而在验证**报表读取、结果展示、文件导出、逐层追溯和数据隔离**是否正确。

本轮测试完成后应形成：

1. 课程级报表查询与导出测试结论
2. 专业级报告查询与导出测试结论
3. 雷达图展示测试结论
4. 穿透式台账下钻与导出测试结论
5. 双专业、双年级不串线验证结论
6. 模块 D 缺陷清单

### 8.4 完成标准

| 完成项 | 状态 |
|--------|------|
| 课程级报表导出可用 | ⬜ 待验证 |
| 专业级报告和雷达图可用 | ⬜ 待验证 |
| 穿透式台账查询与导出可用 | ⬜ 待验证 |
| 双专业、双年级样例下导出结果不串线 | ⬜ 待验证 |
| 模块 D 测试流程文档已形成 | ✅ 已完成 |
| 模块 D 缺陷清单已形成 | ✅ 已完成 |

---

> **附录 A**: 接口列表（模块 D 相关）
>
> | 方法 | 路径 | 说明 | 角色 |
> |------|------|------|------|
> | POST | `/api/teacher/report/data` | 查询课程级评价报表 | 教师/专业负责人/教务 |
> | POST | `/api/teacher/report/export/excel` | 导出课程级评价报表 Excel | 教师/专业负责人/教务 |
> | POST | `/api/teacher/report/export/pdf` | 导出课程级评价报表 PDF | 教师/专业负责人/教务 |
> | POST | `/api/report/majorReport` | 查询专业级评价报告 | 专业负责人/教务 |
> | POST | `/api/report/majorReport/export` | 导出专业级评价报告 Excel | 专业负责人/教务 |
> | POST | `/api/report/majorRadar` | 查询专业级雷达图数据 | 专业负责人/教务 |
> | POST | `/api/achievementTrace/getMajorToCourseTrace` | 专业级 `Gk` 下钻到课程级 `Ek` | 专业负责人/教务 |
> | POST | `/api/achievementTrace/getCourseToObjectiveTrace` | 课程级 `Ek` 下钻到课程目标 | 专业负责人/教务 |
> | POST | `/api/achievementTrace/getObjectiveToScoreTrace` | 课程目标下钻到考核点和原始成绩 | 专业负责人/教务 |
> | POST | `/api/achievementTrace/exportAchievementLedger` | 导出穿透式台账 Excel | 专业负责人/教务 |

> **附录 B**: 关键参考文件
> - 模块 D 缺陷清单：`docs/测试文档及缺陷清单/模块D缺陷清单.md`
> - 穿透式台账接口测试：`graduation-attainment-platform/docs/达成度逐层追溯接口测试.md`
> - 课程级评价报表接口测试：`graduation-attainment-platform/docs/course-report-api-test.md`
> - 后端报表接口：`CourseReportController.java`、`ReportController.java`、`AchievementTraceController.java`
> - 前端页面：`CourseReportView.vue`、`ResultViewEntry.vue`、`ProfessionalRadarChart.vue`、`DrillDownLedgerView.vue`
