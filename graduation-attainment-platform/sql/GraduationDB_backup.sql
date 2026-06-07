-- MySQL dump 10.13  Distrib 8.4.9, for Linux (x86_64)
--
-- Host: localhost    Database: GraduationDB
-- ------------------------------------------------------
-- Server version	8.4.9

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `academic_term`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `academic_term`
--

LOCK TABLES `academic_term` WRITE;
/*!40000 ALTER TABLE `academic_term` DISABLE KEYS */;
INSERT INTO `academic_term` VALUES (1,'2024-2025-1',2024,1,'2024-09-01','2025-01-18',1,'2026-05-30 21:19:11','2026-05-30 21:19:11'),(2,'2024-2025-2',2024,2,'2025-02-24','2025-07-05',1,'2026-05-30 21:19:11','2026-05-30 21:19:11'),(3,'2025-2026-1',2025,1,'2025-09-01','2026-01-17',1,'2026-05-30 21:19:11','2026-05-30 21:19:11');
/*!40000 ALTER TABLE `academic_term` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assessment_point`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assessment_point`
--

LOCK TABLES `assessment_point` WRITE;
/*!40000 ALTER TABLE `assessment_point` DISABLE KEYS */;
INSERT INTO `assessment_point` VALUES (1,'期末卷-链表操作题',15,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(2,'期末卷-栈队列应用题',10,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'实验-二叉树遍历实现',20,2,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'期末卷-图算法题',15,2,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'期末卷-排序算法分析',10,3,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,'实验-查找算法对比',15,3,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(7,'课程设计-综合编程',20,4,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(8,'期末卷-进程调度题',15,5,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(9,'实验-多线程编程',20,5,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(10,'期末卷-内存管理题',15,6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(11,'实验-页面置换模拟',15,6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(12,'期末卷-文件系统题',10,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(13,'实验-磁盘调度模拟',10,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(14,'期末卷-TCP/IP协议题',15,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(15,'实验-Wireshark抓包分析',15,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(16,'期末卷-路由算法题',10,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(17,'实验-网络拓扑设计',20,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(18,'期末卷-Socket编程题',10,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(19,'实验-简易聊天室开发',15,10,'2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `assessment_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `calc_audit_log`
--

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `calc_audit_log`
--

LOCK TABLES `calc_audit_log` WRITE;
/*!40000 ALTER TABLE `calc_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `calc_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `college`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `college`
--

LOCK TABLES `college` WRITE;
/*!40000 ALTER TABLE `college` DISABLE KEYS */;
INSERT INTO `college` VALUES (1,'CS','计算机科学与技术学院',1,'2026-05-30 21:19:11','2026-05-30 21:19:11'),(2,'EE','电子信息工程学院',1,'2026-05-30 21:19:11','2026-05-30 21:19:11');
/*!40000 ALTER TABLE `college` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,'CS201','数据结构',4,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(2,'CS301','操作系统',3,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'CS302','计算机网络',3,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'SW201','软件工程',3,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'EE301','数字电路与逻辑设计',3,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,'CS202','离散数学',3.5,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(7,'CS203','计算机组成原理',3.5,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(8,'CS204','数据库原理',3,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(9,'CS205','编译原理',3,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(10,'CS206','算法设计与分析',3,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(11,'CS207','人工智能导论',2.5,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(12,'CS208','计算机图形学',2.5,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(13,'CS209','嵌入式系统',3,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(14,'CS210','软件测试',2,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(15,'CS211','信息安全导论',2,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(16,'CS220','网络与信息安全',2,1,'2026-05-31 14:48:55','2026-05-31 14:49:11'),(17,'CS230','Python程序设计',2,1,'2026-05-31 15:14:29','2026-05-31 15:14:29'),(18,'CS231','机器学习基础',3,1,'2026-05-31 15:14:29','2026-05-31 15:14:29'),(19,'SW202','软件项目管理',2,1,'2026-05-31 15:14:29','2026-05-31 15:14:29'),(20,'EE302','单片机原理与接口技术',3,1,'2026-05-31 15:14:29','2026-05-31 15:14:29'),(21,'CS240','数据挖掘',2.5,1,'2026-05-31 15:17:07','2026-05-31 15:17:07'),(22,'CS250','数据可视化',2,1,'2026-06-01 10:27:41','2026-06-01 11:07:49'),(23,'CS251','自然语言处理基础',3,1,'2026-06-01 10:27:41','2026-06-01 11:07:49'),(24,'SW203','软件质量保证',2,1,'2026-06-01 10:27:41','2026-06-01 11:07:51'),(25,'EE303','传感器与检测技术',3,1,'2026-06-01 10:27:42','2026-06-01 11:07:49'),(26,'CS252','区块链导论',2,1,'2026-06-01 10:58:57','2026-06-01 10:58:57'),(27,'CS263','知识工程导论',2,1,'2026-06-01 11:01:07','2026-06-01 11:01:07'),(28,'CS260','数字取证技术',2,1,'2026-06-01 11:01:46','2026-06-01 11:01:46'),(29,'CS261','智能系统工程',2.5,1,'2026-06-01 11:01:47','2026-06-01 11:01:47'),(30,'SW204','软件过程度量',2,1,'2026-06-01 11:01:47','2026-06-01 11:01:47'),(31,'EE304','嵌入式接口技术',3,1,'2026-06-01 11:01:47','2026-06-01 11:01:47'),(32,'CS280','联邦学习基础',2,1,'2026-06-03 14:28:09','2026-06-03 14:28:09'),(33,'CS281','数字孪生导论',2.5,1,'2026-06-03 14:28:09','2026-06-03 14:28:09'),(34,'SW206','软件演化与维护',2,1,'2026-06-03 14:28:10','2026-06-03 14:28:10'),(35,'EE306','边缘感知系统',2.5,1,'2026-06-03 14:28:10','2026-06-03 14:28:10'),(36,'TESTCS301','软件质量保证',2,1,'2026-06-03 21:59:00','2026-06-03 21:59:00'),(37,'TESTCS302','专业认证导论',1.5,1,'2026-06-03 21:59:00','2026-06-03 21:59:00'),(38,'TESTSE401','软件过程改进',2,0,'2026-06-03 21:59:00','2026-06-03 21:59:00');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_indicator_achievement`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_indicator_achievement`
--

LOCK TABLES `course_indicator_achievement` WRITE;
/*!40000 ALTER TABLE `course_indicator_achievement` DISABLE KEYS */;
INSERT INTO `course_indicator_achievement` VALUES (4,2,2,0.79,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,2,3,0.72,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,2,6,0.74,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(7,3,2,0.8,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(8,3,4,0.76,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(9,3,10,0.78,1,'2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `course_indicator_achievement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_indicator_support`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=439 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_indicator_support`
--

LOCK TABLES `course_indicator_support` WRITE;
/*!40000 ALTER TABLE `course_indicator_support` DISABLE KEYS */;
INSERT INTO `course_indicator_support` VALUES (402,1,1,0.4,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(403,1,5,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(404,1,9,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(405,6,1,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(406,6,5,0.25,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(407,6,7,0.5,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(408,7,1,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(409,7,6,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(410,7,8,0.35,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(411,8,2,0.4,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(412,8,3,0.4,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(413,8,5,0.25,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(414,8,10,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(415,9,4,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(416,9,5,0.2,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(417,9,11,0.25,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(418,10,3,0.2,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(419,10,7,0.5,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(420,10,9,0.35,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(421,10,10,0.2,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(422,10,12,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(423,11,8,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(424,11,9,0.35,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(425,11,11,0.35,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(426,12,12,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(427,14,6,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(428,18,12,0.2,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(429,21,8,0.35,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(430,2,2,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(431,2,3,0.4,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(432,2,4,0.1,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(433,2,6,0.4,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(434,2,12,0.2,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(435,3,2,0.3,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(436,3,4,0.6,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(437,3,10,0.5,'2026-06-03 23:30:27','2026-06-03 23:30:27'),(438,3,11,0.4,'2026-06-03 23:30:27','2026-06-03 23:30:27');
/*!40000 ALTER TABLE `course_indicator_support` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_major`
--

DROP TABLE IF EXISTS `course_major`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_major` (
  `cm_id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `major_id` bigint NOT NULL,
  `grade_year` int NOT NULL DEFAULT '2022',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cm_id`),
  UNIQUE KEY `uk_course_major_grade` (`course_id`,`major_id`,`grade_year`),
  KEY `idx_cm_course` (`course_id`),
  KEY `idx_cm_major` (`major_id`),
  KEY `idx_cm_major_grade` (`major_id`,`grade_year`),
  CONSTRAINT `course_major_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `course_major_ibfk_2` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_major`
--

LOCK TABLES `course_major` WRITE;
/*!40000 ALTER TABLE `course_major` DISABLE KEYS */;
INSERT INTO `course_major` VALUES (2,2,1,2022,'2026-05-30 21:19:12'),(3,3,1,2022,'2026-05-30 21:19:12'),(4,4,2,2022,'2026-05-30 21:19:12'),(5,5,3,2022,'2026-05-30 21:19:12'),(6,6,1,2022,'2026-05-30 21:19:44'),(7,7,1,2022,'2026-05-30 21:19:44'),(8,8,1,2022,'2026-05-30 21:19:44'),(9,9,1,2022,'2026-05-30 21:19:44'),(10,10,1,2022,'2026-05-30 21:19:44'),(11,11,1,2022,'2026-05-30 21:19:44'),(12,12,1,2022,'2026-05-30 21:19:44'),(13,13,1,2022,'2026-05-30 21:19:44'),(14,14,1,2022,'2026-05-30 21:19:44'),(15,15,1,2022,'2026-05-30 21:19:44'),(18,16,2,2022,'2026-05-31 14:49:08'),(19,16,1,2022,'2026-05-31 14:49:08'),(20,17,1,2022,'2026-05-31 15:14:29'),(21,18,1,2022,'2026-05-31 15:14:29'),(22,19,2,2022,'2026-05-31 15:14:29'),(23,20,3,2022,'2026-05-31 15:14:29'),(24,21,1,2022,'2026-05-31 15:17:07'),(25,22,1,2022,'2026-06-01 10:27:41'),(26,23,1,2022,'2026-06-01 10:27:41'),(27,24,2,2022,'2026-06-01 10:27:41'),(28,25,3,2022,'2026-06-01 10:27:42'),(29,26,1,2022,'2026-06-01 10:58:57'),(30,27,1,2022,'2026-06-01 11:01:07'),(31,28,1,2022,'2026-06-01 11:01:46'),(32,29,1,2022,'2026-06-01 11:01:47'),(33,30,2,2022,'2026-06-01 11:01:47'),(34,31,3,2022,'2026-06-01 11:01:47'),(35,32,1,2022,'2026-06-03 14:28:09'),(36,33,1,2022,'2026-06-03 14:28:09'),(37,34,2,2022,'2026-06-03 14:28:10'),(38,35,3,2022,'2026-06-03 14:28:10'),(39,1,1,2022,'2026-06-03 18:37:09'),(40,1,1,2025,'2026-06-03 18:37:09'),(41,36,1,2022,'2026-06-03 21:59:00'),(42,37,1,2022,'2026-06-03 21:59:00'),(43,38,2,2023,'2026-06-03 21:59:00');
/*!40000 ALTER TABLE `course_major` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_objective`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_objective`
--

LOCK TABLES `course_objective` WRITE;
/*!40000 ALTER TABLE `course_objective` DISABLE KEYS */;
INSERT INTO `course_objective` VALUES (1,'CO1','掌握线性表、栈、队列、串等基本数据结构的逻辑结构与物理实现',1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(2,'CO2','掌握树、图等复杂数据结构的定义、存储方式及遍历算法',1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'CO3','能运用查找和排序算法解决实际应用问题，并分析算法的时间空间复杂度',1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'CO4','能针对具体问题选择恰当的数据结构并编写C++/Java高效实现代码',1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'CO1','理解进程、线程的概念及调度算法，掌握并发编程的基本方法',2,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,'CO2','理解内存管理机制，包括分页、分段、虚拟内存及页面置换算法',2,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(7,'CO3','理解文件系统和I/O子系统的设计原理及磁盘调度策略',2,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(8,'CO1','掌握TCP/IP协议栈各层功能及常见协议（HTTP/DNS/TCP/IP）',3,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(9,'CO2','理解路由算法、拥塞控制机制，能进行网络拓扑设计与性能分析',3,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(10,'CO3','掌握Socket编程并能搭建简单的客户端/服务器网络应用',3,'2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `course_objective` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_objective_achievement`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_objective_achievement`
--

LOCK TABLES `course_objective_achievement` WRITE;
/*!40000 ALTER TABLE `course_objective_achievement` DISABLE KEYS */;
INSERT INTO `course_objective_achievement` VALUES (5,2,5,0.79,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,2,6,0.72,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(7,2,7,0.74,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(8,3,8,0.8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(9,3,9,0.76,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(10,3,10,0.78,'2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `course_objective_achievement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `graduation_requirement`
--

DROP TABLE IF EXISTS `graduation_requirement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `graduation_requirement` (
  `gr_id` bigint NOT NULL AUTO_INCREMENT,
  `gr_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gr_description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `major_id` bigint NOT NULL,
  `grade_year` int NOT NULL DEFAULT '2022',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`gr_id`),
  UNIQUE KEY `uk_major_grade_gr_code` (`major_id`,`grade_year`,`gr_code`),
  KEY `idx_req_major` (`major_id`),
  KEY `idx_req_major_grade` (`major_id`,`grade_year`),
  CONSTRAINT `graduation_requirement_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `major` (`major_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `graduation_requirement`
--

LOCK TABLES `graduation_requirement` WRITE;
/*!40000 ALTER TABLE `graduation_requirement` DISABLE KEYS */;
INSERT INTO `graduation_requirement` VALUES (1,'1','工程知识：能够将数学、自然科学、工程基础和专业知识用于解决复杂工程问题',1,2022,1,'2026-05-30 21:19:12','2026-06-03 21:26:04'),(2,'2','问题分析：能够应用数学、自然科学和工程科学的基本原理，识别、表达并通过文献研究分析复杂工程问题',1,2022,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'3','设计/开发解决方案：能够设计针对复杂工程问题的解决方案，并能够在设计环节中体现创新意识',1,2022,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'4','研究：能够基于科学原理并采用科学方法对复杂工程问题进行研究',1,2022,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'5','使用现代工具：能够针对复杂工程问题，开发、选择与使用恰当的技术、资源、现代工程工具',1,2022,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,'6','工程与社会：能够基于工程相关背景知识进行合理分析，评价专业工程实践和复杂工程问题解决方案对社会的影响',1,2022,1,'2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `graduation_requirement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `indicator_point`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `indicator_point`
--

LOCK TABLES `indicator_point` WRITE;
/*!40000 ALTER TABLE `indicator_point` DISABLE KEYS */;
INSERT INTO `indicator_point` VALUES (1,'1.1','能将数学和自然科学的基本概念运用于计算机工程问题的建模与求解',1,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(2,'1.2','能运用工程基础知识解释计算机系统的设计原理与工作机制',1,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'2.1','能识别和判断计算机复杂工程问题的关键环节与技术瓶颈',2,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'2.2','能通过查阅文献对计算机复杂工程问题进行深入分析与分解',2,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'3.1','能设计满足特定需求的算法、模块或软件系统架构方案',3,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,'3.2','能够在系统设计中综合考虑安全性、经济性、环境适应性等非技术因素',3,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(7,'4.1','能针对计算机复杂工程问题设计有效的实验方案并正确采集数据',4,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(8,'4.2','能运用统计学方法对实验数据进行科学分析与解释，得出有效结论',4,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(9,'5.1','能熟练使用主流开发工具、调试工具和性能分析工具完成软件开发任务',5,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(10,'5.2','能根据具体问题选择并运用适当的仿真软件或云计算平台进行模拟分析',5,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(11,'6.1','理解计算机技术发展对社会、法律及伦理的影响，具有社会责任感',6,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(12,'6.2','能在工程实践中考虑信息安全、知识产权保护等社会约束因素',6,1,'2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `indicator_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `major`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `major`
--

LOCK TABLES `major` WRITE;
/*!40000 ALTER TABLE `major` DISABLE KEYS */;
INSERT INTO `major` VALUES (1,'080901','计算机科学与技术',1,1,'2026-05-30 21:19:11','2026-05-30 21:19:11'),(2,'080902','软件工程',1,1,'2026-05-30 21:19:11','2026-05-30 21:19:11'),(3,'080701','电子信息工程',2,1,'2026-05-30 21:19:11','2026-05-30 21:19:11');
/*!40000 ALTER TABLE `major` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `major_indicator_achievement`
--

DROP TABLE IF EXISTS `major_indicator_achievement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `major_indicator_achievement` (
  `mia_id` bigint NOT NULL AUTO_INCREMENT,
  `major_id` bigint NOT NULL,
  `grade_year` int NOT NULL DEFAULT '2022',
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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `major_indicator_achievement`
--

LOCK TABLES `major_indicator_achievement` WRITE;
/*!40000 ALTER TABLE `major_indicator_achievement` DISABLE KEYS */;
/*!40000 ALTER TABLE `major_indicator_achievement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `objective_indicator_contribution`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `objective_indicator_contribution`
--

LOCK TABLES `objective_indicator_contribution` WRITE;
/*!40000 ALTER TABLE `objective_indicator_contribution` DISABLE KEYS */;
INSERT INTO `objective_indicator_contribution` VALUES (6,5,2,0.5,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(7,6,3,0.65,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(8,7,6,0.55,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(9,8,2,0.5,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(10,9,4,0.6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(11,10,10,0.8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(27,1,1,0.6,'2026-06-07 10:24:41','2026-06-07 10:24:41'),(28,1,5,0.1,'2026-06-07 10:24:41','2026-06-07 10:24:41'),(29,2,1,0.1,'2026-06-07 10:24:41','2026-06-07 10:24:41'),(30,2,5,0.5,'2026-06-07 10:24:41','2026-06-07 10:24:41'),(31,3,1,0.1,'2026-06-07 10:24:41','2026-06-07 10:24:41'),(32,3,9,0.7,'2026-06-07 10:24:41','2026-06-07 10:24:41'),(33,4,1,0.2,'2026-06-07 10:24:41','2026-06-07 10:24:41'),(34,4,5,0.4,'2026-06-07 10:24:41','2026-06-07 10:24:41'),(35,4,9,0.3,'2026-06-07 10:24:41','2026-06-07 10:24:41');
/*!40000 ALTER TABLE `objective_indicator_contribution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,'20220101001','周一帆',1,2022,7,1,'2026-05-30 21:19:12','2026-06-01 15:38:04'),(2,'20220101002','陈逢源',1,2022,8,1,'2026-05-30 21:19:12','2026-06-01 15:38:33'),(3,'20220101003','林晓彤',1,2022,NULL,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'20220101004','王浩然',1,2022,NULL,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'20220101005','赵雨涵',1,2022,NULL,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,'20220101006','刘子轩',1,2022,NULL,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(7,'20220101007','黄诗琪',1,2022,NULL,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(8,'20220101008','杨俊杰',1,2022,NULL,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(9,'20220101009','吴佳怡',1,2022,NULL,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(10,'20220101010','郑明辉',1,2022,NULL,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(11,'20220101011','张伟',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(12,'20220101012','李强',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(13,'20220101013','王磊',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(14,'20220101014','赵明',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(15,'20220101015','刘洋',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(16,'20220101016','陈刚',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(17,'20220101017','杨帆',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(18,'20220101018','黄勇',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(19,'20220101019','周杰',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(20,'20220101020','吴昊',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(21,'20220101021','孙丽',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(22,'20220101022','马玲',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(23,'20220101023','朱婷',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(24,'20220101024','胡敏',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(25,'20220101025','林芳',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(26,'20220101026','何静',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(27,'20220101027','郭娜',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(28,'20220101028','高洁',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(29,'20220101029','罗琳',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(30,'20220101030','梁雪',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(31,'20220101031','宋涛',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(32,'20220101032','唐磊',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(33,'20220101033','韩冰',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(34,'20220101034','冯浩',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(35,'20220101035','董文',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(36,'20220101036','程亮',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(37,'20220101037','曹峰',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(38,'20220101038','袁博',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(39,'20220101039','邓超',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(40,'20220101040','许刚',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(41,'20220101041','沈璐',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(42,'20220101042','彭娟',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(43,'20220101043','吕萍',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(44,'20220101044','苏艳',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(45,'20220101045','蒋颖',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(46,'20220101046','蔡宇',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(47,'20220101047','贾琪',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(48,'20220101048','丁蕾',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(49,'20220101049','魏芳',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(50,'20220101050','薛敏',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(51,'20220101051','叶飞',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(52,'20220101052','余波',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(53,'20220101053','潘越',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(54,'20220101054','戴晴',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(55,'20220101055','夏雨',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(56,'20220101056','田晓',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(57,'20220101057','任远',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(58,'20220101058','姜琳',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(59,'20220101059','范鑫',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(60,'20220101060','方圆',1,2022,NULL,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(61,'20240101001','张晓晨',1,2024,NULL,1,'2026-05-31 19:25:30','2026-05-31 19:25:30'),(62,'20240101002','李思雨',1,2024,NULL,1,'2026-05-31 19:25:30','2026-05-31 19:25:30'),(63,'20240102001','周嘉宁',2,2024,NULL,1,'2026-05-31 19:25:30','2026-05-31 19:25:30'),(64,'20240102002','陈宇航',2,2024,NULL,1,'2026-05-31 19:25:30','2026-05-31 19:25:30'),(65,'20240103001','王一鸣',3,2024,NULL,1,'2026-05-31 19:25:30','2026-05-31 19:25:30'),(66,'20250101001','张可馨',1,2025,NULL,1,'2026-06-01 10:29:21','2026-06-01 11:09:29'),(67,'20250101002','刘书宁',1,2025,NULL,1,'2026-06-01 10:29:21','2026-06-01 11:09:29'),(68,'20250102001','何知远',2,2025,NULL,1,'2026-06-01 10:29:21','2026-06-01 11:09:29'),(69,'20250102002','孙若彤',2,2025,NULL,1,'2026-06-01 10:29:21','2026-06-01 11:09:29'),(70,'20250103001','郑博文',3,2025,NULL,1,'2026-06-01 10:29:21','2026-06-01 11:09:30'),(71,'20250101011','许晨曦',1,2025,NULL,1,'2026-06-01 10:29:31','2026-06-01 11:09:29'),(72,'20260101011','许晨曦',1,2026,NULL,1,'2026-06-01 16:24:18','2026-06-01 16:24:18'),(73,'20270101001','顾知行',1,2027,NULL,1,'2026-06-03 14:30:36','2026-06-03 14:30:36'),(74,'20270101002','林若溪',1,2027,NULL,1,'2026-06-03 14:30:36','2026-06-03 14:30:36'),(75,'20270102001','宋嘉言',2,2027,NULL,1,'2026-06-03 14:30:36','2026-06-03 14:30:36'),(76,'20270102002','许安宁',2,2027,NULL,1,'2026-06-03 14:30:36','2026-06-03 14:30:36'),(77,'20270103001','邵文博',3,2027,NULL,1,'2026-06-03 14:30:36','2026-06-03 14:30:36'),(78,'20260101001','张可馨',1,2026,NULL,1,'2026-06-03 19:10:59','2026-06-03 19:10:59'),(79,'20260101002','刘书宁',1,2026,NULL,1,'2026-06-03 19:10:59','2026-06-03 19:10:59'),(80,'20260102001','何知远',2,2026,NULL,1,'2026-06-03 19:10:59','2026-06-03 19:10:59'),(81,'20260102002','孙若彤',2,2026,NULL,1,'2026-06-03 19:10:59','2026-06-03 19:10:59'),(82,'20260103001','郑博文',3,2026,NULL,1,'2026-06-03 19:10:59','2026-06-03 19:10:59');
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_assessment_score`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=246 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_assessment_score`
--

LOCK TABLES `student_assessment_score` WRITE;
/*!40000 ALTER TABLE `student_assessment_score` DISABLE KEYS */;
INSERT INTO `student_assessment_score` VALUES (1,1,1,1,13,'2026-05-30 21:19:12','2026-06-07 09:31:21'),(2,2,1,1,13,'2026-05-30 21:19:12','2026-06-07 15:10:50'),(3,3,1,1,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,4,1,1,13,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(5,5,1,1,11,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(6,6,1,1,14,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(7,7,1,1,10,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(8,8,1,1,12,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(9,1,2,1,8,'2026-05-30 21:19:12','2026-06-07 09:31:21'),(10,2,2,1,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(11,3,2,1,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(12,4,2,1,9,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(13,5,2,1,8,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(14,6,2,1,9,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(15,7,2,1,7,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(16,8,2,1,8,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(17,1,3,1,18,'2026-05-30 21:19:12','2026-06-07 09:31:21'),(18,2,3,1,18,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(19,3,3,1,18,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(20,4,3,1,17,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(21,5,3,1,16,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(22,6,3,1,19,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(23,7,3,1,15,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(24,8,3,1,17,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(25,1,4,1,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(26,2,4,1,12,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(27,3,4,1,14,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(28,4,4,1,13,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(29,5,4,1,11,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(30,6,4,1,14,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(31,7,4,1,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(32,8,4,1,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(33,1,5,1,8,'2026-05-30 21:19:12','2026-06-07 09:31:21'),(34,2,5,1,8,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(35,3,5,1,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(36,4,5,1,9,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(37,5,5,1,8,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(38,6,5,1,9,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(39,7,5,1,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(40,8,5,1,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(41,1,6,1,13,'2026-05-30 21:19:12','2026-06-07 09:31:21'),(42,2,6,1,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(43,3,6,1,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(44,4,6,1,13,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(45,5,6,1,11,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(46,6,6,1,14,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(47,7,6,1,10,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(48,8,6,1,12,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(49,1,7,1,18,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(50,2,7,1,18,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(51,3,7,1,17,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(52,4,7,1,17,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(53,5,7,1,16,'2026-05-30 21:19:12','2026-06-07 09:31:19'),(54,6,7,1,19,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(55,7,7,1,15,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(56,8,7,1,17,'2026-05-30 21:19:12','2026-06-07 09:31:20'),(57,3,8,2,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(58,4,8,2,11,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(59,5,8,2,14,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(60,6,8,2,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(61,7,8,2,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(62,8,8,2,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(63,9,8,2,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(64,10,8,2,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(65,3,9,2,18,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(66,4,9,2,15,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(67,5,9,2,19,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(68,6,9,2,14,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(69,7,9,2,16,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(70,8,9,2,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(71,9,9,2,17,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(72,10,9,2,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(73,3,10,2,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(74,4,10,2,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(75,5,10,2,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(76,6,10,2,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(77,7,10,2,11,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(78,8,10,2,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(79,9,10,2,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(80,10,10,2,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(81,3,11,2,14,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(82,4,11,2,11,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(83,5,11,2,15,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(84,6,11,2,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(85,7,11,2,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(86,8,11,2,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(87,9,11,2,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(88,10,11,2,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(89,3,12,2,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(90,4,12,2,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(91,5,12,2,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(92,6,12,2,6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(93,7,12,2,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(94,8,12,2,5,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(95,9,12,2,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(96,10,12,2,6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(97,3,13,2,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(98,4,13,2,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(99,5,13,2,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(100,6,13,2,6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(101,7,13,2,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(102,8,13,2,5,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(103,9,13,2,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(104,10,13,2,6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(105,1,14,3,14,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(106,2,14,3,11,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(107,3,14,3,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(108,4,14,3,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(109,5,14,3,15,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(110,6,14,3,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(111,1,15,3,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(112,2,15,3,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(113,3,15,3,14,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(114,4,15,3,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(115,5,15,3,15,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(116,6,15,3,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(117,1,16,3,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(118,2,16,3,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(119,3,16,3,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(120,4,16,3,6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(121,5,16,3,10,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(122,6,16,3,5,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(123,1,17,3,18,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(124,2,17,3,15,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(125,3,17,3,17,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(126,4,17,3,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(127,5,17,3,19,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(128,6,17,3,12,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(129,1,18,3,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(130,2,18,3,7,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(131,3,18,3,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(132,4,18,3,6,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(133,5,18,3,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(134,6,18,3,5,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(135,1,19,3,13,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(136,2,19,3,11,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(137,3,19,3,14,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(138,4,19,3,9,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(139,5,19,3,15,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(140,6,19,3,8,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(141,51,1,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(142,51,2,1,7,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(143,51,3,1,14,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(144,51,4,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(145,51,5,1,7,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(146,51,6,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(147,51,7,1,14,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(148,52,1,1,13,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(149,52,2,1,8,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(150,52,3,1,18,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(151,52,4,1,13,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(152,52,5,1,8,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(153,52,6,1,13,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(154,52,7,1,18,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(155,53,1,1,12,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(156,53,2,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(157,53,3,1,17,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(158,53,4,1,12,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(159,53,5,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(160,53,6,1,12,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(161,53,7,1,17,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(162,54,1,1,11,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(163,54,2,1,8,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(164,54,3,1,16,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(165,54,4,1,11,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(166,54,5,1,8,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(167,54,6,1,11,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(168,54,7,1,16,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(169,55,1,1,14,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(170,55,2,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(171,55,3,1,19,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(172,55,4,1,14,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(173,55,5,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(174,55,6,1,14,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(175,55,7,1,19,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(176,56,1,1,10,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(177,56,2,1,7,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(178,56,3,1,15,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(179,56,4,1,10,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(180,56,5,1,7,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(181,56,6,1,10,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(182,56,7,1,15,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(183,57,1,1,12,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(184,57,2,1,8,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(185,57,3,1,17,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(186,57,4,1,12,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(187,57,5,1,8,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(188,57,6,1,12,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(189,57,7,1,17,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(190,58,1,1,13,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(191,58,2,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(192,58,3,1,18,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(193,58,4,1,13,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(194,58,5,1,9,'2026-06-07 09:31:20','2026-06-07 09:31:20'),(195,58,6,1,13,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(196,58,7,1,18,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(197,59,1,1,11,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(198,59,2,1,8,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(199,59,3,1,16,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(200,59,4,1,11,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(201,59,5,1,8,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(202,59,6,1,11,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(203,59,7,1,16,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(204,60,1,1,12,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(205,60,2,1,8,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(206,60,3,1,17,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(207,60,4,1,12,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(208,60,5,1,8,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(209,60,6,1,12,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(210,60,7,1,17,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(211,9,1,1,14,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(212,9,2,1,9,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(213,9,3,1,19,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(214,9,4,1,14,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(215,9,5,1,9,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(216,9,6,1,14,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(217,9,7,1,19,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(218,10,1,1,10,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(219,10,2,1,7,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(220,10,3,1,15,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(221,10,4,1,10,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(222,10,5,1,7,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(223,10,6,1,10,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(224,10,7,1,15,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(225,11,1,1,11,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(226,11,2,1,8,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(227,11,3,1,16,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(228,11,4,1,11,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(229,11,5,1,8,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(230,11,6,1,11,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(231,11,7,1,16,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(232,12,1,1,12,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(233,12,2,1,9,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(234,12,3,1,17,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(235,12,4,1,12,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(236,12,5,1,9,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(237,12,6,1,12,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(238,12,7,1,17,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(239,21,1,1,14,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(240,21,2,1,9,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(241,21,3,1,19,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(242,21,4,1,14,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(243,21,5,1,9,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(244,21,6,1,14,'2026-06-07 09:31:21','2026-06-07 09:31:21'),(245,21,7,1,19,'2026-06-07 09:31:21','2026-06-07 09:31:21');
/*!40000 ALTER TABLE `student_assessment_score` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_class`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=205 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_class`
--

LOCK TABLES `student_class` WRITE;
/*!40000 ALTER TABLE `student_class` DISABLE KEYS */;
INSERT INTO `student_class` VALUES (2,2,1,'2026-05-30 21:19:12'),(4,4,1,'2026-05-30 21:19:12'),(5,5,1,'2026-05-30 21:19:12'),(6,6,1,'2026-05-30 21:19:12'),(7,7,1,'2026-05-30 21:19:12'),(8,8,1,'2026-05-30 21:19:12'),(9,3,2,'2026-05-30 21:19:12'),(10,4,2,'2026-05-30 21:19:12'),(11,5,2,'2026-05-30 21:19:12'),(12,6,2,'2026-05-30 21:19:12'),(13,7,2,'2026-05-30 21:19:12'),(14,8,2,'2026-05-30 21:19:12'),(15,9,2,'2026-05-30 21:19:12'),(16,10,2,'2026-05-30 21:19:12'),(17,1,3,'2026-05-30 21:19:12'),(18,2,3,'2026-05-30 21:19:12'),(19,3,3,'2026-05-30 21:19:12'),(20,4,3,'2026-05-30 21:19:12'),(21,5,3,'2026-05-30 21:19:12'),(22,6,3,'2026-05-30 21:19:12'),(23,51,1,'2026-05-30 21:19:44'),(24,52,1,'2026-05-30 21:19:44'),(25,53,1,'2026-05-30 21:19:44'),(26,54,1,'2026-05-30 21:19:44'),(27,55,1,'2026-05-30 21:19:44'),(28,56,1,'2026-05-30 21:19:44'),(29,57,1,'2026-05-30 21:19:44'),(30,58,1,'2026-05-30 21:19:44'),(31,59,1,'2026-05-30 21:19:44'),(32,60,1,'2026-05-30 21:19:44'),(33,11,2,'2026-05-30 21:19:44'),(34,12,2,'2026-05-30 21:19:44'),(35,13,2,'2026-05-30 21:19:44'),(36,14,2,'2026-05-30 21:19:44'),(37,15,2,'2026-05-30 21:19:44'),(38,16,2,'2026-05-30 21:19:44'),(39,17,2,'2026-05-30 21:19:44'),(40,18,2,'2026-05-30 21:19:44'),(41,19,2,'2026-05-30 21:19:44'),(42,20,2,'2026-05-30 21:19:44'),(43,51,2,'2026-05-30 21:19:44'),(44,52,2,'2026-05-30 21:19:44'),(45,53,2,'2026-05-30 21:19:44'),(46,54,2,'2026-05-30 21:19:44'),(47,55,2,'2026-05-30 21:19:44'),(48,56,2,'2026-05-30 21:19:44'),(49,57,2,'2026-05-30 21:19:44'),(50,58,2,'2026-05-30 21:19:44'),(51,59,2,'2026-05-30 21:19:44'),(52,60,2,'2026-05-30 21:19:44'),(53,21,3,'2026-05-30 21:19:44'),(54,22,3,'2026-05-30 21:19:44'),(55,23,3,'2026-05-30 21:19:44'),(56,24,3,'2026-05-30 21:19:44'),(57,25,3,'2026-05-30 21:19:44'),(58,26,3,'2026-05-30 21:19:44'),(59,27,3,'2026-05-30 21:19:44'),(60,28,3,'2026-05-30 21:19:44'),(61,29,3,'2026-05-30 21:19:44'),(62,30,3,'2026-05-30 21:19:44'),(63,31,4,'2026-05-30 21:19:44'),(64,32,4,'2026-05-30 21:19:44'),(65,33,4,'2026-05-30 21:19:44'),(66,34,4,'2026-05-30 21:19:44'),(67,35,4,'2026-05-30 21:19:44'),(68,36,4,'2026-05-30 21:19:44'),(69,37,4,'2026-05-30 21:19:44'),(70,38,4,'2026-05-30 21:19:44'),(71,39,4,'2026-05-30 21:19:44'),(72,40,4,'2026-05-30 21:19:44'),(73,41,4,'2026-05-30 21:19:44'),(74,42,4,'2026-05-30 21:19:44'),(75,43,4,'2026-05-30 21:19:44'),(76,44,4,'2026-05-30 21:19:44'),(77,45,4,'2026-05-30 21:19:44'),(78,41,5,'2026-05-30 21:19:44'),(79,42,5,'2026-05-30 21:19:44'),(80,43,5,'2026-05-30 21:19:44'),(81,44,5,'2026-05-30 21:19:44'),(82,45,5,'2026-05-30 21:19:44'),(83,46,5,'2026-05-30 21:19:44'),(84,47,5,'2026-05-30 21:19:44'),(85,48,5,'2026-05-30 21:19:44'),(86,49,5,'2026-05-30 21:19:44'),(87,50,5,'2026-05-30 21:19:44'),(88,51,5,'2026-05-30 21:19:44'),(89,52,5,'2026-05-30 21:19:44'),(90,53,5,'2026-05-30 21:19:44'),(91,54,5,'2026-05-30 21:19:44'),(92,55,5,'2026-05-30 21:19:44'),(93,1,6,'2026-05-30 21:19:44'),(94,2,6,'2026-05-30 21:19:44'),(95,3,6,'2026-05-30 21:19:44'),(96,4,6,'2026-05-30 21:19:44'),(97,5,6,'2026-05-30 21:19:44'),(98,6,6,'2026-05-30 21:19:44'),(99,7,6,'2026-05-30 21:19:44'),(100,8,6,'2026-05-30 21:19:44'),(101,11,6,'2026-05-30 21:19:44'),(102,12,6,'2026-05-30 21:19:44'),(103,13,6,'2026-05-30 21:19:44'),(104,14,6,'2026-05-30 21:19:44'),(105,15,6,'2026-05-30 21:19:44'),(106,16,6,'2026-05-30 21:19:44'),(107,17,6,'2026-05-30 21:19:44'),(108,18,6,'2026-05-30 21:19:44'),(109,19,6,'2026-05-30 21:19:44'),(110,20,6,'2026-05-30 21:19:44'),(111,31,6,'2026-05-30 21:19:44'),(112,32,6,'2026-05-30 21:19:44'),(113,33,6,'2026-05-30 21:19:44'),(114,34,6,'2026-05-30 21:19:44'),(115,35,6,'2026-05-30 21:19:44'),(116,36,6,'2026-05-30 21:19:44'),(117,37,6,'2026-05-30 21:19:44'),(118,38,6,'2026-05-30 21:19:44'),(119,39,6,'2026-05-30 21:19:44'),(120,40,6,'2026-05-30 21:19:44'),(121,21,7,'2026-05-30 21:19:44'),(122,22,7,'2026-05-30 21:19:44'),(123,23,7,'2026-05-30 21:19:44'),(124,24,7,'2026-05-30 21:19:44'),(125,25,7,'2026-05-30 21:19:44'),(126,26,7,'2026-05-30 21:19:44'),(127,27,7,'2026-05-30 21:19:44'),(128,28,7,'2026-05-30 21:19:44'),(129,29,7,'2026-05-30 21:19:44'),(130,30,7,'2026-05-30 21:19:44'),(131,31,7,'2026-05-30 21:19:44'),(132,32,7,'2026-05-30 21:19:44'),(133,33,7,'2026-05-30 21:19:44'),(134,34,7,'2026-05-30 21:19:44'),(135,35,7,'2026-05-30 21:19:44'),(136,36,8,'2026-05-30 21:19:44'),(137,37,8,'2026-05-30 21:19:44'),(138,38,8,'2026-05-30 21:19:44'),(139,39,8,'2026-05-30 21:19:44'),(140,40,8,'2026-05-30 21:19:44'),(141,41,8,'2026-05-30 21:19:44'),(142,42,8,'2026-05-30 21:19:44'),(143,43,8,'2026-05-30 21:19:44'),(144,44,8,'2026-05-30 21:19:44'),(145,45,8,'2026-05-30 21:19:44'),(146,46,8,'2026-05-30 21:19:44'),(147,47,8,'2026-05-30 21:19:44'),(148,48,8,'2026-05-30 21:19:44'),(149,49,8,'2026-05-30 21:19:44'),(150,50,8,'2026-05-30 21:19:44'),(151,1,9,'2026-05-30 21:19:44'),(152,2,9,'2026-05-30 21:19:44'),(153,3,9,'2026-05-30 21:19:44'),(154,4,9,'2026-05-30 21:19:44'),(155,5,9,'2026-05-30 21:19:44'),(156,6,9,'2026-05-30 21:19:44'),(157,7,9,'2026-05-30 21:19:44'),(158,8,9,'2026-05-30 21:19:44'),(159,51,9,'2026-05-30 21:19:44'),(160,52,9,'2026-05-30 21:19:44'),(161,53,9,'2026-05-30 21:19:44'),(162,54,9,'2026-05-30 21:19:44'),(163,55,9,'2026-05-30 21:19:44'),(164,56,9,'2026-05-30 21:19:44'),(165,57,9,'2026-05-30 21:19:44'),(166,58,9,'2026-05-30 21:19:44'),(167,59,9,'2026-05-30 21:19:44'),(168,60,9,'2026-05-30 21:19:44'),(169,11,10,'2026-05-30 21:19:44'),(170,12,10,'2026-05-30 21:19:44'),(171,13,10,'2026-05-30 21:19:44'),(172,14,10,'2026-05-30 21:19:44'),(173,15,10,'2026-05-30 21:19:44'),(174,31,10,'2026-05-30 21:19:44'),(175,32,10,'2026-05-30 21:19:44'),(176,33,10,'2026-05-30 21:19:44'),(177,34,10,'2026-05-30 21:19:44'),(178,35,10,'2026-05-30 21:19:44'),(179,46,10,'2026-05-30 21:19:44'),(180,47,10,'2026-05-30 21:19:44'),(181,48,10,'2026-05-30 21:19:44'),(182,49,10,'2026-05-30 21:19:44'),(183,50,10,'2026-05-30 21:19:44'),(184,51,10,'2026-05-30 21:19:44'),(185,52,10,'2026-05-30 21:19:44'),(186,53,10,'2026-05-30 21:19:44'),(187,54,10,'2026-05-30 21:19:44'),(188,55,10,'2026-05-30 21:19:44'),(189,9,1,'2026-05-31 15:15:07'),(190,10,1,'2026-05-31 15:15:07'),(191,1,2,'2026-05-31 15:15:07'),(192,2,2,'2026-05-31 15:15:07'),(193,7,3,'2026-05-31 15:15:07'),(194,1,1,'2026-06-01 10:40:14'),(195,11,1,'2026-06-01 10:40:14'),(196,21,2,'2026-06-01 10:40:14'),(197,31,3,'2026-06-01 10:40:15'),(198,41,9,'2026-06-01 10:40:15'),(199,12,1,'2026-06-01 10:42:41'),(200,21,1,'2026-06-03 14:31:13'),(201,31,2,'2026-06-03 14:31:13'),(202,41,3,'2026-06-03 14:31:13'),(203,1,4,'2026-06-03 14:31:13'),(204,11,5,'2026-06-03 14:31:13');
/*!40000 ALTER TABLE `student_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_objective_achievement`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=177 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生课程目标达成度中间结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_objective_achievement`
--

LOCK TABLES `student_objective_achievement` WRITE;
/*!40000 ALTER TABLE `student_objective_achievement` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_objective_achievement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_permission`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_permission`
--

LOCK TABLES `sys_permission` WRITE;
/*!40000 ALTER TABLE `sys_permission` DISABLE KEYS */;
INSERT INTO `sys_permission` VALUES (1,'college:manage','学院管理','system',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(2,'major:manage','专业管理','system',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'user:manage','用户管理','system',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'role:assign','角色分配','system',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'dict:manage','字典管理','system',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,'requirement:write','毕业要求编辑','macro',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(7,'matrix:write','支撑矩阵编辑','macro',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(8,'course:import','课程导入','macro',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(9,'class:import','班级学生导入','macro',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(10,'objective:write','课程目标编辑','syllabus',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(11,'weight:write','内部权重编辑','syllabus',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(12,'point:write','考核点编辑','syllabus',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(13,'score:import','成绩导入录入','assessment',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(14,'calc:trigger','达成度计算触发','assessment',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(15,'report:export','报表导出','report',NULL,'2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `sys_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'admin','系统管理员',1,'系统全局配置、用户账号管理','2026-05-30 21:19:12','2026-05-30 21:19:12'),(2,'academic_affairs','教务管理员',1,'培养方案导入、班级学生管理、报表导出','2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'program_director','专业负责人',1,'毕业要求维护、支撑矩阵配置、专业级计算','2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'instructor','课程主讲教师',1,'课程大纲编写、考核点设定、成绩录入、课程级计算','2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'student','学生',1,'查看本人成绩和达成度评价结果','2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_permission`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_permission`
--

LOCK TABLES `sys_role_permission` WRITE;
/*!40000 ALTER TABLE `sys_role_permission` DISABLE KEYS */;
INSERT INTO `sys_role_permission` VALUES (1,1,1,'2026-05-30 21:19:12'),(2,1,2,'2026-05-30 21:19:12'),(3,1,3,'2026-05-30 21:19:12'),(4,1,4,'2026-05-30 21:19:12'),(5,1,5,'2026-05-30 21:19:12'),(6,1,6,'2026-05-30 21:19:12'),(7,1,7,'2026-05-30 21:19:12'),(8,1,8,'2026-05-30 21:19:12'),(9,1,9,'2026-05-30 21:19:12'),(10,1,10,'2026-05-30 21:19:12'),(11,1,11,'2026-05-30 21:19:12'),(12,1,12,'2026-05-30 21:19:12'),(13,1,13,'2026-05-30 21:19:12'),(14,1,14,'2026-05-30 21:19:12'),(15,1,15,'2026-05-30 21:19:12'),(16,2,8,'2026-05-30 21:19:12'),(17,2,9,'2026-05-30 21:19:12'),(18,2,15,'2026-05-30 21:19:12'),(19,3,6,'2026-05-30 21:19:12'),(20,3,7,'2026-05-30 21:19:12'),(21,3,14,'2026-05-30 21:19:12'),(22,3,15,'2026-05-30 21:19:12'),(23,4,10,'2026-05-30 21:19:12'),(24,4,11,'2026-05-30 21:19:12'),(25,4,12,'2026-05-30 21:19:12'),(26,4,13,'2026-05-30 21:19:12'),(27,4,14,'2026-05-30 21:19:12');
/*!40000 ALTER TABLE `sys_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','赵管理员','admin@university.edu.cn','13800000001',1,'2026-05-30 21:19:11','2026-05-30 21:55:16'),(2,'teacher_zhang','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','张教授','zhang@university.edu.cn','13800000002',1,'2026-05-30 21:19:11','2026-05-30 21:55:16'),(3,'teacher_li','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','李副教授','li@university.edu.cn','13800000003',1,'2026-05-30 21:19:11','2026-05-30 21:55:16'),(4,'teacher_wang','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','王讲师','wang@university.edu.cn','13800000004',1,'2026-05-30 21:19:11','2026-05-30 21:55:16'),(5,'director_chen','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','陈主任','chen@university.edu.cn','13800000005',1,'2026-05-30 21:19:11','2026-05-30 21:55:16'),(6,'academic_wu','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','吴老师','wu@university.edu.cn','13800000006',1,'2026-05-30 21:19:11','2026-05-30 21:55:16'),(7,'student_zhou','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','周一帆','zhou@university.edu.cn','13800000007',1,'2026-05-30 21:19:11','2026-05-30 21:55:16'),(8,'student_chen','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','陈思远','chen2@university.edu.cn','13800000008',1,'2026-05-30 21:19:11','2026-05-30 21:55:16'),(9,'teacher_zhao','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','赵讲师','zhao@university.edu.cn','13800000009',1,'2026-05-30 21:19:44','2026-05-30 21:55:16'),(10,'teacher_sun','$2a$10$qu.LU91tlBtajfCGKhWRzuEdxKNUoAv3J1zH5bTWDTUazRYIaWK06','孙副教授','sun@university.edu.cn','13800000010',1,'2026-05-30 21:19:44','2026-05-30 21:55:16');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,1,1,'2026-05-30 21:19:12'),(2,2,4,'2026-05-30 21:19:12'),(3,3,4,'2026-05-30 21:19:12'),(4,4,4,'2026-05-30 21:19:12'),(5,5,3,'2026-05-30 21:19:12'),(6,6,2,'2026-05-30 21:19:12'),(7,7,5,'2026-05-30 21:19:12'),(8,8,5,'2026-05-30 21:19:12'),(9,9,4,'2026-05-30 21:19:44'),(10,10,4,'2026-05-30 21:19:44');
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (1,'score_import_max_rows','500','成绩导入单次最大行数','2026-05-30 21:19:12','2026-05-30 21:19:12'),(2,'score_precision','2','成绩小数保留位数','2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'calc_precision','4','达成度小数保留位数','2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'calc_threshold_pass','0.60','达成度合格阈值','2026-05-30 21:19:12','2026-05-30 21:19:12'),(5,'report_export_timeout','120','报表导出超时时间（秒）','2026-05-30 21:19:12','2026-05-30 21:19:12'),(6,'default_password','123456','新用户默认密码','2026-05-30 21:19:12','2026-05-30 21:19:12');
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

LOCK TABLES `teacher` WRITE;
/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (1,'T2024001','张教授','教授',1,2,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(2,'T2024002','李副教授','副教授',1,3,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'T2024003','王讲师','讲师',1,4,1,'2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'T2024004','赵讲师','讲师',1,9,1,'2026-05-30 21:19:44','2026-05-30 21:19:44'),(5,'T2024005','孙副教授','副教授',1,10,1,'2026-05-30 21:19:44','2026-05-30 21:19:44');
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teaching_class`
--

DROP TABLE IF EXISTS `teaching_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teaching_class` (
  `class_id` bigint NOT NULL AUTO_INCREMENT,
  `class_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教学班编号，业务唯一标识',
  `class_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_id` bigint NOT NULL,
  `term_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `grade_year` int NOT NULL DEFAULT '2022',
  `calc_status` enum('unsubmitted','score_imported','calculating','locked') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unsubmitted' COMMENT '计算状态：unsubmitted=未提交成绩 / score_imported=成绩已导入 / calculating=计算中 / locked=已锁定',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `class_code` (`class_code`),
  UNIQUE KEY `uk_course_term_class` (`course_id`,`term_id`,`class_name`),
  KEY `idx_class_course` (`course_id`),
  KEY `idx_class_term` (`term_id`),
  KEY `idx_class_teacher` (`teacher_id`),
  KEY `idx_tc_calc_status` (`calc_status`,`term_id`),
  KEY `idx_class_grade` (`grade_year`),
  CONSTRAINT `teaching_class_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE RESTRICT,
  CONSTRAINT `teaching_class_ibfk_2` FOREIGN KEY (`term_id`) REFERENCES `academic_term` (`term_id`) ON DELETE RESTRICT,
  CONSTRAINT `teaching_class_ibfk_3` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teaching_class`
--

LOCK TABLES `teaching_class` WRITE;
/*!40000 ALTER TABLE `teaching_class` DISABLE KEYS */;
INSERT INTO `teaching_class` VALUES (1,'TC2024CS01','数据结构2024-2025-1班',1,1,1,2022,'score_imported','2026-05-30 21:19:12','2026-06-07 15:15:06'),(2,'TC2024CS02','操作系统2024-2025-1班',2,1,2,2022,'unsubmitted','2026-05-30 21:19:12','2026-05-30 21:19:12'),(3,'TC2024CS03','计算机网络2024-2025-1班',3,1,3,2022,'unsubmitted','2026-05-30 21:19:12','2026-05-30 21:19:12'),(4,'TC2024CS04','离散数学2024-2025-1班',6,1,4,2022,'unsubmitted','2026-05-30 21:19:44','2026-05-30 21:19:44'),(5,'TC2024CS05','计算机组成原理2024-2025-1班',7,1,5,2022,'unsubmitted','2026-05-30 21:19:44','2026-05-30 21:19:44'),(6,'TC2024CS06','数据库原理2024-2025-1班',8,1,1,2022,'unsubmitted','2026-05-30 21:19:44','2026-05-30 21:19:44'),(7,'TC2024CS07','编译原理2024-2025-1班',9,1,2,2022,'unsubmitted','2026-05-30 21:19:44','2026-05-30 21:19:44'),(8,'TC2024CS08','算法设计与分析2024-2025-1班',10,1,3,2022,'unsubmitted','2026-05-30 21:19:44','2026-05-30 21:19:44'),(9,'TC2024CS09','人工智能导论2024-2025-1班',11,1,4,2022,'unsubmitted','2026-05-30 21:19:44','2026-05-30 21:19:44'),(10,'TC2024CS10','计算机图形学2024-2025-2班',12,2,5,2022,'unsubmitted','2026-05-30 21:19:44','2026-05-30 21:19:44');
/*!40000 ALTER TABLE `teaching_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `temp_import_staging`
--

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

--
-- Dumping data for table `temp_import_staging`
--

LOCK TABLES `temp_import_staging` WRITE;
/*!40000 ALTER TABLE `temp_import_staging` DISABLE KEYS */;
/*!40000 ALTER TABLE `temp_import_staging` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unlock_audit_log`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unlock_audit_log`
--

LOCK TABLES `unlock_audit_log` WRITE;
/*!40000 ALTER TABLE `unlock_audit_log` DISABLE KEYS */;
INSERT INTO `unlock_audit_log` VALUES (1,1,1,5,'分值有错误','2026-06-07 11:21:13'),(2,1,1,5,'录入成绩有误','2026-06-07 15:13:45');
/*!40000 ALTER TABLE `unlock_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_course_calc_progress`
--

DROP TABLE IF EXISTS `v_course_calc_progress`;
/*!50001 DROP VIEW IF EXISTS `v_course_calc_progress`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_course_calc_progress` AS SELECT 
 1 AS `class_id`,
 1 AS `class_name`,
 1 AS `calc_status`,
 1 AS `grade_year`,
 1 AS `course_code`,
 1 AS `course_name`,
 1 AS `teacher_name`,
 1 AS `major_id`,
 1 AS `term_id`,
 1 AS `student_count`,
 1 AS `score_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_major_achievement_dashboard`
--

DROP TABLE IF EXISTS `v_major_achievement_dashboard`;
/*!50001 DROP VIEW IF EXISTS `v_major_achievement_dashboard`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_major_achievement_dashboard` AS SELECT 
 1 AS `major_id`,
 1 AS `grade_year`,
 1 AS `term_id`,
 1 AS `ip_id`,
 1 AS `ip_code`,
 1 AS `ip_description`,
 1 AS `gr_code`,
 1 AS `gr_description`,
 1 AS `final_achievement`,
 1 AS `pass_status`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_score_drilldown`
--

DROP TABLE IF EXISTS `v_score_drilldown`;
/*!50001 DROP VIEW IF EXISTS `v_score_drilldown`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_score_drilldown` AS SELECT 
 1 AS `gr_code`,
 1 AS `gr_desc`,
 1 AS `grade_year`,
 1 AS `ip_code`,
 1 AS `ip_desc`,
 1 AS `course_code`,
 1 AS `course_name`,
 1 AS `objective_code`,
 1 AS `co_description`,
 1 AS `ap_name`,
 1 AS `full_score`,
 1 AS `actual_score`,
 1 AS `student_no`,
 1 AS `student_name`,
 1 AS `class_name`,
 1 AS `term_id`,
 1 AS `macro_weight`,
 1 AS `micro_weight`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_weight_validation`
--

DROP TABLE IF EXISTS `v_weight_validation`;
/*!50001 DROP VIEW IF EXISTS `v_weight_validation`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_weight_validation` AS SELECT 
 1 AS `ip_id`,
 1 AS `ip_code`,
 1 AS `gr_code`,
 1 AS `grade_year`,
 1 AS `major_id`,
 1 AS `major_name`,
 1 AS `support_course_count`,
 1 AS `weight_sum`,
 1 AS `is_valid`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_course_calc_progress`
--

/*!50001 DROP VIEW IF EXISTS `v_course_calc_progress`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `v_course_calc_progress` AS select `tc`.`class_id` AS `class_id`,`tc`.`class_name` AS `class_name`,`tc`.`calc_status` AS `calc_status`,`tc`.`grade_year` AS `grade_year`,`c`.`course_code` AS `course_code`,`c`.`course_name` AS `course_name`,`t`.`teacher_name` AS `teacher_name`,`cm`.`major_id` AS `major_id`,`tc`.`term_id` AS `term_id`,(select count(0) from `student_class` `sc` where (`sc`.`class_id` = `tc`.`class_id`)) AS `student_count`,(select count(0) from `student_assessment_score` `sas` where (`sas`.`class_id` = `tc`.`class_id`)) AS `score_count` from (((`teaching_class` `tc` join `course` `c` on((`c`.`course_id` = `tc`.`course_id`))) join `teacher` `t` on((`t`.`id` = `tc`.`teacher_id`))) join `course_major` `cm` on(((`cm`.`course_id` = `c`.`course_id`) and (`cm`.`grade_year` = `tc`.`grade_year`)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_major_achievement_dashboard`
--

/*!50001 DROP VIEW IF EXISTS `v_major_achievement_dashboard`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `v_major_achievement_dashboard` AS select `mia`.`major_id` AS `major_id`,`mia`.`grade_year` AS `grade_year`,`mia`.`term_id` AS `term_id`,`mia`.`ip_id` AS `ip_id`,`ip`.`ip_code` AS `ip_code`,`ip`.`ip_description` AS `ip_description`,`gr`.`gr_code` AS `gr_code`,`gr`.`gr_description` AS `gr_description`,`mia`.`final_achievement` AS `final_achievement`,(case when (`mia`.`final_achievement` >= 0.60) then '合格' else '不合格' end) AS `pass_status` from ((`major_indicator_achievement` `mia` join `indicator_point` `ip` on((`ip`.`ip_id` = `mia`.`ip_id`))) join `graduation_requirement` `gr` on((`gr`.`gr_id` = `ip`.`gr_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_score_drilldown`
--

/*!50001 DROP VIEW IF EXISTS `v_score_drilldown`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `v_score_drilldown` AS select `gr`.`gr_code` AS `gr_code`,`gr`.`gr_description` AS `gr_desc`,`gr`.`grade_year` AS `grade_year`,`ip`.`ip_code` AS `ip_code`,`ip`.`ip_description` AS `ip_desc`,`c`.`course_code` AS `course_code`,`c`.`course_name` AS `course_name`,`co`.`objective_code` AS `objective_code`,`co`.`co_description` AS `co_description`,`ap`.`ap_name` AS `ap_name`,`ap`.`full_score` AS `full_score`,`sas`.`actual_score` AS `actual_score`,`s`.`student_no` AS `student_no`,`s`.`student_name` AS `student_name`,`tc`.`class_name` AS `class_name`,`tc`.`term_id` AS `term_id`,`cis`.`total_weight` AS `macro_weight`,`oic`.`internal_weight` AS `micro_weight` from ((((((((((`student_assessment_score` `sas` join `assessment_point` `ap` on((`ap`.`ap_id` = `sas`.`ap_id`))) join `course_objective` `co` on((`co`.`co_id` = `ap`.`co_id`))) join `objective_indicator_contribution` `oic` on((`oic`.`co_id` = `co`.`co_id`))) join `indicator_point` `ip` on((`ip`.`ip_id` = `oic`.`ip_id`))) join `graduation_requirement` `gr` on((`gr`.`gr_id` = `ip`.`gr_id`))) join `teaching_class` `tc` on((`tc`.`class_id` = `sas`.`class_id`))) join `course` `c` on((`c`.`course_id` = `tc`.`course_id`))) join `course_major` `cm` on(((`cm`.`course_id` = `c`.`course_id`) and (`cm`.`major_id` = `gr`.`major_id`) and (`cm`.`grade_year` = `gr`.`grade_year`)))) join `course_indicator_support` `cis` on(((`cis`.`course_id` = `c`.`course_id`) and (`cis`.`ip_id` = `ip`.`ip_id`)))) join `student` `s` on((`s`.`student_id` = `sas`.`student_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_weight_validation`
--

/*!50001 DROP VIEW IF EXISTS `v_weight_validation`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `v_weight_validation` AS select `ip`.`ip_id` AS `ip_id`,`ip`.`ip_code` AS `ip_code`,`gr`.`gr_code` AS `gr_code`,`gr`.`grade_year` AS `grade_year`,`m`.`major_id` AS `major_id`,`m`.`major_name` AS `major_name`,count(distinct (case when (`cm`.`cm_id` is not null) then `cis`.`course_id` end)) AS `support_course_count`,coalesce(sum((case when (`cm`.`cm_id` is not null) then `cis`.`total_weight` else 0 end)),0) AS `weight_sum`,(case when (abs((coalesce(sum((case when (`cm`.`cm_id` is not null) then `cis`.`total_weight` else 0 end)),0) - 1.0)) < 0.001) then 'OK' else 'FAIL' end) AS `is_valid` from ((((`indicator_point` `ip` join `graduation_requirement` `gr` on((`gr`.`gr_id` = `ip`.`gr_id`))) join `major` `m` on((`m`.`major_id` = `gr`.`major_id`))) left join `course_indicator_support` `cis` on((`cis`.`ip_id` = `ip`.`ip_id`))) left join `course_major` `cm` on(((`cm`.`course_id` = `cis`.`course_id`) and (`cm`.`major_id` = `gr`.`major_id`) and (`cm`.`grade_year` = `gr`.`grade_year`)))) group by `ip`.`ip_id`,`ip`.`ip_code`,`gr`.`gr_code`,`gr`.`grade_year`,`m`.`major_id`,`m`.`major_name` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-07 15:47:37
