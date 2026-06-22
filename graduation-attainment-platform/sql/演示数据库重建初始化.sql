-- 演示数据库重建与基础数据预置脚本（合并版）
-- 生成时间：2026-06-20
-- 说明：保留表结构和演示账号；不预置教学班、名单关联、支撑矩阵、成绩、计算结果、报表结果。
-- 本脚本已合并 demo_update_major_specific_requirements_20260620.sql，毕业要求编号和指标点描述按专业区分。
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS `GraduationDB` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `GraduationDB`;
SET FOREIGN_KEY_CHECKS=0;
SET UNIQUE_CHECKS=0;

-- Table structure for `academic_term`
DROP TABLE IF EXISTS `academic_term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_term` (
  `term_id` bigint NOT NULL AUTO_INCREMENT,
  `term_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `academic_year` int NOT NULL,
  `semester` int NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=当前学期 0=历史学期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`term_id`),
  UNIQUE KEY `term_code` (`term_code`),
  CONSTRAINT `academic_term_chk_1` CHECK ((`semester` in (1,2,3)))
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `assessment_point`
DROP TABLE IF EXISTS `assessment_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assessment_point` (
  `ap_id` bigint NOT NULL AUTO_INCREMENT,
  `ap_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_score` float NOT NULL,
  `co_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ap_id`),
  KEY `idx_ap_objective` (`co_id`),
  CONSTRAINT `assessment_point_ibfk_1` FOREIGN KEY (`co_id`) REFERENCES `course_objective` (`co_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `calc_audit_log`
DROP TABLE IF EXISTS `calc_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `calc_audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `operator_id` bigint NOT NULL COMMENT '触发计算的用户ID sys_user.id',
  `action_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'course_obj_calc / course_ind_calc / major_calc / unlock',
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'teaching_class / major',
  `target_id` bigint NOT NULL COMMENT 'class_id 或 major_id',
  `term_id` bigint DEFAULT NULL COMMENT '学期ID，仅专业级计算时有值',
  `result_json` json DEFAULT NULL COMMENT '计算结果JSON快照，用于历史回溯',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_cal_target` (`target_type`,`target_id`),
  KEY `idx_cal_operator` (`operator_id`),
  KEY `idx_cal_time` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `college`
DROP TABLE IF EXISTS `college`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `college` (
  `college_id` bigint NOT NULL AUTO_INCREMENT,
  `college_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `college_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`college_id`),
  UNIQUE KEY `college_code` (`college_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `course`
DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `course_id` bigint NOT NULL AUTO_INCREMENT,
  `course_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `credit` float NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=开课中 0=停开',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`course_id`),
  UNIQUE KEY `course_code` (`course_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `course_indicator_achievement`
DROP TABLE IF EXISTS `course_indicator_achievement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_indicator_achievement` (
  `cia_id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `ip_id` bigint NOT NULL,
  `achievement` float NOT NULL,
  `is_locked` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`cia_id`),
  UNIQUE KEY `uk_class_ip` (`class_id`,`ip_id`),
  KEY `ip_id` (`ip_id`),
  KEY `idx_cia_class` (`class_id`),
  KEY `idx_cia_class_ip` (`class_id`,`ip_id`),
  CONSTRAINT `course_indicator_achievement_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `teaching_class` (`class_id`) ON DELETE RESTRICT,
  CONSTRAINT `course_indicator_achievement_ibfk_2` FOREIGN KEY (`ip_id`) REFERENCES `indicator_point` (`ip_id`) ON DELETE RESTRICT,
  CONSTRAINT `course_indicator_achievement_chk_1` CHECK (((`achievement` >= 0) and (`achievement` <= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `course_indicator_support`
DROP TABLE IF EXISTS `course_indicator_support`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_indicator_support` (
  `cis_id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `ip_id` bigint NOT NULL,
  `total_weight` float NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`cis_id`),
  UNIQUE KEY `uk_course_ip` (`course_id`,`ip_id`),
  KEY `idx_cis_course` (`course_id`),
  KEY `idx_cis_indicator` (`ip_id`),
  CONSTRAINT `course_indicator_support_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE RESTRICT,
  CONSTRAINT `course_indicator_support_ibfk_2` FOREIGN KEY (`ip_id`) REFERENCES `indicator_point` (`ip_id`) ON DELETE RESTRICT,
  CONSTRAINT `course_indicator_support_chk_1` CHECK (((`total_weight` >= 0) and (`total_weight` <= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `course_major`
DROP TABLE IF EXISTS `course_major`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_major` (
  `cm_id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `major_id` bigint NOT NULL,
  `grade_year` int NOT NULL DEFAULT '2022' COMMENT '适用年级',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cm_id`),
  UNIQUE KEY `uk_course_major_grade` (`course_id`,`major_id`,`grade_year`),
  KEY `idx_cm_course` (`course_id`),
  KEY `idx_cm_major` (`major_id`),
  KEY `idx_cm_major_grade` (`major_id`,`grade_year`),
  CONSTRAINT `course_major_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `course_major_ibfk_2` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `course_objective`
DROP TABLE IF EXISTS `course_objective`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_objective` (
  `co_id` bigint NOT NULL AUTO_INCREMENT,
  `objective_code` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `co_description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`co_id`),
  UNIQUE KEY `uk_course_obj_code` (`course_id`,`objective_code`),
  KEY `idx_obj_course` (`course_id`),
  CONSTRAINT `course_objective_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `course_objective_achievement`
DROP TABLE IF EXISTS `course_objective_achievement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_objective_achievement` (
  `coa_id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `co_id` bigint NOT NULL,
  `average_achievement` float NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`coa_id`),
  UNIQUE KEY `uk_class_co` (`class_id`,`co_id`),
  KEY `co_id` (`co_id`),
  KEY `idx_coa_class` (`class_id`),
  KEY `idx_coa_class_co` (`class_id`,`co_id`),
  CONSTRAINT `course_objective_achievement_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `teaching_class` (`class_id`) ON DELETE RESTRICT,
  CONSTRAINT `course_objective_achievement_ibfk_2` FOREIGN KEY (`co_id`) REFERENCES `course_objective` (`co_id`) ON DELETE RESTRICT,
  CONSTRAINT `course_objective_achievement_chk_1` CHECK (((`average_achievement` >= 0) and (`average_achievement` <= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `graduation_requirement`
DROP TABLE IF EXISTS `graduation_requirement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `graduation_requirement` (
  `gr_id` bigint NOT NULL AUTO_INCREMENT,
  `gr_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gr_description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `major_id` bigint NOT NULL,
  `grade_year` int NOT NULL DEFAULT '2022' COMMENT '适用年级',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`gr_id`),
  UNIQUE KEY `uk_major_grade_gr_code` (`major_id`,`grade_year`,`gr_code`),
  KEY `idx_req_major` (`major_id`),
  KEY `idx_req_major_grade` (`major_id`,`grade_year`),
  CONSTRAINT `graduation_requirement_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `indicator_point`
DROP TABLE IF EXISTS `indicator_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `indicator_point` (
  `ip_id` bigint NOT NULL AUTO_INCREMENT,
  `ip_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ip_description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `gr_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ip_id`),
  UNIQUE KEY `uk_gr_ip_code` (`gr_id`,`ip_code`),
  KEY `idx_ind_req` (`gr_id`),
  CONSTRAINT `indicator_point_ibfk_1` FOREIGN KEY (`gr_id`) REFERENCES `graduation_requirement` (`gr_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `major`
DROP TABLE IF EXISTS `major`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `major` (
  `major_id` bigint NOT NULL AUTO_INCREMENT,
  `major_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `major_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `college_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=招生中 0=停招',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`major_id`),
  UNIQUE KEY `major_code` (`major_code`),
  KEY `college_id` (`college_id`),
  CONSTRAINT `major_ibfk_1` FOREIGN KEY (`college_id`) REFERENCES `college` (`college_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `major_indicator_achievement`
DROP TABLE IF EXISTS `major_indicator_achievement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `major_indicator_achievement` (
  `mia_id` bigint NOT NULL AUTO_INCREMENT,
  `major_id` bigint NOT NULL,
  `grade_year` int NOT NULL DEFAULT '2022' COMMENT '适用年级',
  `term_id` bigint NOT NULL,
  `ip_id` bigint NOT NULL,
  `final_achievement` float NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`mia_id`),
  UNIQUE KEY `uk_major_grade_term_ip` (`major_id`,`grade_year`,`term_id`,`ip_id`),
  KEY `ip_id` (`ip_id`),
  KEY `idx_mia_major` (`major_id`),
  KEY `idx_mia_term` (`term_id`),
  KEY `idx_mia_major_grade` (`major_id`,`grade_year`),
  CONSTRAINT `major_indicator_achievement_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT,
  CONSTRAINT `major_indicator_achievement_ibfk_2` FOREIGN KEY (`term_id`) REFERENCES `academic_term` (`term_id`) ON DELETE RESTRICT,
  CONSTRAINT `major_indicator_achievement_ibfk_3` FOREIGN KEY (`ip_id`) REFERENCES `indicator_point` (`ip_id`) ON DELETE RESTRICT,
  CONSTRAINT `major_indicator_achievement_chk_1` CHECK (((`final_achievement` >= 0) and (`final_achievement` <= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `objective_indicator_contribution`
DROP TABLE IF EXISTS `objective_indicator_contribution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `objective_indicator_contribution` (
  `oic_id` bigint NOT NULL AUTO_INCREMENT,
  `co_id` bigint NOT NULL,
  `ip_id` bigint NOT NULL,
  `internal_weight` float NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`oic_id`),
  UNIQUE KEY `uk_co_ip` (`co_id`,`ip_id`),
  KEY `idx_oic_objective` (`co_id`),
  KEY `idx_oic_indicator` (`ip_id`),
  KEY `idx_oic_co_ip` (`co_id`,`ip_id`),
  CONSTRAINT `objective_indicator_contribution_ibfk_1` FOREIGN KEY (`co_id`) REFERENCES `course_objective` (`co_id`) ON DELETE RESTRICT,
  CONSTRAINT `objective_indicator_contribution_ibfk_2` FOREIGN KEY (`ip_id`) REFERENCES `indicator_point` (`ip_id`) ON DELETE RESTRICT,
  CONSTRAINT `objective_indicator_contribution_chk_1` CHECK (((`internal_weight` >= 0) and (`internal_weight` <= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `student`
DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `student_id` bigint NOT NULL AUTO_INCREMENT,
  `student_no` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `major_id` bigint NOT NULL,
  `enrollment_year` int NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=在读 2=毕业 3=休学 0=退学',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `student_no` (`student_no`),
  UNIQUE KEY `uk_student_user` (`user_id`),
  KEY `idx_student_major` (`major_id`),
  CONSTRAINT `student_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT,
  CONSTRAINT `student_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `student_assessment_score`
DROP TABLE IF EXISTS `student_assessment_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_assessment_score` (
  `sas_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `ap_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `actual_score` float NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sas_id`),
  UNIQUE KEY `uk_student_ap_class` (`student_id`,`ap_id`,`class_id`),
  KEY `idx_sas_class` (`class_id`),
  KEY `idx_sas_student` (`student_id`),
  KEY `idx_sas_ap_student` (`ap_id`,`student_id`),
  KEY `idx_sas_class_ap` (`class_id`,`ap_id`),
  CONSTRAINT `student_assessment_score_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE RESTRICT,
  CONSTRAINT `student_assessment_score_ibfk_2` FOREIGN KEY (`ap_id`) REFERENCES `assessment_point` (`ap_id`) ON DELETE RESTRICT,
  CONSTRAINT `student_assessment_score_ibfk_3` FOREIGN KEY (`class_id`) REFERENCES `teaching_class` (`class_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `student_class`
DROP TABLE IF EXISTS `student_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_class` (
  `sc_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sc_id`),
  UNIQUE KEY `uk_student_class` (`student_id`,`class_id`),
  KEY `class_id` (`class_id`),
  CONSTRAINT `student_class_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE RESTRICT,
  CONSTRAINT `student_class_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `teaching_class` (`class_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `student_objective_achievement`
DROP TABLE IF EXISTS `student_objective_achievement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_objective_achievement` (
  `soa_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `class_id` bigint NOT NULL COMMENT '教学班ID',
  `co_id` bigint NOT NULL COMMENT '课程目标ID',
  `achievement` float NOT NULL COMMENT '学生对课程目标的达成度，取值范围 0 到 1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`soa_id`),
  UNIQUE KEY `uk_soa_student_class_co` (`student_id`,`class_id`,`co_id`),
  KEY `idx_soa_student` (`student_id`),
  KEY `idx_soa_class` (`class_id`),
  KEY `idx_soa_co` (`co_id`),
  KEY `idx_soa_class_co` (`class_id`,`co_id`),
  CONSTRAINT `fk_soa_class` FOREIGN KEY (`class_id`) REFERENCES `teaching_class` (`class_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_soa_co` FOREIGN KEY (`co_id`) REFERENCES `course_objective` (`co_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_soa_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_soa_achievement` CHECK (((`achievement` >= 0) and (`achievement` <= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生课程目标达成度中间结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `sys_permission`
DROP TABLE IF EXISTS `sys_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `perm_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `perm_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `module_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `perm_code` (`perm_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `sys_role`
DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=禁用',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `sys_role_permission`
DROP TABLE IF EXISTS `sys_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_rp_role` (`role_id`),
  KEY `idx_rp_perm` (`permission_id`),
  CONSTRAINT `sys_role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `sys_role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `sys_user`
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `sys_user_role`
DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_ur_role` (`role_id`),
  CONSTRAINT `sys_user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `sys_user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `system_config`
DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `config_value` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `config_desc` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `teacher`
DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `teacher_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `major_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=在职 0=离职',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `teacher_no` (`teacher_no`),
  UNIQUE KEY `uk_teacher_user` (`user_id`),
  KEY `idx_teacher_major` (`major_id`),
  CONSTRAINT `teacher_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE SET NULL,
  CONSTRAINT `teacher_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `teaching_class`
DROP TABLE IF EXISTS `teaching_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teaching_class` (
  `class_id` bigint NOT NULL AUTO_INCREMENT,
  `class_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教学班编号，业务唯一标识',
  `class_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_id` bigint NOT NULL,
  `major_id` bigint NOT NULL,
  `term_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `grade_year` int NOT NULL DEFAULT '2022' COMMENT '适用年级',
  `calc_status` enum('unsubmitted','score_imported','calculating','locked') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unsubmitted' COMMENT '计算状态：unsubmitted=未提交成绩 / score_imported=成绩已导入 / calculating=计算中 / locked=已锁定',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `class_code` (`class_code`),
  UNIQUE KEY `uk_course_term_class` (`course_id`,`term_id`,`class_name`),
  UNIQUE KEY `uk_tc_major_grade_course` (`major_id`,`grade_year`,`course_id`),
  KEY `idx_class_course` (`course_id`),
  KEY `idx_class_term` (`term_id`),
  KEY `idx_class_teacher` (`teacher_id`),
  KEY `idx_tc_calc_status` (`calc_status`,`term_id`),
  KEY `idx_class_grade` (`grade_year`),
  KEY `idx_class_major_grade` (`major_id`,`grade_year`),
  CONSTRAINT `fk_tc_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT,
  CONSTRAINT `teaching_class_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE RESTRICT,
  CONSTRAINT `teaching_class_ibfk_2` FOREIGN KEY (`term_id`) REFERENCES `academic_term` (`term_id`) ON DELETE RESTRICT,
  CONSTRAINT `teaching_class_ibfk_3` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `temp_import_staging`
DROP TABLE IF EXISTS `temp_import_staging`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `temp_import_staging` (
  `staging_id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导入批次UUID',
  `table_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标表名',
  `row_index` int NOT NULL COMMENT 'Excel行号（从2开始）',
  `row_data` json NOT NULL COMMENT '行原始数据的 JSON',
  `status` enum('pending','validated','imported','error') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '校验失败原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`staging_id`),
  KEY `idx_staging_batch` (`batch_id`),
  KEY `idx_staging_status` (`batch_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- Table structure for `unlock_audit_log`
DROP TABLE IF EXISTS `unlock_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `unlock_audit_log` (
  `ulog_id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL COMMENT '被解锁的教学班级 teaching_class.class_id',
  `request_by` bigint NOT NULL COMMENT '申请解锁的教师 teacher.id',
  `approved_by` bigint NOT NULL COMMENT '审批解锁的教务管理员 sys_user.id',
  `reason` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '解锁原因（必填）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ulog_id`),
  KEY `idx_ulog_class` (`class_id`),
  CONSTRAINT `unlock_audit_log_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `teaching_class` (`class_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

-- ============================================================
-- 系统账号、角色与权限：复用当前数据库演示账号
-- ============================================================
INSERT INTO `sys_role` VALUES (1,'admin','系统管理员',1,'系统全局配置、用户账号管理','2026-06-14 22:31:24','2026-06-14 22:31:24'),(2,'academic_affairs','教务管理员',1,'课程、教学班、学生名单和专业级结果管理','2026-06-14 22:31:24','2026-06-14 22:31:24'),(3,'program_director','专业负责人',1,'毕业要求、支撑矩阵、专业级达成度分析','2026-06-14 22:31:24','2026-06-14 22:31:24'),(4,'instructor','课程主讲教师',1,'课程目标、考核点、成绩录入和课程级计算','2026-06-14 22:31:24','2026-06-14 22:31:24');

INSERT INTO `sys_permission` VALUES (1,'college:manage','学院管理','system',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(2,'major:manage','专业管理','system',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(3,'user:manage','用户管理','system',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(4,'role:assign','角色分配','system',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(5,'dict:manage','字典管理','system',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(6,'requirement:write','毕业要求编辑','macro',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(7,'matrix:write','支撑矩阵编辑','macro',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(8,'course:import','课程导入','macro',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(9,'class:import','班级学生导入','macro',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(10,'objective:write','课程目标编辑','syllabus',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(11,'weight:write','内部权重编辑','syllabus',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(12,'point:write','考核点编辑','syllabus',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(13,'score:import','成绩导入录入','assessment',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(14,'calc:trigger','达成度计算触发','assessment',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(15,'report:export','报表导出','report',NULL,'2026-06-14 22:31:24','2026-06-14 22:31:24');

INSERT INTO `sys_role_permission` VALUES (1,1,14,'2026-06-14 22:31:24'),(2,1,9,'2026-06-14 22:31:24'),(3,1,1,'2026-06-14 22:31:24'),(4,1,8,'2026-06-14 22:31:24'),(5,1,5,'2026-06-14 22:31:24'),(6,1,2,'2026-06-14 22:31:24'),(7,1,7,'2026-06-14 22:31:24'),(8,1,10,'2026-06-14 22:31:24'),(9,1,12,'2026-06-14 22:31:24'),(10,1,15,'2026-06-14 22:31:24'),(11,1,6,'2026-06-14 22:31:24'),(12,1,4,'2026-06-14 22:31:24'),(13,1,13,'2026-06-14 22:31:24'),(14,1,3,'2026-06-14 22:31:24'),(15,1,11,'2026-06-14 22:31:24'),(16,2,14,'2026-06-14 22:31:24'),(17,2,9,'2026-06-14 22:31:24'),(18,2,8,'2026-06-14 22:31:24'),(19,2,15,'2026-06-14 22:31:24'),(20,3,14,'2026-06-14 22:31:24'),(21,3,7,'2026-06-14 22:31:24'),(22,3,15,'2026-06-14 22:31:24'),(23,3,6,'2026-06-14 22:31:24'),(24,4,14,'2026-06-14 22:31:24'),(25,4,10,'2026-06-14 22:31:24'),(26,4,12,'2026-06-14 22:31:24'),(27,4,15,'2026-06-14 22:31:24'),(28,4,13,'2026-06-14 22:31:24'),(29,4,11,'2026-06-14 22:31:24');

INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','赵明远','admin@university.edu.cn','13800000001',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(2,'academic_wu','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','吴雅琴','academic@university.edu.cn','13800000002',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(3,'director_chen','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','陈志远','director@university.edu.cn','13800000003',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(4,'teacher_zhang','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','张文博','zhang.wb@university.edu.cn','13800000004',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(5,'teacher_li','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','李嘉宁','li.jn@university.edu.cn','13800000005',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(6,'teacher_wang','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','王若愚','wang.ry@university.edu.cn','13800000006',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(7,'teacher_sun','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','孙晓峰','sun.xf@university.edu.cn','13800000007',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(8,'teacher_zhao','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','赵清扬','zhao.qy@university.edu.cn','13800000008',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(9,'teacher_huang','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','黄若兰','huang.rl@university.edu.cn','13800000009',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(10,'teacher_luo','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','罗景辰','luo.jc@university.edu.cn','13800000010',1,'2026-06-14 22:31:24','2026-06-14 22:31:24'),(11,'teacher_xu','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','许思远','xu.sy@university.edu.cn','13800000011',1,'2026-06-14 22:31:24','2026-06-14 22:31:24');

INSERT INTO `sys_user_role` VALUES (1,1,1,'2026-06-14 22:31:24'),(2,2,2,'2026-06-14 22:31:24'),(3,3,3,'2026-06-14 22:31:24'),(4,9,4,'2026-06-14 22:31:24'),(5,5,4,'2026-06-14 22:31:24'),(6,10,4,'2026-06-14 22:31:24'),(7,7,4,'2026-06-14 22:31:24'),(8,6,4,'2026-06-14 22:31:24'),(9,11,4,'2026-06-14 22:31:24'),(10,4,4,'2026-06-14 22:31:24'),(11,8,4,'2026-06-14 22:31:24');

-- 基础组织与学期
INSERT INTO `college` VALUES (1,'CS','计算机学院',1,'2026-06-20 19:00:00','2026-06-20 19:00:00');
INSERT INTO `major` VALUES (1,'080901','计算机科学与技术',1,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(2,'080902','软件工程',1,1,'2026-06-20 19:00:00','2026-06-20 19:00:00');
INSERT INTO `academic_term` VALUES (1,'2025-2026-1',2025,1,'2025-09-01','2026-01-16',1,'2026-06-20 19:00:00','2026-06-20 19:00:00');

-- 教师：保留当前教师用户账号关联，重新分配到两个演示专业
INSERT INTO `teacher` VALUES (1,'T2025001','张文博','教授',1,4,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(2,'T2025002','李嘉宁','副教授',1,5,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(3,'T2025003','王若愚','副教授',2,6,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(4,'T2025004','孙晓峰','讲师',2,7,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(5,'T2025005','赵清扬','教授',1,8,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(6,'T2025006','黄若兰','副教授',1,9,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(7,'T2025007','罗景辰','副教授',2,10,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(8,'T2025008','许思远','讲师',2,11,1,'2026-06-20 19:00:00','2026-06-20 19:00:00');

-- 课程
INSERT INTO `course` VALUES (1,'CS-MATH','高等数学A',4.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(2,'CS-PROG','程序设计基础',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(3,'CS-DS','数据结构',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(4,'CS-DB','数据库系统',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(5,'CS-OS','操作系统',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(6,'CS-NET','计算机网络',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(7,'CS-ARCH','计算机组成原理',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(8,'CS-COMP','编译原理',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(9,'CS-AI','人工智能导论',2.5,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(10,'CS-PRA','综合工程实践',2.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(11,'SE-MATH','离散数学',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(12,'SE-PROG','程序设计基础',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(13,'SE-DS','数据结构',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(14,'SE-REQ','软件需求分析',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(15,'SE-DESIGN','软件设计与体系结构',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(16,'SE-TEST','软件测试技术',3.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(17,'SE-PM','软件项目管理',2.5,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(18,'SE-DEVOPS','软件工程工具与环境',2.5,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(19,'SE-SEC','软件安全基础',2.5,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(20,'SE-PRA','软件工程实践',2.0,1,'2026-06-20 19:00:00','2026-06-20 19:00:00');

-- 课程-专业年级培养方案绑定
INSERT INTO `course_major` VALUES (1,1,1,2022,'2026-06-20 19:00:00'),
(2,1,1,2023,'2026-06-20 19:00:00'),
(3,2,1,2022,'2026-06-20 19:00:00'),
(4,2,1,2023,'2026-06-20 19:00:00'),
(5,3,1,2022,'2026-06-20 19:00:00'),
(6,3,1,2023,'2026-06-20 19:00:00'),
(7,4,1,2022,'2026-06-20 19:00:00'),
(8,4,1,2023,'2026-06-20 19:00:00'),
(9,5,1,2022,'2026-06-20 19:00:00'),
(10,5,1,2023,'2026-06-20 19:00:00'),
(11,6,1,2022,'2026-06-20 19:00:00'),
(12,6,1,2023,'2026-06-20 19:00:00'),
(13,7,1,2022,'2026-06-20 19:00:00'),
(14,7,1,2023,'2026-06-20 19:00:00'),
(15,8,1,2022,'2026-06-20 19:00:00'),
(16,8,1,2023,'2026-06-20 19:00:00'),
(17,9,1,2022,'2026-06-20 19:00:00'),
(18,9,1,2023,'2026-06-20 19:00:00'),
(19,10,1,2022,'2026-06-20 19:00:00'),
(20,10,1,2023,'2026-06-20 19:00:00'),
(21,11,2,2022,'2026-06-20 19:00:00'),
(22,11,2,2023,'2026-06-20 19:00:00'),
(23,12,2,2022,'2026-06-20 19:00:00'),
(24,12,2,2023,'2026-06-20 19:00:00'),
(25,13,2,2022,'2026-06-20 19:00:00'),
(26,13,2,2023,'2026-06-20 19:00:00'),
(27,14,2,2022,'2026-06-20 19:00:00'),
(28,14,2,2023,'2026-06-20 19:00:00'),
(29,15,2,2022,'2026-06-20 19:00:00'),
(30,15,2,2023,'2026-06-20 19:00:00'),
(31,16,2,2022,'2026-06-20 19:00:00'),
(32,16,2,2023,'2026-06-20 19:00:00'),
(33,17,2,2022,'2026-06-20 19:00:00'),
(34,17,2,2023,'2026-06-20 19:00:00'),
(35,18,2,2022,'2026-06-20 19:00:00'),
(36,18,2,2023,'2026-06-20 19:00:00'),
(37,19,2,2022,'2026-06-20 19:00:00'),
(38,19,2,2023,'2026-06-20 19:00:00'),
(39,20,2,2022,'2026-06-20 19:00:00'),
(40,20,2,2023,'2026-06-20 19:00:00');

-- 学生：每专业每年级40人
INSERT INTO `student` VALUES (1,'0809012022001','赵明轩',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(2,'0809012022002','钱子涵',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(3,'0809012022003','孙思远',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(4,'0809012022004','李雨桐',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(5,'0809012022005','周浩然',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(6,'0809012022006','吴若曦',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(7,'0809012022007','郑一凡',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(8,'0809012022008','王语晨',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(9,'0809012022009','冯景辰',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(10,'0809012022010','陈书瑶',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(11,'0809012022011','褚嘉宁',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(12,'0809012022012','卫清扬',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(13,'0809012022013','蒋知行',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(14,'0809012022014','沈沐阳',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(15,'0809012022015','韩可欣',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(16,'0809012022016','杨彦霖',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(17,'0809012022017','朱星辰',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(18,'0809012022018','秦予安',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(19,'0809012022019','尤文博',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(20,'0809012022020','许雅琪',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(21,'0809012022021','何承宇',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(22,'0809012022022','吕诗涵',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(23,'0809012022023','施俊逸',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(24,'0809012022024','张梦瑶',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(25,'0809012022025','孔泽宇',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(26,'0809012022026','曹欣然',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(27,'0809012022027','严睿哲',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(28,'0809012022028','华佳怡',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(29,'0809012022029','金晓峰',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(30,'0809012022030','魏若兰',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(31,'0809012022031','陶景文',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(32,'0809012022032','姜嘉怡',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(33,'0809012022033','赵启航',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(34,'0809012022034','钱舒涵',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(35,'0809012022035','孙宇轩',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(36,'0809012022036','李梓萱',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(37,'0809012022037','周博远',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(38,'0809012022038','吴晨曦',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(39,'0809012022039','郑雨泽',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(40,'0809012022040','王语嫣',1,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(41,'0809012023001','冯明轩',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(42,'0809012023002','陈子涵',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(43,'0809012023003','褚思远',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(44,'0809012023004','卫雨桐',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(45,'0809012023005','蒋浩然',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(46,'0809012023006','沈若曦',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(47,'0809012023007','韩一凡',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(48,'0809012023008','杨语晨',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(49,'0809012023009','朱景辰',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(50,'0809012023010','秦书瑶',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(51,'0809012023011','尤嘉宁',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(52,'0809012023012','许清扬',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(53,'0809012023013','何知行',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(54,'0809012023014','吕沐阳',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(55,'0809012023015','施可欣',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(56,'0809012023016','张彦霖',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(57,'0809012023017','孔星辰',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(58,'0809012023018','曹予安',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(59,'0809012023019','严文博',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(60,'0809012023020','华雅琪',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(61,'0809012023021','金承宇',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(62,'0809012023022','魏诗涵',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(63,'0809012023023','陶俊逸',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(64,'0809012023024','姜梦瑶',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(65,'0809012023025','赵泽宇',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(66,'0809012023026','钱欣然',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(67,'0809012023027','孙睿哲',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(68,'0809012023028','李佳怡',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(69,'0809012023029','周晓峰',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(70,'0809012023030','吴若兰',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(71,'0809012023031','郑景文',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(72,'0809012023032','王嘉怡',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(73,'0809012023033','冯启航',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(74,'0809012023034','陈舒涵',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(75,'0809012023035','褚宇轩',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(76,'0809012023036','卫梓萱',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(77,'0809012023037','蒋博远',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(78,'0809012023038','沈晨曦',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(79,'0809012023039','韩雨泽',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(80,'0809012023040','杨语嫣',1,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(81,'0809022022001','朱明轩',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(82,'0809022022002','秦子涵',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(83,'0809022022003','尤思远',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(84,'0809022022004','许雨桐',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(85,'0809022022005','何浩然',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(86,'0809022022006','吕若曦',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(87,'0809022022007','施一凡',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(88,'0809022022008','张语晨',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(89,'0809022022009','孔景辰',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(90,'0809022022010','曹书瑶',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(91,'0809022022011','严嘉宁',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(92,'0809022022012','华清扬',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(93,'0809022022013','金知行',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(94,'0809022022014','魏沐阳',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(95,'0809022022015','陶可欣',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(96,'0809022022016','姜彦霖',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(97,'0809022022017','赵星辰',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(98,'0809022022018','钱予安',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(99,'0809022022019','孙文博',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(100,'0809022022020','李雅琪',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(101,'0809022022021','周承宇',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(102,'0809022022022','吴诗涵',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(103,'0809022022023','郑俊逸',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(104,'0809022022024','王梦瑶',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(105,'0809022022025','冯泽宇',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(106,'0809022022026','陈欣然',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(107,'0809022022027','褚睿哲',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(108,'0809022022028','卫佳怡',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(109,'0809022022029','蒋晓峰',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(110,'0809022022030','沈若兰',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(111,'0809022022031','韩景文',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(112,'0809022022032','杨嘉怡',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(113,'0809022022033','朱启航',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(114,'0809022022034','秦舒涵',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(115,'0809022022035','尤宇轩',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(116,'0809022022036','许梓萱',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(117,'0809022022037','何博远',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(118,'0809022022038','吕晨曦',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(119,'0809022022039','施雨泽',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(120,'0809022022040','张语嫣',2,2022,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(121,'0809022023001','孔明轩',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(122,'0809022023002','曹子涵',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(123,'0809022023003','严思远',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(124,'0809022023004','华雨桐',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(125,'0809022023005','金浩然',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(126,'0809022023006','魏若曦',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(127,'0809022023007','陶一凡',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(128,'0809022023008','姜语晨',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(129,'0809022023009','赵景辰',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(130,'0809022023010','钱书瑶',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(131,'0809022023011','孙嘉宁',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(132,'0809022023012','李清扬',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(133,'0809022023013','周知行',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(134,'0809022023014','吴沐阳',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(135,'0809022023015','郑可欣',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(136,'0809022023016','王彦霖',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(137,'0809022023017','冯星辰',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(138,'0809022023018','陈予安',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(139,'0809022023019','褚文博',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(140,'0809022023020','卫雅琪',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(141,'0809022023021','蒋承宇',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(142,'0809022023022','沈诗涵',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(143,'0809022023023','韩俊逸',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(144,'0809022023024','杨梦瑶',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(145,'0809022023025','朱泽宇',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(146,'0809022023026','秦欣然',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(147,'0809022023027','尤睿哲',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(148,'0809022023028','许佳怡',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(149,'0809022023029','何晓峰',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(150,'0809022023030','吕若兰',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(151,'0809022023031','施景文',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(152,'0809022023032','张嘉怡',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(153,'0809022023033','孔启航',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(154,'0809022023034','曹舒涵',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(155,'0809022023035','严宇轩',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(156,'0809022023036','华梓萱',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(157,'0809022023037','金博远',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(158,'0809022023038','魏晨曦',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(159,'0809022023039','陶雨泽',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(160,'0809022023040','姜语嫣',2,2023,NULL,1,'2026-06-20 19:00:00','2026-06-20 19:00:00');

-- 毕业要求：每专业每年级8个
INSERT INTO `graduation_requirement` VALUES (1,'GR1','工程知识：能够将数学、自然科学、工程基础和计算机专业知识用于解决复杂工程问题。',1,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(2,'GR2','问题分析：能够应用数学、自然科学和工程科学基本原理识别、表达并分析复杂计算问题。',1,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(3,'GR3','设计开发解决方案：能够设计满足特定需求的软件、硬件或计算系统方案。',1,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(4,'GR4','研究：能够基于科学原理并采用科学方法对复杂工程问题进行研究。',1,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(5,'GR5','使用现代工具：能够针对复杂工程问题选择、使用和开发现代工程工具。',1,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(6,'GR6','工程与社会：能够评价计算机工程实践对社会、健康、安全、法律和文化的影响。',1,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(7,'GR7','职业规范：具有人文社会科学素养、社会责任感和工程职业道德。',1,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(8,'GR8','个人与团队：能够在多学科背景团队中承担个体、团队成员及负责人角色。',1,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(9,'GR1','工程知识：能够将数学、自然科学、工程基础和计算机专业知识用于解决复杂工程问题。',1,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(10,'GR2','问题分析：能够应用数学、自然科学和工程科学基本原理识别、表达并分析复杂计算问题。',1,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(11,'GR3','设计开发解决方案：能够设计满足特定需求的软件、硬件或计算系统方案。',1,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(12,'GR4','研究：能够基于科学原理并采用科学方法对复杂工程问题进行研究。',1,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(13,'GR5','使用现代工具：能够针对复杂工程问题选择、使用和开发现代工程工具。',1,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(14,'GR6','工程与社会：能够评价计算机工程实践对社会、健康、安全、法律和文化的影响。',1,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(15,'GR7','职业规范：具有人文社会科学素养、社会责任感和工程职业道德。',1,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(16,'GR8','个人与团队：能够在多学科背景团队中承担个体、团队成员及负责人角色。',1,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(17,'GR1','工程知识：能够将数学、工程基础和软件工程专业知识用于解决复杂软件工程问题。',2,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(18,'GR2','问题分析：能够识别、表达并分析复杂软件系统中的需求、设计和质量问题。',2,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(19,'GR3','设计开发解决方案：能够设计满足用户需求的软件系统、组件或过程。',2,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(20,'GR4','研究：能够采用实验、度量和分析方法研究复杂软件工程问题。',2,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(21,'GR5','使用现代工具：能够选择和使用软件建模、开发、测试和项目管理工具。',2,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(22,'GR6','工程与社会：能够评价软件工程实践对社会、法律、安全和伦理的影响。',2,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(23,'GR7','职业规范：具备职业责任、质量意识、知识产权意识和软件工程伦理。',2,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(24,'GR8','个人与团队：能够在软件项目团队中有效沟通、协作并承担责任。',2,2022,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(25,'GR1','工程知识：能够将数学、工程基础和软件工程专业知识用于解决复杂软件工程问题。',2,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(26,'GR2','问题分析：能够识别、表达并分析复杂软件系统中的需求、设计和质量问题。',2,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(27,'GR3','设计开发解决方案：能够设计满足用户需求的软件系统、组件或过程。',2,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(28,'GR4','研究：能够采用实验、度量和分析方法研究复杂软件工程问题。',2,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(29,'GR5','使用现代工具：能够选择和使用软件建模、开发、测试和项目管理工具。',2,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(30,'GR6','工程与社会：能够评价软件工程实践对社会、法律、安全和伦理的影响。',2,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(31,'GR7','职业规范：具备职业责任、质量意识、知识产权意识和软件工程伦理。',2,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(32,'GR8','个人与团队：能够在软件项目团队中有效沟通、协作并承担责任。',2,2023,1,'2026-06-20 19:00:00','2026-06-20 19:00:00');
-- 指标点：每个毕业要求2个
INSERT INTO `indicator_point` VALUES (1,'1-1','能够识别问题所需的理论知识和工程约束。',1,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(2,'1-2','能够将相关知识用于建模、推理和方案判断。',1,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(3,'2-1','能够分解复杂问题并提取关键因素。',2,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(4,'2-2','能够基于证据对问题进行分析并形成结论。',2,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(5,'3-1','能够提出满足需求和约束的系统方案。',3,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(6,'3-2','能够评价并改进设计方案的可行性和质量。',3,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(7,'4-1','能够设计实验、收集数据并分析结果。',4,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(8,'4-2','能够解释研究结果并形成有效结论。',4,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(9,'5-1','能够选择合适工具支撑分析、设计和实现。',5,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(10,'5-2','能够理解工具局限并进行必要改进。',5,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(11,'6-1','能够分析工程实践与社会因素的关系。',6,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(12,'6-2','能够评价方案可能产生的影响和风险。',6,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(13,'7-1','能够理解并遵守工程职业规范。',7,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(14,'7-2','能够在工程实践中体现责任意识和伦理意识。',7,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(15,'8-1','能够在团队中完成分工协作。',8,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(16,'8-2','能够进行有效沟通并支持团队目标实现。',8,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(17,'1-1','能够识别问题所需的理论知识和工程约束。',9,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(18,'1-2','能够将相关知识用于建模、推理和方案判断。',9,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(19,'2-1','能够分解复杂问题并提取关键因素。',10,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(20,'2-2','能够基于证据对问题进行分析并形成结论。',10,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(21,'3-1','能够提出满足需求和约束的系统方案。',11,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(22,'3-2','能够评价并改进设计方案的可行性和质量。',11,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(23,'4-1','能够设计实验、收集数据并分析结果。',12,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(24,'4-2','能够解释研究结果并形成有效结论。',12,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(25,'5-1','能够选择合适工具支撑分析、设计和实现。',13,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(26,'5-2','能够理解工具局限并进行必要改进。',13,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(27,'6-1','能够分析工程实践与社会因素的关系。',14,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(28,'6-2','能够评价方案可能产生的影响和风险。',14,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(29,'7-1','能够理解并遵守工程职业规范。',15,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(30,'7-2','能够在工程实践中体现责任意识和伦理意识。',15,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(31,'8-1','能够在团队中完成分工协作。',16,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(32,'8-2','能够进行有效沟通并支持团队目标实现。',16,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(33,'1-1','能够识别问题所需的理论知识和工程约束。',17,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(34,'1-2','能够将相关知识用于建模、推理和方案判断。',17,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(35,'2-1','能够分解复杂问题并提取关键因素。',18,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(36,'2-2','能够基于证据对问题进行分析并形成结论。',18,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(37,'3-1','能够提出满足需求和约束的系统方案。',19,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(38,'3-2','能够评价并改进设计方案的可行性和质量。',19,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(39,'4-1','能够设计实验、收集数据并分析结果。',20,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(40,'4-2','能够解释研究结果并形成有效结论。',20,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(41,'5-1','能够选择合适工具支撑分析、设计和实现。',21,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(42,'5-2','能够理解工具局限并进行必要改进。',21,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(43,'6-1','能够分析工程实践与社会因素的关系。',22,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(44,'6-2','能够评价方案可能产生的影响和风险。',22,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(45,'7-1','能够理解并遵守工程职业规范。',23,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(46,'7-2','能够在工程实践中体现责任意识和伦理意识。',23,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(47,'8-1','能够在团队中完成分工协作。',24,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(48,'8-2','能够进行有效沟通并支持团队目标实现。',24,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(49,'1-1','能够识别问题所需的理论知识和工程约束。',25,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(50,'1-2','能够将相关知识用于建模、推理和方案判断。',25,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(51,'2-1','能够分解复杂问题并提取关键因素。',26,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(52,'2-2','能够基于证据对问题进行分析并形成结论。',26,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(53,'3-1','能够提出满足需求和约束的系统方案。',27,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(54,'3-2','能够评价并改进设计方案的可行性和质量。',27,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(55,'4-1','能够设计实验、收集数据并分析结果。',28,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(56,'4-2','能够解释研究结果并形成有效结论。',28,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(57,'5-1','能够选择合适工具支撑分析、设计和实现。',29,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(58,'5-2','能够理解工具局限并进行必要改进。',29,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(59,'6-1','能够分析工程实践与社会因素的关系。',30,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(60,'6-2','能够评价方案可能产生的影响和风险。',30,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(61,'7-1','能够理解并遵守工程职业规范。',31,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(62,'7-2','能够在工程实践中体现责任意识和伦理意识。',31,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(63,'8-1','能够在团队中完成分工协作。',32,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(64,'8-2','能够进行有效沟通并支持团队目标实现。',32,1,'2026-06-20 19:00:00','2026-06-20 19:00:00');

-- 课程目标：每门课程3个
INSERT INTO `course_objective` VALUES (1,'CO1','高等数学A：运用微积分与线性代数工具分析工程问题。',1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(2,'CO2','高等数学A：建立数学模型并完成推导计算。',1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(3,'CO3','高等数学A：解释数学结果在计算问题中的意义。',1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(4,'CO1','程序设计基础：掌握程序设计基本语法和控制结构。',2,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(5,'CO2','程序设计基础：使用函数与数组解决基础计算问题。',2,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(6,'CO3','程序设计基础：完成规范的程序调试与测试。',2,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(7,'CO1','数据结构：掌握线性表、树、图等核心数据结构。',3,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(8,'CO2','数据结构：分析算法复杂度并选择合适结构。',3,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(9,'CO3','数据结构：实现并验证典型数据结构应用。',3,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(10,'CO1','数据库系统：理解关系模型和数据库设计方法。',4,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(11,'CO2','数据库系统：使用SQL完成数据定义和查询。',4,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(12,'CO3','数据库系统：设计并优化小型数据库应用。',4,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(13,'CO1','操作系统：理解进程、线程与调度机制。',5,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(14,'CO2','操作系统：分析存储管理和文件系统原理。',5,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(15,'CO3','操作系统：解释操作系统资源管理策略。',5,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(16,'CO1','计算机网络：理解网络体系结构和协议分层。',6,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(17,'CO2','计算机网络：分析典型网络协议的工作过程。',6,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(18,'CO3','计算机网络：完成基础网络配置与故障定位。',6,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(19,'CO1','计算机组成原理：理解计算机硬件组成和指令执行过程。',7,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(20,'CO2','计算机组成原理：分析存储系统和I/O组织方式。',7,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(21,'CO3','计算机组成原理：解释软硬件协同执行机制。',7,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(22,'CO1','编译原理：理解词法、语法和语义分析过程。',8,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(23,'CO2','编译原理：构造简单语言的分析程序。',8,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(24,'CO3','编译原理：解释编译优化和目标代码生成思想。',8,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(25,'CO1','人工智能导论：理解搜索、推理和机器学习基本方法。',9,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(26,'CO2','人工智能导论：使用基础算法解决智能问题。',9,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(27,'CO3','人工智能导论：分析人工智能应用的适用场景。',9,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(28,'CO1','综合工程实践：综合运用专业知识完成工程任务。',10,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(29,'CO2','综合工程实践：进行团队协作和过程管理。',10,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(30,'CO3','综合工程实践：形成规范的工程文档和展示成果。',10,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(31,'CO1','离散数学：掌握集合、关系、图论等离散结构。',11,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(32,'CO2','离散数学：使用逻辑推理描述软件问题。',11,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(33,'CO3','离散数学：将离散模型应用于软件设计分析。',11,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(34,'CO1','程序设计基础：掌握程序设计语法和基本算法。',12,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(35,'CO2','程序设计基础：编写结构清晰的基础程序。',12,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(36,'CO3','程序设计基础：完成程序调试、测试和文档说明。',12,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(37,'CO1','数据结构：理解常用数据结构及其操作。',13,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(38,'CO2','数据结构：根据软件需求选择数据组织方式。',13,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(39,'CO3','数据结构：实现数据结构支撑的软件模块。',13,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(40,'CO1','软件需求分析：掌握需求获取和需求建模方法。',14,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(41,'CO2','软件需求分析：编写清晰一致的软件需求规格说明。',14,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(42,'CO3','软件需求分析：识别并管理需求变更和约束。',14,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(43,'CO1','软件设计与体系结构：理解软件体系结构风格和设计原则。',15,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(44,'CO2','软件设计与体系结构：完成模块划分和接口设计。',15,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(45,'CO3','软件设计与体系结构：评估设计方案的质量属性。',15,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(46,'CO1','软件测试技术：掌握黑盒和白盒测试方法。',16,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(47,'CO2','软件测试技术：设计并执行有效测试用例。',16,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(48,'CO3','软件测试技术：分析缺陷并改进测试过程。',16,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(49,'CO1','软件项目管理：理解软件项目计划和进度管理。',17,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(50,'CO2','软件项目管理：进行风险、质量和配置管理。',17,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(51,'CO3','软件项目管理：使用项目数据支持管理决策。',17,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(52,'CO1','软件工程工具与环境：掌握版本控制和持续集成基础。',18,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(53,'CO2','软件工程工具与环境：配置常用软件开发工具链。',18,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(54,'CO3','软件工程工具与环境：改进团队协作和交付流程。',18,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(55,'CO1','软件安全基础：识别常见软件安全风险。',19,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(56,'CO2','软件安全基础：应用安全编码和测试方法。',19,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(57,'CO3','软件安全基础：分析软件系统的安全防护策略。',19,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(58,'CO1','软件工程实践：完成从需求到交付的软件项目。',20,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(59,'CO2','软件工程实践：实践团队协作和迭代开发。',20,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(60,'CO3','软件工程实践：提交可运行系统和工程文档。',20,'2026-06-20 19:00:00','2026-06-20 19:00:00');
-- 考核点：每门课程4个，总分100，分布到3个课程目标
INSERT INTO `assessment_point` VALUES (1,'平时作业(高等数学A)',20,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(2,'阶段测验(高等数学A)',20,1,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(3,'实验/项目(高等数学A)',30,2,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(4,'期末考核(高等数学A)',30,3,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(5,'平时作业(程序设计基础)',20,4,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(6,'阶段测验(程序设计基础)',20,4,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(7,'实验/项目(程序设计基础)',30,5,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(8,'期末考核(程序设计基础)',30,6,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(9,'平时作业(数据结构)',20,7,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(10,'阶段测验(数据结构)',20,7,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(11,'实验/项目(数据结构)',30,8,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(12,'期末考核(数据结构)',30,9,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(13,'平时作业(数据库系统)',20,10,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(14,'阶段测验(数据库系统)',20,10,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(15,'实验/项目(数据库系统)',30,11,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(16,'期末考核(数据库系统)',30,12,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(17,'平时作业(操作系统)',20,13,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(18,'阶段测验(操作系统)',20,13,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(19,'实验/项目(操作系统)',30,14,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(20,'期末考核(操作系统)',30,15,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(21,'平时作业(计算机网络)',20,16,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(22,'阶段测验(计算机网络)',20,16,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(23,'实验/项目(计算机网络)',30,17,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(24,'期末考核(计算机网络)',30,18,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(25,'平时作业(计算机组成原理)',20,19,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(26,'阶段测验(计算机组成原理)',20,19,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(27,'实验/项目(计算机组成原理)',30,20,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(28,'期末考核(计算机组成原理)',30,21,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(29,'平时作业(编译原理)',20,22,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(30,'阶段测验(编译原理)',20,22,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(31,'实验/项目(编译原理)',30,23,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(32,'期末考核(编译原理)',30,24,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(33,'平时作业(人工智能导论)',20,25,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(34,'阶段测验(人工智能导论)',20,25,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(35,'实验/项目(人工智能导论)',30,26,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(36,'期末考核(人工智能导论)',30,27,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(37,'平时作业(综合工程实践)',20,28,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(38,'阶段测验(综合工程实践)',20,28,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(39,'实验/项目(综合工程实践)',30,29,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(40,'期末考核(综合工程实践)',30,30,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(41,'平时作业(离散数学)',20,31,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(42,'阶段测验(离散数学)',20,31,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(43,'实验/项目(离散数学)',30,32,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(44,'期末考核(离散数学)',30,33,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(45,'平时作业(程序设计基础)',20,34,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(46,'阶段测验(程序设计基础)',20,34,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(47,'实验/项目(程序设计基础)',30,35,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(48,'期末考核(程序设计基础)',30,36,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(49,'平时作业(数据结构)',20,37,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(50,'阶段测验(数据结构)',20,37,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(51,'实验/项目(数据结构)',30,38,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(52,'期末考核(数据结构)',30,39,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(53,'平时作业(软件需求分析)',20,40,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(54,'阶段测验(软件需求分析)',20,40,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(55,'实验/项目(软件需求分析)',30,41,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(56,'期末考核(软件需求分析)',30,42,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(57,'平时作业(软件设计与体系结构)',20,43,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(58,'阶段测验(软件设计与体系结构)',20,43,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(59,'实验/项目(软件设计与体系结构)',30,44,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(60,'期末考核(软件设计与体系结构)',30,45,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(61,'平时作业(软件测试技术)',20,46,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(62,'阶段测验(软件测试技术)',20,46,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(63,'实验/项目(软件测试技术)',30,47,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(64,'期末考核(软件测试技术)',30,48,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(65,'平时作业(软件项目管理)',20,49,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(66,'阶段测验(软件项目管理)',20,49,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(67,'实验/项目(软件项目管理)',30,50,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(68,'期末考核(软件项目管理)',30,51,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(69,'平时作业(软件工程工具与环境)',20,52,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(70,'阶段测验(软件工程工具与环境)',20,52,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(71,'实验/项目(软件工程工具与环境)',30,53,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(72,'期末考核(软件工程工具与环境)',30,54,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(73,'平时作业(软件安全基础)',20,55,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(74,'阶段测验(软件安全基础)',20,55,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(75,'实验/项目(软件安全基础)',30,56,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(76,'期末考核(软件安全基础)',30,57,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(77,'平时作业(软件工程实践)',20,58,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(78,'阶段测验(软件工程实践)',20,58,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(79,'实验/项目(软件工程实践)',30,59,'2026-06-20 19:00:00','2026-06-20 19:00:00'),
(80,'期末考核(软件工程实践)',30,60,'2026-06-20 19:00:00','2026-06-20 19:00:00');

-- 核心演示流程表保持空表：teaching_class、student_class、course_indicator_support、objective_indicator_contribution、成绩、计算结果、审计记录。

-- ============================================================
-- 合并自 demo_update_major_specific_requirements_20260620.sql
-- 按专业区分毕业要求编号/描述与指标点描述
-- ============================================================
-- 演示库增量修正：按专业区分毕业要求与指标点描述
-- 适用前提：已执行 demo_rebuild_seed_20260620.sql
-- 说明：只更新 graduation_requirement 的编号/描述与 indicator_point 的描述，不新增核心流程数据。

SET NAMES utf8mb4;
USE `GraduationDB`;

-- ============================================================
-- 计算机科学与技术：强调计算理论、系统能力、算法与计算机工程实践
-- ============================================================
UPDATE graduation_requirement gr
JOIN major m ON m.major_id = gr.major_id
SET gr.gr_description = CASE SUBSTRING_INDEX(gr.gr_code, '-', -1)
    WHEN 'GR1' THEN '工程知识：能够将数学、自然科学、工程基础、计算机组成、程序设计、算法与系统软件知识用于解决复杂计算机工程问题。'
    WHEN 'GR2' THEN '问题分析：能够应用数学、算法分析、系统建模和计算机专业知识识别、表达并分析复杂计算机系统问题。'
    WHEN 'GR3' THEN '设计开发解决方案：能够面向复杂计算需求设计计算机软硬件系统、算法模块、数据管理方案或网络系统方案。'
    WHEN 'GR4' THEN '研究：能够基于计算机科学原理，采用实验、仿真、性能测试和数据分析方法研究复杂计算问题。'
    WHEN 'GR5' THEN '使用现代工具：能够选择并使用开发环境、数据库、网络分析、系统调试、性能分析等现代工具解决计算机工程问题。'
    WHEN 'GR6' THEN '工程与社会：能够评价计算机系统、网络服务和数据应用对社会、健康、安全、法律、隐私和文化的影响。'
    WHEN 'GR7' THEN '职业规范：具有人文社会科学素养、社会责任感、计算机职业道德、数据伦理和知识产权意识。'
    WHEN 'GR8' THEN '个人与团队：能够在计算机系统开发、算法实现和工程集成团队中承担个体、成员和负责人角色。'
    ELSE gr.gr_description
END,
    gr.gr_code = CONCAT('CS-', SUBSTRING_INDEX(gr.gr_code, '-', -1))
WHERE m.major_code = '080901';

UPDATE indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN major m ON m.major_id = gr.major_id
SET ip.ip_description = CASE CONCAT(SUBSTRING_INDEX(gr.gr_code, '-', -1), '|', ip.ip_code)
    WHEN 'GR1|1-1' THEN '能够运用数学、离散结构、算法和程序设计知识描述计算机工程问题。'
    WHEN 'GR1|1-2' THEN '能够综合计算机组成、操作系统、数据库和网络知识分析系统运行机制。'
    WHEN 'GR2|2-1' THEN '能够识别复杂计算机系统问题中的关键数据结构、算法复杂度和系统约束。'
    WHEN 'GR2|2-2' THEN '能够基于日志、实验结果和性能指标分析问题原因并形成结论。'
    WHEN 'GR3|3-1' THEN '能够设计满足功能、性能和可靠性要求的软件或计算系统方案。'
    WHEN 'GR3|3-2' THEN '能够完成模块划分、接口设计、数据组织和算法实现方案评价。'
    WHEN 'GR4|4-1' THEN '能够设计计算机系统实验、仿真或性能测试方案并采集有效数据。'
    WHEN 'GR4|4-2' THEN '能够对实验数据进行分析解释，验证算法、系统或网络方案的有效性。'
    WHEN 'GR5|5-1' THEN '能够使用集成开发环境、调试器、数据库工具和网络工具支撑工程实现。'
    WHEN 'GR5|5-2' THEN '能够理解工具适用边界，并结合脚本、自动化或测试工具提高开发效率。'
    WHEN 'GR6|6-1' THEN '能够分析计算机系统部署对隐私保护、网络安全和社会运行的影响。'
    WHEN 'GR6|6-2' THEN '能够评价计算机工程方案在法律合规、安全风险和社会责任方面的约束。'
    WHEN 'GR7|7-1' THEN '能够理解计算机职业规范、开源协议、知识产权和数据伦理要求。'
    WHEN 'GR7|7-2' THEN '能够在系统设计、数据处理和软件开发中体现责任意识和职业伦理。'
    WHEN 'GR8|8-1' THEN '能够在计算机工程团队中完成算法、编码、测试或系统集成任务。'
    WHEN 'GR8|8-2' THEN '能够与团队成员有效沟通，协调接口、进度和质量目标。'
    ELSE ip.ip_description
END
WHERE m.major_code = '080901';

-- ============================================================
-- 软件工程：强调需求、设计、测试、过程管理、质量保障与工程交付
-- ============================================================
UPDATE graduation_requirement gr
JOIN major m ON m.major_id = gr.major_id
SET gr.gr_description = CASE SUBSTRING_INDEX(gr.gr_code, '-', -1)
    WHEN 'GR1' THEN '工程知识：能够将数学、工程基础、程序设计、软件建模、软件质量和项目管理知识用于解决复杂软件工程问题。'
    WHEN 'GR2' THEN '问题分析：能够识别、表达并分析复杂软件系统中的需求冲突、设计缺陷、质量风险和过程问题。'
    WHEN 'GR3' THEN '设计开发解决方案：能够面向用户需求设计软件系统、组件、接口、数据库和测试方案，并考虑质量属性约束。'
    WHEN 'GR4' THEN '研究：能够采用需求调研、原型验证、软件度量、测试实验和缺陷分析方法研究复杂软件工程问题。'
    WHEN 'GR5' THEN '使用现代工具：能够使用建模、版本控制、持续集成、自动化测试、缺陷管理和项目管理工具支撑软件开发。'
    WHEN 'GR6' THEN '工程与社会：能够评价软件系统对业务流程、用户体验、数据安全、法律合规和社会运行的影响。'
    WHEN 'GR7' THEN '职业规范：具备软件工程职业责任、质量意识、团队规范、知识产权意识、数据伦理和安全意识。'
    WHEN 'GR8' THEN '个人与团队：能够在软件项目团队中承担需求、设计、开发、测试、配置管理或项目协调等角色。'
    ELSE gr.gr_description
END,
    gr.gr_code = CONCAT('SE-', SUBSTRING_INDEX(gr.gr_code, '-', -1))
WHERE m.major_code = '080902';

UPDATE indicator_point ip
JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id
JOIN major m ON m.major_id = gr.major_id
SET ip.ip_description = CASE CONCAT(SUBSTRING_INDEX(gr.gr_code, '-', -1), '|', ip.ip_code)
    WHEN 'GR1|1-1' THEN '能够运用程序设计、数据结构、数据库和软件工程基础知识描述软件问题。'
    WHEN 'GR1|1-2' THEN '能够综合需求分析、软件设计、测试和项目管理知识支撑软件工程决策。'
    WHEN 'GR2|2-1' THEN '能够识别复杂软件系统中的用户需求、业务规则、质量属性和工程约束。'
    WHEN 'GR2|2-2' THEN '能够基于需求文档、缺陷数据、测试结果和用户反馈分析问题原因。'
    WHEN 'GR3|3-1' THEN '能够设计满足功能需求和质量属性的软件架构、模块和接口方案。'
    WHEN 'GR3|3-2' THEN '能够结合可维护性、可测试性、安全性和可扩展性评价并改进设计方案。'
    WHEN 'GR4|4-1' THEN '能够设计需求验证、原型评估、软件测试或质量度量实验。'
    WHEN 'GR4|4-2' THEN '能够分析测试数据、缺陷分布和度量结果，形成软件改进结论。'
    WHEN 'GR5|5-1' THEN '能够使用建模工具、版本控制、持续集成和自动化测试工具支撑开发过程。'
    WHEN 'GR5|5-2' THEN '能够理解软件工具链局限，并根据项目需要配置流程或改进工具使用方式。'
    WHEN 'GR6|6-1' THEN '能够分析软件系统上线对业务、用户、数据安全和法律合规的影响。'
    WHEN 'GR6|6-2' THEN '能够评价软件方案在隐私保护、可用性、可靠性和社会责任方面的风险。'
    WHEN 'GR7|7-1' THEN '能够理解软件工程职业规范、质量标准、开源协议和知识产权要求。'
    WHEN 'GR7|7-2' THEN '能够在需求、设计、编码、测试和交付活动中体现质量意识和职业伦理。'
    WHEN 'GR8|8-1' THEN '能够在软件项目团队中承担需求分析、开发、测试或配置管理任务。'
    WHEN 'GR8|8-2' THEN '能够通过评审、会议、文档和协作工具推动团队目标达成。'
    ELSE ip.ip_description
END
WHERE m.major_code = '080902';

SET UNIQUE_CHECKS=1;
SET FOREIGN_KEY_CHECKS=1;
