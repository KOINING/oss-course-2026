# 模块 C 测试缺陷清单

> **测试人员**: 毛小斌 (mxb)  
> **测试日期**: 2026-06-18  
> **测试分支**: `member/mxb`  
> **测试范围**: 成绩模板生成导出、原始成绩导入/预览/校验/保存、课程级达成度计算、课程级结果锁定/解锁、专业级达成度汇总

---

## 缺陷分级说明

| 级别 | 含义 |
|------|------|
| **🔴 严重** | 阻断主链路，导致数据丢失或计算结果错误 |
| **🟡 中等** | 功能可用但存在合规风险或口径偏差 |
| **🟢 低** | 不影响核心功能，但影响使用体验或可维护性 |
| **🔵 建议** | 非缺陷，但建议优化 |

---

## 缺陷列表

---

### D1 🔴 严重：专业级解锁时误删全部专业级结果

**发现位置**: `AssessmentQueryServiceImpl.approveUnlock()`（第 300-303 行）

**问题描述**:
教务管理员对某个教学班执行解锁操作时，代码会删除该专业+年级下**所有**的 `major_indicator_achievement` 记录，而不仅仅是与被解锁教学班相关的记录。

```java
// 当前代码：删除全专业+年级的结果
miaMapper.delete(new LambdaQueryWrapper<MajorIndicatorAchievement>()
        .eq(MajorIndicatorAchievement::getMajorId, teachingClass.getMajorId())
        .eq(MajorIndicatorAchievement::getGradeYear, teachingClass.getGradeYear()));
```

**影响分析**:
假设专业"计算机科学与技术"2024 级有 3 门支撑课程（数据结构、操作系统、软件工程），均已计算并锁定，专业级汇总已完成。如果数据结构课程主讲教师申请解锁数据结构教学班，教务审批通过后，操作系统和软件工程的专业级贡献也会被一并删除，导致专业级结果丢失。

**建议修复**:
- 短中期：解锁后仅标记该教学班的专业级结果为"待重新计算"（增加 `stale` 状态字段），而非物理删除
- 或：仅当所有支撑课程都没有有效结果时才允许删除，否则只标记失效

**测试场景**:
1. 建立 2 个以上教学班均已完成课程级计算并锁定
2. 执行专业级汇总
3. 对其中 1 个教学班申请并执行解锁
4. 验证：其他教学班的专业级贡献是否仍然保留

---

### D2 🟡 中等：专业级计算缺少宏观权重 W 的归一化校验

**发现位置**: `ScoreCalcServiceImpl.calcMajorAchievement()`（第 797-916 行）

**问题描述**:
需求规格说明书明确要求专业级计算前必须验证同一指标点下各支撑课程的宏观权重 W 之和为 1.0：

> $$\sum_c W_{ck} = 1.0$$

但 `calcMajorAchievement` 方法中**没有执行此校验**，仅校验了"所有教学班已锁定"这一前置条件。

**对比**: 课程级计算中有 `validateInternalWeightSums()` 校验内部权重 w 的和是否为 1.0（第 1125-1146 行），专业级反而缺失了对应的宏观权重 W 校验。

**影响分析**:
如果专业负责人错误配置了宏观权重（例如某指标点下两门课各设 0.6），系统会静默计算出错误的 Gk 值，且无法在计算时发现。

**建议修复**:
在 `calcMajorAchievement` 中加入宏观权重归一化校验，类似：
```java
Map<Long, Float> macroWeightSums = new LinkedHashMap<>();
for (CourseIndicatorSupport support : courseIndicatorSupports) {
    macroWeightSums.merge(support.getIpId(), support.getTotalWeight(), Float::sum);
}
// 检查每个 ipId 的 sum 是否在 [0.999, 1.001] 范围内
```

**测试场景**:
1. 配置某指标点的宏观权重 W 之和不为 1.0
2. 执行专业级计算
3. 验证：系统应拒绝计算并给出明确提示

---

### D3 🟡 中等：CourseIndicatorSupport 查询缺少年级过滤

**发现位置**: `ScoreCalcServiceImpl.calcMajorAchievement()`（第 838-840 行）

**问题描述**:
查询宏观支撑权重 `CourseIndicatorSupport` 时，仅按 `courseId` 过滤，未按 `gradeYear` 过滤：

```java
List<CourseIndicatorSupport> courseIndicatorSupports = cisMapper.selectList(
        new LambdaQueryWrapper<CourseIndicatorSupport>()
                .in(CourseIndicatorSupport::getCourseId, courseIds));
```

**影响分析**:
如果同一课程在不同年级有**不同的宏观支撑权重 W**（这在认证中是常见场景——不同年级的培养方案版本可能不同），当前代码会读取到跨年级的混合权重数据，导致 Gk 计算错误。

**建议修复**:
增加 `gradeYear` 过滤条件：
```java
.eq(CourseIndicatorSupport::getGradeYear, request.getGradeYear())
```
同时确认 `course_indicator_support` 表结构中存在 `grade_year` 字段；若不存在则需要加字段。

**测试场景**:
1. 同一课程在 2023 级和 2024 级配置不同的 W 权重
2. 对 2024 级执行专业级计算
3. 验证：是否只使用了 2024 级的权重数据

---

### D4 🟢 低：成绩模板预览不必要地强制校验内部权重

**发现位置**: `ScoreCalcServiceImpl.previewTemplate()`（第 143 行）

**问题描述**:
`previewTemplate` 方法中调用了 `ensureInternalWeightsConfigured()`，该调用会抛出异常阻止模板生成。但**成绩模板的用途是让教师填写原始成绩**，此时还不需要内部权重 w——w 只有在课程级计算时才需要。

```java
// previewTemplate 中
ensureInternalWeightsConfigured(objectives, teachingClass); // 不必要
```

**对比**: 如果用户只是想把成绩模板下载下来发给其他老师填写，而此时内部权重尚未配置完成，就会被阻断。

**影响分析**:
用户体验不佳：教师想先下载模板准备成绩数据，但被要求必须先完成权重配置。正确的流程应该允许"先下载模板→配置权重→导入成绩→计算"。

**建议修复**:
将 `ensureInternalWeightsConfigured` 从 `previewTemplate` 和 `downloadTemplate` 中移除，仅在 `calcCourseAchievement` 和 `importScorePreview` 时校验。

**测试场景**:
1. 配置课程目标、考核点，但**不配置内部权重 w**
2. 尝试预览/下载成绩模板
3. 验证：是否被不合理地阻断

---

### D5 🟢 低：课程级计算缺失成绩时错误提示不够具体

**发现位置**: `ScoreCalcServiceImpl.ensureAllScoresCompleted()`（第 1148-1167 行）

**问题描述**:
当存在缺失成绩时，系统抛出统一错误：

> "当前教学班仍有未录入的考核点成绩，必须补齐全部学生成绩后才能执行课程级计算"

但**没有指出具体是哪个学生、哪个考核点缺失**。对教师来说，需要逐行逐列核对才能定位问题。

**建议修复**:
错误信息中附带缺失明细，例如前 5 个缺失项的"学号 + 考核点名称"：
```java
List<String> missingDetails = new ArrayList<>();
// 收集缺失明细
throw new BusinessException(400, 
    "当前教学班仍有 " + missingCount + " 个考核点成绩未录入，" +
    "例如：" + String.join("、", missingDetails.subList(0, Math.min(5, missingDetails.size()))));
```

**测试场景**:
1. 导入部分学生成绩（故意缺失几个考核点）
2. 执行课程级计算
3. 验证：错误信息是否包含具体的缺失定位

---

### D6 🟢 低：达成度计算使用 float 可能导致精度问题

**发现位置**: `ScoreCalcServiceImpl` 全局

**问题描述**:
所有达成度计算均使用 Java `float`（32位 IEEE 754），有效精度约 6-7 位有效数字。在以下场景可能出现精度问题：
- 教学班人数较多（如 200 人），累加 200 个 float 值
- 多层权重累乘（w × W 嵌套计算）

虽然当前样例数据规模不大，但工程认证场景对精度有一定要求（通常保留 4 位小数）。

**建议修复**:
将关键计算路径中的 `float` 改为 `double`（64位，约 15 位有效数字），特别是在累加和乘法链路上：
- `Cij` 计算：`totalActual / totalFull`
- `C̄j` 计算：`sum / count`
- `Ek` 计算：累加 `C̄j × w`
- `Gk` 计算：累加 `Ek × W`

---

### D7 🔵 建议：CSV 成绩文件编码假设为 UTF-8

**发现位置**: `ScoreCalcServiceImpl.readImportTable()`（第 1180 行）

**问题描述**:
CSV 文件解析固定使用 UTF-8：
```java
return parseCsvRows(new String(fileBytes, StandardCharsets.UTF_8));
```

但中文 Windows 环境下 Excel 导出的 CSV 默认使用 GBK 编码，可能导致中文姓名乱码或解析失败。

**建议修复**:
增加编码探测或自动回退机制：先尝试 UTF-8，失败则尝试 GBK。

---

### D8 🔵 建议：成绩保存缺少完整的考核点归属校验

**发现位置**: `ScoreCalcServiceImpl.saveScores()`（第 415-465 行）

**问题描述**:
`saveScores` 验证了学生归属教学班、考核点存在、分数范围，但**未验证考核点是否属于当前课程的课程目标**。如果前端传入属于其他课程的考核点 ID，系统不会拒绝。

虽然正常前端操作不会出现此问题，但缺乏后端防御性校验。

**建议修复**:
增加考核点→课程目标→课程的追溯校验。

---

### D9 🔵 建议：成绩编辑无操作日志

**发现位置**: `ScoreCalcServiceImpl.saveScores()`（第 415-465 行）

**问题描述**:
教师可以在线编辑成绩并保存（`saveScores`），系统直接 upsert 覆盖旧成绩，不保留任何历史记录。如果教师误操作覆盖了正确成绩，无法追溯恢复。

**建议修复**:
考虑增加成绩变更审计日志表，记录每次成绩修改的`操作人、时间、旧值、新值`。

---

## 缺陷统计

| 级别 | 数量 | 编号 |
|------|------|------|
| 🔴 严重 | 1 | D1 |
| 🟡 中等 | 2 | D2, D3 |
| 🟢 低 | 3 | D4, D5, D6 |
| 🔵 建议 | 3 | D7, D8, D9 |
| **合计** | **9** | |

---

## 已确认可用的功能（无缺陷）

以下功能经过代码审查和公式验证，确认实现正确：

| # | 功能 | 验证结论 |
|---|------|----------|
| 1 | 动态成绩模板生成 | ✅ 正确生成动态列（学号+姓名+考核点），满分行和课程目标行正确 |
| 2 | 成绩模板 Excel 导出 | ✅ 格式正确，含表头/满分/课程目标三行元数据 |
| 3 | 成绩导入预校验（学号、姓名、分数范围、重复学号） | ✅ 校验逻辑完整 |
| 4 | 成绩保存（upsert 逻辑） | ✅ 正确实现新增/更新 |
| 5 | 学生个人课程目标达成度 Cij 计算 | ✅ 公式 Cij = Σ实际得分/Σ满分 正确 |
| 6 | 教学班课程目标平均达成度 C̄j 计算 | ✅ 公式 C̄j = ΣCij/n 正确 |
| 7 | 课程级指标点达成度 Ek 计算 | ✅ 公式 Ek = Σ(C̄j × w_jk) 正确 |
| 8 | 内部权重 w 归一化校验（和为 1.0） | ✅ 校验容差 0.001 |
| 9 | 课程级计算前置成绩完整性检查 | ✅ 确保所有学生×考核点成绩齐全 |
| 10 | 锁定状态防护（locked 后禁止导入/保存/重复计算） | ✅ 各入口均有判断 |
| 11 | 教师申请解锁（需填写原因，不可重复申请） | ✅ 逻辑正确 |
| 12 | 教务审批解锁（需待处理申请 + 确认对话框） | ✅ 流程正确 |
| 13 | 专业级达成度 Gk 计算 | ✅ 公式 Gk = Σ(E_ck × W_ck) 正确 |
| 14 | 支撑课程完成状态校验（全部 locked 才可专业级计算） | ✅ 逻辑正确 |
| 15 | 专业级结果查询与展示 | ✅ 正确读取 `major_indicator_achievement` 表 |
| 16 | 跨角色权限控制（教师/专业负责人/教务管理员） | ✅ 各接口角色判断正确 |
| 17 | 专业+年级数据隔离 | ✅ 各查询均携带 majorId+gradeYear |

---

> **附录**: 手工样例复核过程见《模块C测试流程文档》第 6 节。
