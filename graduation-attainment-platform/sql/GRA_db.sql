-- ==============================
-- 数据库创建
-- ==============================
DROP DATABASE IF EXISTS GraduationDB;
CREATE DATABASE GraduationDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
USE GraduationDB;

-- ==============================
-- 1. 系统用户权限表 (RBAC)
-- ==============================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(100) NOT NULL UNIQUE,
    perm_name VARCHAR(100) NOT NULL,
    module_name VARCHAR(50),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role(user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission(role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE RESTRICT,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ==============================
-- 2. 基础组织与时间实体
-- ==============================
CREATE TABLE IF NOT EXISTS College (
    college_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    college_code VARCHAR(20) NOT NULL UNIQUE,
    college_name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Major (
    major_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_code VARCHAR(20) NOT NULL UNIQUE,
    major_name VARCHAR(100) NOT NULL,
    college_id BIGINT NOT NULL,
    FOREIGN KEY (college_id) REFERENCES College(college_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS AcademicTerm (
    term_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    term_code VARCHAR(20) NOT NULL UNIQUE,
    academic_year INT NOT NULL,
    semester INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================
-- 3. 毕业要求体系
-- ==============================
CREATE TABLE IF NOT EXISTS GraduationRequirement (
    gr_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gr_code VARCHAR(10) NOT NULL,
    gr_description TEXT NOT NULL,
    major_id BIGINT NOT NULL,
    UNIQUE KEY uk_major_gr_code(major_id, gr_code),
    FOREIGN KEY (major_id) REFERENCES Major(major_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS IndicatorPoint (
    ip_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip_code VARCHAR(10) NOT NULL,
    ip_description TEXT NOT NULL,
    gr_id BIGINT NOT NULL,
    UNIQUE KEY uk_gr_ip_code(gr_id, ip_code),
    FOREIGN KEY (gr_id) REFERENCES GraduationRequirement(gr_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================
-- 4. 课程与教学
-- ==============================
CREATE TABLE IF NOT EXISTS Course (
    course_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credit FLOAT NOT NULL,
    major_id BIGINT NOT NULL,
    FOREIGN KEY (major_id) REFERENCES Major(major_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS TeachingClass (
    class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_name VARCHAR(50) NOT NULL,
    course_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    UNIQUE KEY uk_course_term_class(course_id, term_id, class_name),
    FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE RESTRICT,
    FOREIGN KEY (term_id) REFERENCES AcademicTerm(term_id) ON DELETE RESTRICT,
    FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Student (
    student_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_number VARCHAR(20) NOT NULL UNIQUE,
    student_name VARCHAR(50) NOT NULL,
    major_id BIGINT NOT NULL,
    enrollment_year INT NOT NULL,
    FOREIGN KEY (major_id) REFERENCES Major(major_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS StudentClass (
    sc_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    UNIQUE KEY uk_student_class(student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE RESTRICT,
    FOREIGN KEY (class_id) REFERENCES TeachingClass(class_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================
-- 5. 支撑关系
-- ==============================
CREATE TABLE IF NOT EXISTS CourseIndicatorSupport (
    cis_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    total_weight FLOAT NOT NULL,
    UNIQUE KEY uk_course_ip(course_id, ip_id),
    FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES IndicatorPoint(ip_id) ON DELETE RESTRICT,
    CHECK (total_weight >= 0 AND total_weight <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS CourseObjective (
    co_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    co_description TEXT NOT NULL,
    course_id BIGINT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ObjectiveIndicatorContribution (
    oic_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    co_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    internal_weight FLOAT NOT NULL,
    UNIQUE KEY uk_co_ip(co_id, ip_id),
    FOREIGN KEY (co_id) REFERENCES CourseObjective(co_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES IndicatorPoint(ip_id) ON DELETE RESTRICT,
    CHECK (internal_weight >= 0 AND internal_weight <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================
-- 6. 考核与成绩
-- ==============================
CREATE TABLE IF NOT EXISTS AssessmentPoint (
    ap_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ap_name VARCHAR(100) NOT NULL,
    full_score FLOAT NOT NULL,
    co_id BIGINT NOT NULL,
    FOREIGN KEY (co_id) REFERENCES CourseObjective(co_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS StudentAssessmentScore (
    sas_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    ap_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    actual_score FLOAT NOT NULL,
    UNIQUE KEY uk_student_ap_class(student_id, ap_id, class_id),
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE RESTRICT,
    FOREIGN KEY (ap_id) REFERENCES AssessmentPoint(ap_id) ON DELETE RESTRICT,
    FOREIGN KEY (class_id) REFERENCES TeachingClass(class_id) ON DELETE RESTRICT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================
-- 7. 计算结果
-- ==============================
CREATE TABLE IF NOT EXISTS CourseObjectiveAchievement (
    coa_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    co_id BIGINT NOT NULL,
    average_achievement FLOAT NOT NULL,
    UNIQUE KEY uk_class_co(class_id, co_id),
    FOREIGN KEY (class_id) REFERENCES TeachingClass(class_id) ON DELETE RESTRICT,
    FOREIGN KEY (co_id) REFERENCES CourseObjective(co_id) ON DELETE RESTRICT,
    CHECK (average_achievement >= 0 AND average_achievement <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS CourseIndicatorAchievement (
    cia_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    achievement FLOAT NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_class_ip(class_id, ip_id),
    FOREIGN KEY (class_id) REFERENCES TeachingClass(class_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES IndicatorPoint(ip_id) ON DELETE RESTRICT,
    CHECK (achievement >= 0 AND achievement <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS MajorIndicatorAchievement (
    mia_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    ip_id BIGINT NOT NULL,
    final_achievement FLOAT NOT NULL,
    UNIQUE KEY uk_major_term_ip(major_id, term_id, ip_id),
    FOREIGN KEY (major_id) REFERENCES Major(major_id) ON DELETE RESTRICT,
    FOREIGN KEY (term_id) REFERENCES AcademicTerm(term_id) ON DELETE RESTRICT,
    FOREIGN KEY (ip_id) REFERENCES IndicatorPoint(ip_id) ON DELETE RESTRICT,
    CHECK (final_achievement >= 0 AND final_achievement <= 1),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- ================================================================
--                          测试数据
--  场景：某理工大学计算机相关专业 2024-2025 学年 OBE 达成度评价
--  共 22 张表，按依赖顺序插入
-- ================================================================


-- ==============================
-- A. 学院（2 条）
-- ==============================
INSERT INTO College (college_code, college_name) VALUES
('CS', '计算机科学与技术学院'),
('EE', '电子信息工程学院');

-- ==============================
-- B. 专业（3 条）
-- ==============================
INSERT INTO Major (major_code, major_name, college_id) VALUES
('080901', '计算机科学与技术', 1),
('080902', '软件工程', 1),
('080701', '电子信息工程', 2);

-- ==============================
-- C. 学期（3 条）
-- ==============================
INSERT INTO AcademicTerm (term_code, academic_year, semester, start_date, end_date) VALUES
('2024-2025-1', 2024, 1, '2024-09-01', '2025-01-18'),
('2024-2025-2', 2024, 2, '2025-02-24', '2025-07-05'),
('2025-2026-1', 2025, 1, '2025-09-01', '2026-01-17');

-- ==============================
-- D. 系统用户（6 条）
-- ==============================
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin',          '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '赵管理员', 1),
('teacher_zhang',  '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '张教授',   1),
('teacher_li',     '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '李副教授', 1),
('teacher_wang',   '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '王讲师',   1),
('director_chen',  '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '陈主任',   1),
('academic_wu',    '$2a$10$N.zmdr9k7uOCQb7nVB1cReAtViFqHtBeGqMbGtX4EGrLp/JAUa5BW', '吴老师',   1);

-- ==============================
-- E. 角色（4 条）
-- ==============================
INSERT INTO sys_role (role_code, role_name, remark) VALUES
('admin',             '系统管理员',   '系统全局配置、用户账号管理'),
('academic_affairs',  '教务管理员',   '培养方案导入、班级学生管理、报表导出'),
('program_director',  '专业负责人',   '毕业要求维护、支撑矩阵配置、专业级计算'),
('instructor',        '课程主讲教师', '课程大纲编写、考核点设定、成绩录入、课程级计算');

-- ==============================
-- F. 用户角色分配
-- ==============================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),                   -- admin → 系统管理员
(2, 4),                   -- teacher_zhang → 主讲教师
(3, 4),                   -- teacher_li    → 主讲教师
(4, 4),                   -- teacher_wang  → 主讲教师
(5, 3),                   -- director_chen → 专业负责人
(6, 2);                   -- academic_wu   → 教务管理员

-- ==============================
-- G. 权限（15 条）
-- ==============================
INSERT INTO sys_permission (perm_code, perm_name, module_name) VALUES
('college:manage',    '学院管理',      'system'),
('major:manage',      '专业管理',      'system'),
('user:manage',       '用户管理',      'system'),
('role:assign',       '角色分配',      'system'),
('dict:manage',       '字典管理',      'system'),
('requirement:write', '毕业要求编辑',   'macro'),
('matrix:write',      '支撑矩阵编辑',   'macro'),
('course:import',     '课程导入',      'macro'),
('class:import',      '班级学生导入',   'macro'),
('objective:write',   '课程目标编辑',   'syllabus'),
('weight:write',      '内部权重编辑',   'syllabus'),
('point:write',       '考核点编辑',     'syllabus'),
('score:import',      '成绩导入录入',   'assessment'),
('calc:trigger',      '达成度计算触发', 'assessment'),
('report:export',     '报表导出',      'report');

-- ==============================
-- H. 角色权限分配
-- ==============================
-- admin 拥有全部权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15);
-- academic_affairs: 课程/班级导入 + 报表
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (2,8),(2,9),(2,15);
-- program_director: 毕业要求/矩阵 + 专业级计算 + 报表
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (3,6),(3,7),(3,14),(3,15);
-- instructor: 大纲 + 考核点 + 成绩 + 课程级计算
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (4,10),(4,11),(4,12),(4,13),(4,14);

-- ==============================
-- I. 毕业要求（计算机科学与技术，6 条 一级 GR）
-- ==============================
INSERT INTO GraduationRequirement (gr_code, gr_description, major_id) VALUES
('1', '工程知识：能够将数学、自然科学、工程基础和专业知识用于解决复杂工程问题', 1),
('2', '问题分析：能够应用数学、自然科学和工程科学的基本原理，识别、表达并通过文献研究分析复杂工程问题', 1),
('3', '设计/开发解决方案：能够设计针对复杂工程问题的解决方案，并能够在设计环节中体现创新意识', 1),
('4', '研究：能够基于科学原理并采用科学方法对复杂工程问题进行研究', 1),
('5', '使用现代工具：能够针对复杂工程问题，开发、选择与使用恰当的技术、资源、现代工程工具', 1),
('6', '工程与社会：能够基于工程相关背景知识进行合理分析，评价专业工程实践和复杂工程问题解决方案对社会的影响', 1);

-- ==============================
-- J. 二级指标点（12 条，每条 GR 下 2 个）
-- ==============================
INSERT INTO IndicatorPoint (ip_code, ip_description, gr_id) VALUES
('1.1', '能将数学和自然科学的基本概念运用于计算机工程问题的建模与求解', 1),
('1.2', '能运用工程基础知识解释计算机系统的设计原理与工作机制', 1),
('2.1', '能识别和判断计算机复杂工程问题的关键环节与技术瓶颈', 2),
('2.2', '能通过查阅文献对计算机复杂工程问题进行深入分析与分解', 2),
('3.1', '能设计满足特定需求的算法、模块或软件系统架构方案', 3),
('3.2', '能够在系统设计中综合考虑安全性、经济性、环境适应性等非技术因素', 3),
('4.1', '能针对计算机复杂工程问题设计有效的实验方案并正确采集数据', 4),
('4.2', '能运用统计学方法对实验数据进行科学分析与解释，得出有效结论', 4),
('5.1', '能熟练使用主流开发工具、调试工具和性能分析工具完成软件开发任务', 5),
('5.2', '能根据具体问题选择并运用适当的仿真软件或云计算平台进行模拟分析', 5),
('6.1', '理解计算机技术发展对社会、法律及伦理的影响，具有社会责任感', 6),
('6.2', '能在工程实践中考虑信息安全、知识产权保护等社会约束因素', 6);

-- ==============================
-- K. 课程（5 门）
-- ==============================
INSERT INTO Course (course_code, course_name, credit, major_id) VALUES
('CS201', '数据结构',         4.0, 1),
('CS301', '操作系统',         3.0, 1),
('CS302', '计算机网络',       3.0, 1),
('SW201', '软件工程',         3.0, 2),
('EE301', '数字电路与逻辑设计', 3.0, 3);

-- ==============================
-- L. 教学班级（3 个）
-- ==============================
INSERT INTO TeachingClass (class_name, course_id, term_id, teacher_id) VALUES
('数据结构2024-2025-1班', 1, 1, 2),
('操作系统2024-2025-1班', 2, 1, 3),
('计算机网络2024-2025-1班', 3, 1, 4);

-- ==============================
-- M. 学生（10 名，计算机科学与技术 2022 级）
-- ==============================
INSERT INTO Student (student_number, student_name, major_id, enrollment_year) VALUES
('20220101001', '周一帆', 1, 2022),
('20220101002', '陈思远', 1, 2022),
('20220101003', '林晓彤', 1, 2022),
('20220101004', '王浩然', 1, 2022),
('20220101005', '赵雨涵', 1, 2022),
('20220101006', '刘子轩', 1, 2022),
('20220101007', '黄诗琪', 1, 2022),
('20220101008', '杨俊杰', 1, 2022),
('20220101009', '吴佳怡', 1, 2022),
('20220101010', '郑明辉', 1, 2022);

-- ==============================
-- N. 学生选班（前8名学生选修数据结构班，后8名选修操作系统班，前6名选修计算机网络班）
-- ==============================
INSERT INTO StudentClass (student_id, class_id) VALUES
(1,1),(2,1),(3,1),(4,1),(5,1),(6,1),(7,1),(8,1),
(3,2),(4,2),(5,2),(6,2),(7,2),(8,2),(9,2),(10,2),
(1,3),(2,3),(3,3),(4,3),(5,3),(6,3);

-- ==============================
-- O. 课程目标（每门课 3~4 个）
-- ==============================
-- 数据结构 (course_id=1) : co_id 1-4
INSERT INTO CourseObjective (co_description, course_id) VALUES
('掌握线性表、栈、队列、串等基本数据结构的逻辑结构与物理实现', 1),
('掌握树、图等复杂数据结构的定义、存储方式及遍历算法', 1),
('能运用查找和排序算法解决实际应用问题，并分析算法的时间空间复杂度', 1),
('能针对具体问题选择恰当的数据结构并编写C++/Java高效实现代码', 1);

-- 操作系统 (course_id=2) : co_id 5-7
INSERT INTO CourseObjective (co_description, course_id) VALUES
('理解进程、线程的概念及调度算法，掌握并发编程的基本方法', 2),
('理解内存管理机制，包括分页、分段、虚拟内存及页面置换算法', 2),
('理解文件系统和I/O子系统的设计原理及磁盘调度策略', 2);

-- 计算机网络 (course_id=3) : co_id 8-10
INSERT INTO CourseObjective (co_description, course_id) VALUES
('掌握TCP/IP协议栈各层功能及常见协议（HTTP/DNS/TCP/IP）', 3),
('理解路由算法、拥塞控制机制，能进行网络拓扑设计与性能分析', 3),
('掌握Socket编程并能搭建简单的客户端/服务器网络应用', 3);

-- ==============================
-- P. 宏观支撑矩阵（课程→指标点，每门课2~3个支撑指标点）
-- ==============================
INSERT INTO CourseIndicatorSupport (course_id, ip_id, total_weight) VALUES
-- 数据结构支撑：1.1(算法基础), 3.1(设计方案), 5.1(编程工具)
(1, 1, 0.40),
(1, 5, 0.30),
(1, 9, 0.30),
-- 操作系统支撑：1.2(工程原理), 2.1(识别瓶颈), 3.2(综合设计)
(2, 2, 0.40),
(2, 3, 0.25),
(2, 6, 0.35),
-- 计算机网络支撑：1.2(工程原理), 2.2(文献分析), 5.2(仿真平台)
(3, 2, 0.30),
(3, 4, 0.30),
(3, 10, 0.40);

-- ==============================
-- Q. 内部贡献权重（课程目标→指标点）
-- ==============================
INSERT INTO ObjectiveIndicatorContribution (co_id, ip_id, internal_weight) VALUES
-- 数据结构 CO1→1.1  CO2→3.1  CO3→5.1  CO4→3.1+5.1
(1, 1, 0.60),
(2, 5, 0.55),
(3, 9, 0.70),
(4, 5, 0.45),
(4, 9, 0.30),
-- 操作系统 CO5→1.2  CO6→2.1  CO7→3.2
(5, 2, 0.50),
(6, 3, 0.65),
(7, 6, 0.55),
-- 计算机网络 CO8→1.2  CO9→2.2  CO10→5.2
(8, 2, 0.50),
(9, 4, 0.60),
(10, 10, 0.80);

-- ==============================
-- R. 考核点（每课目标 2~3 个考核点，共约 28 个）
-- ==============================
-- 数据结构考核点
INSERT INTO AssessmentPoint (ap_name, full_score, co_id) VALUES
('期末卷-链表操作题',   15, 1),
('期末卷-栈队列应用题', 10, 1),
('实验-二叉树遍历实现', 20, 2),
('期末卷-图算法题',     15, 2),
('期末卷-排序算法分析', 10, 3),
('实验-查找算法对比',   15, 3),
('课程设计-综合编程',   20, 4);

-- 操作系统考核点
INSERT INTO AssessmentPoint (ap_name, full_score, co_id) VALUES
('期末卷-进程调度题',   15, 5),
('实验-多线程编程',     20, 5),
('期末卷-内存管理题',   15, 6),
('实验-页面置换模拟',   15, 6),
('期末卷-文件系统题',   10, 7),
('实验-磁盘调度模拟',   10, 7);

-- 计算机网络考核点
INSERT INTO AssessmentPoint (ap_name, full_score, co_id) VALUES
('期末卷-TCP/IP协议题',        15, 8),
('实验-Wireshark抓包分析',     15, 8),
('期末卷-路由算法题',           10, 9),
('实验-网络拓扑设计',           20, 9),
('期末卷-Socket编程题',        10, 10),
('实验-简易聊天室开发',         15, 10);

-- ==============================
-- S. 学生成绩（每个考核点×每个学生，共约 240 条）
-- 模拟真实成绩分布：优秀(85%+)、良好(70-84%)、中等(60-69%)、不及格(<60%)
-- ==============================
-- 数据结构班 (class_id=1)：学生 1-8，考核点 1-7
INSERT INTO StudentAssessmentScore (student_id, ap_id, class_id, actual_score) VALUES
-- ap1 链表操作题 (满分15)
(1,1,1,14),(2,1,1,12),(3,1,1,13),(4,1,1,10),(5,1,1,15),(6,1,1,9),(7,1,1,11),(8,1,1,13),
-- ap2 栈队列应用题 (满分10)
(1,2,1,9),(2,2,1,8),(3,2,1,10),(4,2,1,7),(5,2,1,9),(6,2,1,6),(7,2,1,8),(8,2,1,7),
-- ap3 二叉树遍历实验 (满分20)
(1,3,1,19),(2,3,1,16),(3,3,1,18),(4,3,1,14),(5,3,1,20),(6,3,1,13),(7,3,1,15),(8,3,1,17),
-- ap4 图算法题 (满分15)
(1,4,1,13),(2,4,1,11),(3,4,1,14),(4,4,1,9),(5,4,1,13),(6,4,1,8),(7,4,1,10),(8,4,1,12),
-- ap5 排序算法分析 (满分10)
(1,5,1,9),(2,5,1,7),(3,5,1,8),(4,5,1,6),(5,5,1,10),(6,5,1,5),(7,5,1,7),(8,5,1,8),
-- ap6 查找算法对比实验 (满分15)
(1,6,1,14),(2,6,1,12),(3,6,1,13),(4,6,1,10),(5,6,1,15),(6,6,1,9),(7,6,1,11),(8,6,1,13),
-- ap7 课程设计综合编程 (满分20)
(1,7,1,18),(2,7,1,15),(3,7,1,17),(4,7,1,13),(5,7,1,19),(6,7,1,12),(7,7,1,14),(8,7,1,16);

-- 操作系统班 (class_id=2)：学生 3-10，考核点 8-13
INSERT INTO StudentAssessmentScore (student_id, ap_id, class_id, actual_score) VALUES
-- ap8 进程调度题 (满分15)
(3,8,2,13),(4,8,2,11),(5,8,2,14),(6,8,2,10),(7,8,2,12),(8,8,2,9),(9,8,2,13),(10,8,2,8),
-- ap9 多线程编程实验 (满分20)
(3,9,2,18),(4,9,2,15),(5,9,2,19),(6,9,2,14),(7,9,2,16),(8,9,2,13),(9,9,2,17),(10,9,2,12),
-- ap10 内存管理题 (满分15)
(3,10,2,12),(4,10,2,10),(5,10,2,13),(6,10,2,9),(7,10,2,11),(8,10,2,8),(9,10,2,12),(10,10,2,7),
-- ap11 页面置换模拟实验 (满分15)
(3,11,2,14),(4,11,2,11),(5,11,2,15),(6,11,2,10),(7,11,2,12),(8,11,2,9),(9,11,2,13),(10,11,2,8),
-- ap12 文件系统题 (满分10)
(3,12,2,9),(4,12,2,7),(5,12,2,8),(6,12,2,6),(7,12,2,8),(8,12,2,5),(9,12,2,9),(10,12,2,6),
-- ap13 磁盘调度模拟实验 (满分10)
(3,13,2,8),(4,13,2,7),(5,13,2,9),(6,13,2,6),(7,13,2,7),(8,13,2,5),(9,13,2,8),(10,13,2,6);

-- 计算机网络班 (class_id=3)：学生 1-6，考核点 14-19
INSERT INTO StudentAssessmentScore (student_id, ap_id, class_id, actual_score) VALUES
-- ap14 TCP/IP协议题 (满分15)
(1,14,3,14),(2,14,3,11),(3,14,3,13),(4,14,3,10),(5,14,3,15),(6,14,3,9),
-- ap15 Wireshark抓包实验 (满分15)
(1,15,3,13),(2,15,3,12),(3,15,3,14),(4,15,3,10),(5,15,3,15),(6,15,3,9),
-- ap16 路由算法题 (满分10)
(1,16,3,9),(2,16,3,7),(3,16,3,8),(4,16,3,6),(5,16,3,10),(6,16,3,5),
-- ap17 网络拓扑设计实验 (满分20)
(1,17,3,18),(2,17,3,15),(3,17,3,17),(4,17,3,13),(5,17,3,19),(6,17,3,12),
-- ap18 Socket编程题 (满分10)
(1,18,3,8),(2,18,3,7),(3,18,3,9),(4,18,3,6),(5,18,3,9),(6,18,3,5),
-- ap19 简易聊天室开发实验 (满分15)
(1,19,3,13),(2,19,3,11),(3,19,3,14),(4,19,3,9),(5,19,3,15),(6,19,3,8);

-- ==============================
-- T. 课程目标达成度（手动计算/模拟）
-- ==============================
-- 数据结构 class_id=1, co_id 1-4
INSERT INTO CourseObjectiveAchievement (class_id, co_id, average_achievement) VALUES
(1, 1, 0.82), (1, 2, 0.78), (1, 3, 0.75), (1, 4, 0.80);
-- 操作系统 class_id=2, co_id 5-7
INSERT INTO CourseObjectiveAchievement (class_id, co_id, average_achievement) VALUES
(2, 5, 0.79), (2, 6, 0.72), (2, 7, 0.74);
-- 计算机网络 class_id=3, co_id 8-10
INSERT INTO CourseObjectiveAchievement (class_id, co_id, average_achievement) VALUES
(3, 8, 0.80), (3, 9, 0.76), (3, 10, 0.78);

-- ==============================
-- U. 课程级指标点达成度
-- ==============================
INSERT INTO CourseIndicatorAchievement (class_id, ip_id, achievement, is_locked) VALUES
-- 数据结构对指标点 1.1, 3.1, 5.1
(1, 1, 0.82, TRUE),
(1, 5, 0.79, TRUE),
(1, 9, 0.80, TRUE),
-- 操作系统对指标点 1.2, 2.1, 3.2
(2, 2, 0.79, TRUE),
(2, 3, 0.72, TRUE),
(2, 6, 0.74, TRUE),
-- 计算机网络对指标点 1.2, 2.2, 5.2
(3, 2, 0.80, TRUE),
(3, 4, 0.76, TRUE),
(3, 10, 0.78, TRUE);

-- ==============================
-- V. 专业级指标点达成度（计算机科学与技术 major_id=1, term_id=1）
-- ==============================
INSERT INTO MajorIndicatorAchievement (major_id, term_id, ip_id, final_achievement) VALUES
(1, 1, 1, 0.82),
(1, 1, 2, 0.80),
(1, 1, 3, 0.72),
(1, 1, 4, 0.76),
(1, 1, 5, 0.79),
(1, 1, 6, 0.74),
(1, 1, 9, 0.80),
(1, 1, 10, 0.78);
