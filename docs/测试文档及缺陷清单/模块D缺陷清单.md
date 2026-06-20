# 模块 D 缺陷清单

## 测试信息

- **测试人员**：叶高平
- **测试日期**：2026-06-20
- **测试环境**：本地代码级测试与后端编译验证
- **测试分支**：`member/ygp`
- **测试范围**：课程级评价报表查询与导出、专业级评价报告查询与导出、专业级雷达图、穿透式台账查询与导出、结果表读取验证、双专业双年级不串线验证
- **测试结果**：后端 `mvn -q -DskipTests compile` 编译通过；以下缺陷来自模块 D 代码级测试和静态联调核对

---

## 缺陷列表

| 缺陷ID | 发现日期 | 模块 | 严重程度 | 描述 | 复现步骤 | 预期结果 | 实际结果 | 状态 |
|--------|----------|------|----------|------|----------|----------|----------|------|
| BUG-D-001 | 2026-06-20 | 数据库脚本 | 严重 | 全量建库脚本和样例数据脚本未同步 `teaching_class.major_id`，但后端实体和报表服务已依赖该字段 | 使用 `gra_db_full.sql` 或 `rebuild_rich_test_data.sql` 重建数据库后启动模块 D 报表查询 | 新库结构应包含 `teaching_class.major_id`，样例教学班应写入专业ID | 后端代码按 `TeachingClass::getMajorId` 查询，脚本中表结构/插入语句未同步，可能导致新库运行报错或数据缺失 | 已确认 |
| BUG-D-002 | 2026-06-20 | 课程级报表 | 严重 | 课程主讲教师只做角色校验，未校验是否为该课程/教学班任课教师 | 任意 `instructor` 登录后构造其他教师的 `courseId + majorId + gradeYear` 调用课程报表查询或导出 | 教师只能查看自己负责教学班的课程级报表 | 只要角色是 `instructor` 即可通过 Controller 校验，存在越权查看风险 | 已确认 |
| BUG-D-003 | 2026-06-20 | 课程级报表 | 一般 | 课程级报表返回的教学班 `termCode` 被写死为空字符串 | 查询课程级报表，查看 `classSummaries`、`classScoreSummaries`、`teachingClasses` 中的 `termCode` | 应显示教学班所属学期编码 | Service 构造响应时多处 `.termCode("")`，前端和导出文件无法区分学期 | 已确认 |
| BUG-D-004 | 2026-06-20 | 专业级报告 | 严重 | 专业级报告按课程汇总贡献时，同一课程存在多个教学班只取第一个教学班 | 同一专业年级同一课程配置两个 locked 教学班后查询专业级报告 | 支撑课程贡献应覆盖该课程下所有相关教学班，或按明确口径聚合 | `classByCourseMap.putIfAbsent(tc.getCourseId(), tc)` 只保留第一个教学班，其他平行班被忽略 | 已确认 |
| BUG-D-005 | 2026-06-20 | 专业级报告/雷达图 | 一般 | 已存在专业级结果时，如果支撑教学班状态后来不是 locked，报告和雷达图会隐藏已有结果 | 先生成 `major_indicator_achievement`，再将某支撑教学班解锁或改为非 locked 后查询专业级报告 | 模块 D 应直接展示已确认的结果表快照，并提示数据可能已过期 | `assembleMajorReport()` 在读取到结果后仍要求所有支撑教学班 locked，否则返回结果未就绪 | 已确认 |
| BUG-D-006 | 2026-06-20 | 穿透式台账 | 一般 | 第二、三层下钻接口缺少专业年级上下文参数，无法保证与第一层 `Gk` 链路同源 | 拿任意 `classId + ipId` 或 `classId + coId` 直接调用下钻接口 | 下钻接口应校验该教学班、指标点、课程目标属于同一个专业年级追溯链路 | 接口只按 `classId/ipId/coId` 查询，缺少 `majorId + gradeYear + termId` 链路约束 | 已确认 |
| BUG-D-007 | 2026-06-20 | 穿透式台账导出 | 轻微 | 台账导出方法存在永远不会执行的旧版导出分支，代码可维护性差 | 阅读 `AchievementTraceServiceImpl.exportAchievementLedger()` | 导出逻辑应只有一套清晰实现 | `rows` 为空时已在 `listLedgerRows()` 抛异常，`if (rows != null)` 永远成立，后续旧版导出逻辑不可达 | 已确认 |

---

## 严重程度说明

| 等级 | 说明 | 示例 |
|------|------|------|
| **阻断** | 主流程无法继续，必须立即修复 | 接口启动失败、数据库无法初始化 |
| **严重** | 功能异常，影响核心业务或数据安全 | 越权访问、报表结果错误、数据串线 |
| **一般** | 功能可绕过，但需要修复 | 字段缺失、状态口径不一致、链路校验不足 |
| **轻微** | 体验或维护性问题，不影响主要功能 | 冗余代码、提示不够友好、文案不统一 |

---

## 测试结果汇总

### 课程级评价报表

| 测试项 | 结果 | 备注 |
|--------|------|------|
| 后端接口编译 | ✅ 通过 | `mvn -q -DskipTests compile` 通过 |
| 课程级报表查询接口 | ⚠️ 存在问题 | 教师越权风险，见 BUG-D-002 |
| 课程级报表导出 Excel/PDF | ⚠️ 存在问题 | 复用查询结果，但教师越权风险仍存在 |
| 学期字段展示 | ❌ 有缺陷 | `termCode` 写死为空，见 BUG-D-003 |

### 专业级评价报告与雷达图

| 测试项 | 结果 | 备注 |
|--------|------|------|
| 专业级报告查询接口 | ⚠️ 存在问题 | 平行教学班只取第一个，见 BUG-D-004 |
| 专业级报告导出 | ⚠️ 存在问题 | 与查询共用结果源，因此继承 BUG-D-004 |
| 专业级雷达图 | ⚠️ 存在问题 | 依赖 `assembleMajorReport()`，继承 BUG-D-005 |
| 结果表读取口径 | ⚠️ 存在问题 | 结果已存在但教学班状态变化后可能隐藏快照，见 BUG-D-005 |

### 穿透式台账

| 测试项 | 结果 | 备注 |
|--------|------|------|
| 第一层 `Gk -> Ek` 下钻 | ✅ 基本可用 | 已校验专业级结果和支撑课程锁定状态 |
| 第二层 `Ek -> 课程目标` 下钻 | ⚠️ 存在问题 | 缺少专业年级上下文，见 BUG-D-006 |
| 第三层 `课程目标 -> 原始成绩` 下钻 | ⚠️ 存在问题 | 缺少专业年级上下文，见 BUG-D-006 |
| 台账 Excel 导出 | ⚠️ 存在问题 | 存在不可达旧代码，见 BUG-D-007 |

### 数据库脚本

| 测试项 | 结果 | 备注 |
|--------|------|------|
| 后端实体与数据库字段一致性 | ❌ 有缺陷 | 实体依赖 `majorId`，全量脚本未同步，见 BUG-D-001 |
| 样例数据可用于双专业双年级测试 | ❌ 有缺陷 | 多个样例插入 `teaching_class` 时未写入 `major_id` |

---

## 详细缺陷说明

---

### BUG-D-001 严重：全量建库脚本未同步 `teaching_class.major_id`

**发现位置**:
- `graduation-attainment-platform/sql/gra_db_full.sql`
- `graduation-attainment-platform/sql/rebuild_rich_test_data.sql`
- `graduation-attainment-platform/sql/upgrade_week4_e2e_test_data.sql`
- `graduation-attainment-platform/sql/week5_sample_data.sql`
- `TeachingClass.java`
- `CourseReportServiceImpl`
- `MajorReportServiceImpl`
- `AchievementTraceMapper`

**问题描述**:
后端 `TeachingClass` 实体已经包含 `majorId` 字段，模块 D 的课程级报表、专业级报告、穿透式台账也大量按 `TeachingClass::getMajorId` 或 `tc.major_id` 过滤。

但全量建库脚本和部分样例数据脚本未同步 `teaching_class.major_id` 字段和插入值，导致新建数据库或重建测试数据后，模块 D 相关查询可能直接失败或查不到数据。

**影响分析**:
这是模块 D 双专业、双年级不串线验证的基础字段。如果数据库脚本没有同步 `major_id`，会导致：

1. 新环境初始化失败或运行时报 SQL 字段不存在。
2. 教学班无法准确归属专业。
3. 课程级报表、专业级报告、穿透台账无法按专业隔离。
4. 双专业、双年级样例测试无法稳定复现。

**建议修复**:
1. 在全量建库脚本 `teaching_class` 表中加入 `major_id BIGINT NOT NULL`。
2. 为 `major_id` 增加外键和索引，例如 `idx_class_major_grade`。
3. 所有 `INSERT INTO teaching_class` 语句同步写入 `major_id`。
4. 已有数据库继续保留增量迁移脚本，用于从学生名单或 `course_major` 回填教学班专业。
5. 回填完成后增加校验 SQL：确认所有教学班 `major_id` 非空。

---

### BUG-D-002 严重：课程主讲教师可越权查看其他教师课程报表

**发现位置**:
- `CourseReportController.ensureCourseReportAccess()`
- `CourseReportServiceImpl.getCourseReport()`

**问题描述**:
课程级报表接口只判断用户角色是否属于 `instructor`、`program_director`、`academic_affairs`，但没有校验当前登录教师是否为目标课程或教学班的任课教师。

这意味着任意课程主讲教师只要知道其他课程的 `courseId + majorId + gradeYear`，就可能查询或导出其他教师的课程评价报表。

**影响分析**:
课程级报表包含教学班、学生成绩统计、课程目标达成度和指标点达成度，属于课程评价业务数据。教师越权访问会造成数据权限边界失效。

**建议修复**:
1. Controller 从请求属性中读取 `userId`。
2. 将 `userId` 解析为教师 `teacherId`。
3. 当角色为 `instructor` 时，只允许查询 `teaching_class.teacher_id = 当前教师ID` 的数据。
4. `program_director` 和 `academic_affairs` 可保留按专业年级查看权限。
5. 导出接口复用同一套权限校验，避免只修查询不修导出。

---

### BUG-D-003 一般：课程级报表 `termCode` 被写死为空

**发现位置**:
- `CourseReportServiceImpl.getCourseReport()`

**问题描述**:
课程级报表响应中，教学班相关对象的 `termCode` 被写死为空字符串：

```java
.termCode("")
```

该问题出现在 `TeachingClassReport`、`ClassSummary`、`ClassScoreSummary` 等返回对象中。

**影响分析**:
如果同一课程同一年级跨多个学期存在教学班，前端页面和导出文件无法区分教学班所属学期，影响报表归档口径。

**建议修复**:
1. 在查询教学班时关联 `academic_term` 表。
2. 或批量查询 `termId -> termCode` 映射后回填。
3. 前端和导出文件使用真实 `termCode` 展示。

---

### BUG-D-004 严重：专业级报告同一课程多个教学班时只取第一个教学班

**发现位置**:
- `MajorReportServiceImpl.assembleMajorReport()`

**问题描述**:
专业级报告组装支撑课程贡献时使用：

```java
classByCourseMap.putIfAbsent(tc.getCourseId(), tc);
```

该逻辑会导致同一课程下如果存在多个 locked 教学班，只保留第一个教学班，其余平行教学班的课程级达成度不会进入专业级报告的支撑课程贡献明细。

**影响分析**:
课程级报表支持多个教学班，但专业级报告只展示一个教学班，会导致：

1. 支撑课程贡献明细不完整。
2. 专业级报告与课程级报表口径不一致。
3. 双专业、双年级或平行班样例下容易出现归档结果缺失。

**建议修复**:
1. 将 `Map<Long, TeachingClass>` 改为 `Map<Long, List<TeachingClass>>`。
2. 报告中按课程展示多个教学班贡献，或明确按学生数加权聚合为课程贡献。
3. Excel 导出同步展示所有教学班，避免只导出第一个。

---

### BUG-D-005 一般：专业级报告会因教学班状态变化隐藏已有结果快照

**发现位置**:
- `MajorReportServiceImpl.assembleMajorReport()`
- `MajorReportServiceImpl.getMajorRadar()`

**问题描述**:
专业级报告已经读取到了 `major_indicator_achievement` 中的结果，但随后仍检查当前支撑课程教学班是否全部为 `locked`。如果某个教学班在结果生成后被解锁或状态变化，报告会返回“当前专业年级尚未生成专业级计算结果”，从而隐藏已有结果快照。

**影响分析**:
模块 D 的要求是报告、雷达图、台账查询应直接读取第五周确认的结果表。如果已有归档结果被当前状态隐藏，会导致历史结果无法查看，也会让用户误以为专业级结果不存在。

**建议修复**:
1. 专业级报告优先展示 `major_indicator_achievement` 中已生成的结果快照。
2. 如果当前教学班状态已变化，可额外返回 `staleWarning` 或 `dataOutdated=true`。
3. 不建议因为当前状态变化直接隐藏已有报告结果。

---

### BUG-D-006 一般：穿透式台账二、三层下钻缺少专业年级上下文约束

**发现位置**:
- `AchievementTraceController.getCourseToObjectiveTrace()`
- `AchievementTraceController.getObjectiveToScoreTrace()`
- `AchievementTraceServiceImpl.getCourseToObjectiveTrace()`
- `AchievementTraceServiceImpl.getObjectiveToScoreTrace()`

**问题描述**:
第一层下钻接口使用 `majorId + gradeYear + ipId` 查询专业级到课程级链路，但第二层和第三层接口只接收：

```json
{
  "classId": 1,
  "ipId": 1
}
```

或：

```json
{
  "classId": 1,
  "coId": 1
}
```

它们没有继续携带 `majorId + gradeYear + termId`，因此接口层无法强制校验该 `classId/ipId/coId` 是否来自同一个专业年级追溯链路。

**影响分析**:
在前端正常点击下钻时通常不会出错，但直接调用接口或参数被篡改时，可能查询到不属于当前专业年级链路的数据，影响穿透台账可信度。

**建议修复**:
1. 第二层、第三层请求 DTO 增加 `majorId`、`gradeYear`、`termId`。
2. 后端校验 `teaching_class.major_id`、`teaching_class.grade_year` 与请求上下文一致。
3. 校验 `ipId/coId` 确实在该专业年级的支撑矩阵和内部权重链路中。

---

### BUG-D-007 轻微：穿透式台账导出存在不可达旧代码分支

**发现位置**:
- `AchievementTraceServiceImpl.exportAchievementLedger()`

**问题描述**:
`exportAchievementLedger()` 先调用 `listLedgerRows()`，该方法在无数据时会直接抛出异常。因此后续：

```java
if (rows != null) {
    ...
}
```

永远成立，后面的旧版导出逻辑永远不会执行。

**影响分析**:
该问题不影响当前导出主流程，但会造成代码可读性差、维护成本高。后续开发人员可能误以为存在两套导出逻辑。

**建议修复**:
1. 删除不可达的旧版导出代码。
2. 保留一套清晰的 `buildMergedLedgerWorkbook()` 导出实现。
3. 为导出方法补充单元测试或集成测试，避免重复逻辑再次出现。

---

## 缺陷统计

| 级别 | 数量 | 编号 |
|------|------|------|
| 阻断 | 0 | — |
| 严重 | 3 | BUG-D-001, BUG-D-002, BUG-D-004 |
| 一般 | 3 | BUG-D-003, BUG-D-005, BUG-D-006 |
| 轻微 | 1 | BUG-D-007 |
| **合计** | **7** | — |

---

## 已确认可用的功能

以下功能经过代码级测试和编译验证，确认具备基础实现：

| # | 功能 | 验证结论 |
|---|------|----------|
| 1 | 后端模块 D 相关代码编译 | ✅ 通过，`mvn -q -DskipTests compile` 成功 |
| 2 | 课程级报表查询接口 | ✅ 已实现，路径 `/api/teacher/report/data` |
| 3 | 课程级报表 Excel 导出 | ✅ 已实现，路径 `/api/teacher/report/export/excel` |
| 4 | 课程级报表 PDF 导出 | ✅ 已实现，路径 `/api/teacher/report/export/pdf` |
| 5 | 专业级报告查询 | ✅ 已实现，路径 `/api/report/majorReport` |
| 6 | 专业级报告 Excel 导出 | ✅ 已实现，路径 `/api/report/majorReport/export` |
| 7 | 专业级雷达图数据 | ✅ 已实现，路径 `/api/report/majorRadar` |
| 8 | 穿透式台账三层下钻接口 | ✅ 已实现 |
| 9 | 穿透式台账 Excel 导出 | ✅ 已实现 |
| 10 | PDF 中文字体资源 | ✅ 已存在 `NotoSansSC-Regular.ttf` |

---

## 测试结论

模块 D 后端编译通过，课程级报表、专业级报告、雷达图和穿透式台账的主体接口均已实现。

本次测试发现的主要问题集中在：

1. **数据库脚本与后端实体不一致**：`teaching_class.major_id` 是双专业双年级隔离的关键字段，脚本未同步属于严重问题。
2. **课程级报表教师权限过宽**：教师可通过构造参数查看其他教师课程报表。
3. **专业级报告平行教学班口径不完整**：同一课程多个教学班时只取第一个。
4. **穿透式台账二、三层接口缺少专业年级上下文校验**：直接调用接口时链路可信度不足。

建议优先修复顺序：

1. BUG-D-001：同步数据库脚本和样例数据。
2. BUG-D-002：补教师课程报表权限校验。
3. BUG-D-004：修正专业级报告平行教学班贡献口径。
4. BUG-D-006：补穿透式台账二、三层上下文校验。

---

## 附件

- 测试流程文档：`docs/测试文档及缺陷清单/模块D测试流程文档.md`
- 穿透式台账接口测试：`graduation-attainment-platform/docs/达成度逐层追溯接口测试.md`
- 课程级评价报表接口测试：`graduation-attainment-platform/docs/course-report-api-test.md`
