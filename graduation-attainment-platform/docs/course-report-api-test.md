# 课程级评价报表 API 自测样例

## 前置条件

1. 确保后端服务已启动
2. 确保数据库中已有课程级计算结果（course_objective_achievement、course_indicator_achievement 表有数据）
3. 使用教师账号登录获取 token

## 测试流程

### 1. 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher_zhang","password":"123456"}'
```

### 2. 查询课程级评价报表数据

```bash
curl -X POST http://localhost:8080/api/teacher/report/data \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "courseId": 1,
    "gradeYear": 2022
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "courseId": 1,
    "courseCode": "CS201",
    "courseName": "数据结构",
    "gradeYear": 2022,
    "credit": 3.0,
    "teachingClasses": [
      {
        "classId": 1,
        "classCode": "TC2024CS01",
        "className": "数据结构2024-2025-1班",
        "termCode": "2024-2025-1",
        "studentCount": 30,
        "calcStatus": "locked",
        "assessmentPointAverages": [
          {
            "apId": 1,
            "apName": "期末卷-链表操作题",
            "fullScore": 20.0,
            "averageScore": 15.5,
            "scoreRate": 0.775
          }
        ],
        "objectiveAchievementDetails": [
          {
            "coId": 1,
            "objectiveCode": "CO1",
            "description": "能够运用数据结构知识解决实际问题",
            "averageAchievement": 0.75
          }
        ],
        "indicatorAchievementDetails": [
          {
            "ipId": 1,
            "ipCode": "1.1",
            "ipDescription": "能够运用数学、自然科学和工程科学的基本原理",
            "achievement": 0.72
          }
        ]
      }
    ],
    "objectiveAchievements": [
      {
        "coId": 1,
        "objectiveCode": "CO1",
        "description": "能够运用数据结构知识解决实际问题",
        "classAchievements": [...],
        "averageAchievement": 0.73
      }
    ],
    "indicatorAchievements": [
      {
        "ipId": 1,
        "ipCode": "1.1",
        "ipDescription": "能够运用数学、自然科学和工程科学的基本原理",
        "classAchievements": [...],
        "averageAchievement": 0.70
      }
    ]
  }
}
```

### 3. 导出课程级评价报表 Excel

```bash
curl -X POST http://39.104.52.187/api/teacher/report/export/excel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhY2FkZW1pY193dSIsInVzZXJJZCI6NiwicmVhbE5hbWUiOiLlkLTogIHluIgiLCJyb2xlcyI6WyJhY2FkZW1pY19hZmZhaXJzIl0sInBlcm1pc3Npb25zIjpbImNvdXJzZTppbXBvcnQiLCJjbGFzczppbXBvcnQiLCJyZXBvcnQ6ZXhwb3J0Il0sImlhdCI6MTc4MTIyMzI4NSwiZXhwIjoxNzgxMzA5Njg1fQ.qQGhO4c22L-wkh_QjYctjkrzWLMZc0j-JnsVJRKNQsBAIB2rQqedESzCACcq_0ME" \
  -d '{
    "courseId": 62,
    "gradeYear": 2022
  }' \
  -o "课程级评价报表.xlsx"
```

**说明：**
- 文件会下载到当前目录
- 文件名格式：`课程级评价报表_{courseId}_{gradeYear}.xlsx`

### 4. 导出课程级评价报表 PDF

```bash
curl -X POST http://39.104.52.187/api/teacher/report/export/pdf \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhY2FkZW1pY193dSIsInVzZXJJZCI6NiwicmVhbE5hbWUiOiLlkLTogIHluIgiLCJyb2xlcyI6WyJhY2FkZW1pY19hZmZhaXJzIl0sInBlcm1pc3Npb25zIjpbImNvdXJzZTppbXBvcnQiLCJjbGFzczppbXBvcnQiLCJyZXBvcnQ6ZXhwb3J0Il0sImlhdCI6MTc4MTIyMzI4NSwiZXhwIjoxNzgxMzA5Njg1fQ.qQGhO4c22L-wkh_QjYctjkrzWLMZc0j-JnsVJRKNQsBAIB2rQqedESzCACcq_0ME" \
  -d '{
    "courseId": 62,
    "gradeYear": 2022
  }' \
  -o "课程级评价报表.pdf"
```

**说明：**
- 文件会下载到当前目录
- 文件名格式：`课程级评价报表_{courseId}_{gradeYear}.pdf`
- PDF 内容使用英文显示（中文需要额外字体支持）

### 5. 按学期查询报表

```bash
curl -X POST http://localhost:8080/api/teacher/report/data \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "courseId": 1,
    "gradeYear": 2022,
    "termId": 1
  }'
```

---

## 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/teacher/report/data` | 查询课程级评价报表数据 |
| POST | `/api/teacher/report/export/excel` | 导出课程级评价报表 Excel |
| POST | `/api/teacher/report/export/pdf` | 导出课程级评价报表 PDF |

---

## 报表内容说明

### 报表字段

1. **各教学班单项平均分**
   - 考核点名称
   - 满分
   - 平均分
   - 得分率

2. **课程目标达成度明细**
   - 课程目标编码
   - 课程目标描述
   - 班级平均达成度

3. **课程级指标点达成度**
   - 指标点编码
   - 指标点描述
   - 课程级指标点达成度

### 数据来源

- `course_objective_achievement` - 课程目标达成度
- `course_indicator_achievement` - 课程级指标点达成度
- `student_assessment_score` - 学生考核成绩（用于计算平均分）
