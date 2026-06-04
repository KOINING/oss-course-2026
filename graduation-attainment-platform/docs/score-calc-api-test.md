# 成绩计算 API 自测样例

## 前置条件

1. 确保后端服务已启动
2. 确保数据库中已有基础数据（课程、学期、教师、专业、教学班、学生、课程目标、考核点等）
3. 使用 admin 账号登录获取 token

## 测试流程

### 1. 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

### 2. 成绩模板预览

```bash
curl -X POST "http://localhost:8080/api/teacher/previewTemplate?classId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2.1 下载成绩模板 Excel

```bash
curl -X GET "http://localhost:8080/api/teacher/downloadTemplate?classId=1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o "成绩模板.xlsx"
```

**说明：**
- 下载的 Excel 文件包含以下内容：
  - 第一行：固定列头（学号、姓名）+ 动态列头（考核点名称）
  - 第二行：满分信息
  - 第三行：课程目标编码
  - 第四行起：学生数据（学号、姓名，成绩列留空供填写）
- 文件会自动下载到当前目录，文件名为 `成绩模板_1.xlsx`

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "classId": 1,
    "className": "数据结构2024-2025-1班",
    "courseName": "数据结构",
    "studentCount": 30,
    "assessmentPointCount": 5,
    "fixedHeaders": ["学号", "姓名"],
    "dynamicHeaders": [
      {
        "apId": 1,
        "apName": "期末卷-链表操作题",
        "fullScore": 20.0,
        "objectiveCode": "CO1"
      }
    ],
    "rows": [...]
  }
}
```

### 3. 成绩导入预校验

```bash
curl -X POST http://localhost:8080/api/teacher/importScorePreview \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "classId": 1,
    "jsonData": "[{\"studentNo\":\"20220101001\",\"scores\":{\"期末卷-链表操作题\":18}}]"
  }'
```

### 4. 保存成绩

```bash
curl -X POST http://localhost:8080/api/teacher/saveScores \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "classId": 1,
    "scores": [
      {"studentId": 1, "apId": 1, "actualScore": 18.0},
      {"studentId": 1, "apId": 2, "actualScore": 15.5},
      {"studentId": 2, "apId": 1, "actualScore": 16.0},
      {"studentId": 2, "apId": 2, "actualScore": 14.0}
    ]
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "成绩保存成功",
  "data": null
}
```

### 5. 课程级达成度计算

```bash
curl -X POST http://localhost:8080/api/teacher/calcCourseAchievement \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "classId": 1
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "classId": 1,
    "className": "数据结构2024-2025-1班",
    "courseName": "数据结构",
    "studentCount": 30,
    "objectiveAchievements": [
      {
        "coId": 1,
        "objectiveCode": "CO1",
        "description": "能够运用数据结构知识解决实际问题",
        "averageAchievement": 0.75
      }
    ],
    "indicatorAchievements": [
      {
        "ipId": 1,
        "ipCode": "1.1",
        "ipDescription": "能够运用数学、自然科学和工程科学的基本原理",
        "achievement": 0.72
      }
    ],
    "isLocked": false
  }
}
```

### 6. 查询课程计算状态

```bash
curl -X POST "http://localhost:8080/api/teacher/getCourseCalcStatus?majorId=1&termId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "termId": 1,
    "termCode": "2024-2025-1",
    "canCalcMajor": false,
    "blockReason": "存在未完成计算的教学班",
    "courseStatuses": [
      {
        "courseId": 1,
        "courseCode": "CS201",
        "courseName": "数据结构",
        "classId": 1,
        "className": "数据结构2024-2025-1班",
        "calcStatus": "calculating",
        "isLocked": false
      }
    ]
  }
}
```

### 7. 专业级达成度汇总

```bash
curl -X POST http://localhost:8080/api/teacher/calcMajorAchievement \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "majorId": 1,
    "termId": 1
  }'
```

**预期响应（成功时）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "termId": 1,
    "termCode": "2024-2025-1",
    "indicatorAchievements": [
      {
        "ipId": 1,
        "ipCode": "1.1",
        "ipDescription": "能够运用数学、自然科学和工程科学的基本原理",
        "finalAchievement": 0.78
      }
    ]
  }
}
```

**预期响应（前置条件未满足时）：**
```json
{
  "code": 400,
  "message": "存在未完成计算的教学班：数据结构2024-2025-1班",
  "data": null
}
```

---

## 计算公式说明

### 课程级计算

1. **学生-课程目标达成度 Cij**
   ```
   Cij = Σ(支撑目标j的考核点实际得分) / Σ(支撑目标j的考核点满分)
   ```

2. **班级课程目标平均达成度 C̄j**
   ```
   C̄j = 全班 Cij 的算术平均
   ```

3. **课程级指标点达成度 Ek**
   ```
   Ek = Σ(C̄j × wjk)
   其中 wjk 是课程目标j对指标点k的内部贡献权重
   ```

### 专业级汇总

4. **专业级指标点达成度 Gk**
   ```
   Gk = Σ(Eck × Wck)
   其中 Eck 是课程c对指标点k的课程级达成度
   其中 Wck 是课程c对指标点k的宏观支撑权重
   ```

---

## 校验规则

### 成绩保存校验
- 学生必须属于当前教学班
- 考核点必须存在
- 成绩必须在 0 到满分之间

### 课程级计算校验
- 教学班必须存在
- 必须有成绩数据
- 必须配置课程目标和考核点

### 专业级汇总校验
- 专业和学期必须存在
- 所有支撑课程的教学班必须已完成计算（状态为 locked）
- 同一指标点下 ΣW = 1.0（由第三周支撑矩阵配置保证）

---

## 接口列表汇总

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/teacher/previewTemplate` | 预览成绩模板 |
| GET | `/api/teacher/downloadTemplate` | 下载成绩模板 Excel |
| POST | `/api/teacher/importScorePreview` | 成绩导入预校验 |
| POST | `/api/teacher/saveScores` | 保存成绩 |
| POST | `/api/teacher/calcCourseAchievement` | 课程级达成度计算 |
| POST | `/api/teacher/calcMajorAchievement` | 专业级达成度汇总 |
| POST | `/api/teacher/getCourseCalcStatus` | 查询课程计算状态 |
