-- MySQL dump 10.13  Distrib 8.0.43, for macos15 (x86_64)
--
-- Host: 127.0.0.1    Database: doand21
-- ------------------------------------------------------
-- Server version	8.4.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `answer`
--

DROP TABLE IF EXISTS `answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext,
  `created_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_correct` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `order_index` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `question_id` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK98t55tlk1ywnoun4kcecdtgcy` (`created_by`),
  KEY `FK8frr4bcabmmeyyu60qt7iiblo` (`question_id`),
  KEY `FKql9th04jdm2qjbscmx817hcv1` (`updated_by`),
  CONSTRAINT `FK8frr4bcabmmeyyu60qt7iiblo` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`),
  CONSTRAINT `FK98t55tlk1ywnoun4kcecdtgcy` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKql9th04jdm2qjbscmx817hcv1` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `answer`
--

LOCK TABLES `answer` WRITE;
/*!40000 ALTER TABLE `answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attach_document_class`
--

DROP TABLE IF EXISTS `attach_document_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attach_document_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `link_url` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  `class_notification` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmfbtgeuky577wukry90nodyve` (`class_notification`),
  KEY `FKo8svues1sxu71ytfwe4wohhko` (`created_by`),
  KEY `FKnhp7e7h3q97u94mqets9mhe3e` (`updated_by`),
  CONSTRAINT `FKmfbtgeuky577wukry90nodyve` FOREIGN KEY (`class_notification`) REFERENCES `class_notification` (`id`),
  CONSTRAINT `FKnhp7e7h3q97u94mqets9mhe3e` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKo8svues1sxu71ytfwe4wohhko` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attach_document_class`
--

LOCK TABLES `attach_document_class` WRITE;
/*!40000 ALTER TABLE `attach_document_class` DISABLE KEYS */;
/*!40000 ALTER TABLE `attach_document_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attach_document_lesson`
--

DROP TABLE IF EXISTS `attach_document_lesson`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attach_document_lesson` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `link_url` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `lesson` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmcrp7687h8fqwwx8v094pd499` (`created_by`),
  KEY `FK8glpm8nnb38i7b8t4ybc78ihb` (`lesson`),
  KEY `FKoynlknom96cgdbpli9jgle2kv` (`updated_by`),
  CONSTRAINT `FK8glpm8nnb38i7b8t4ybc78ihb` FOREIGN KEY (`lesson`) REFERENCES `lesson` (`id`),
  CONSTRAINT `FKmcrp7687h8fqwwx8v094pd499` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKoynlknom96cgdbpli9jgle2kv` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attach_document_lesson`
--

LOCK TABLES `attach_document_lesson` WRITE;
/*!40000 ALTER TABLE `attach_document_lesson` DISABLE KEYS */;
/*!40000 ALTER TABLE `attach_document_lesson` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_in` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `status` enum('ABSENT','LATE','PRESENT') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `schedule` bigint DEFAULT NULL,
  `student` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKir82dwygi2dkvw2h1kythowkc` (`created_by`),
  KEY `FKmdavphvqrcyoedyk7f44c7qnp` (`schedule`),
  KEY `FK3jltoo02du3rfe44ataw0vm6p` (`student`),
  KEY `FKpjqf432e66c0dsqttinreoixc` (`updated_by`),
  CONSTRAINT `FK3jltoo02du3rfe44ataw0vm6p` FOREIGN KEY (`student`) REFERENCES `user` (`id`),
  CONSTRAINT `FKir82dwygi2dkvw2h1kythowkc` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKmdavphvqrcyoedyk7f44c7qnp` FOREIGN KEY (`schedule`) REFERENCES `class_schedule` (`id`),
  CONSTRAINT `FKpjqf432e66c0dsqttinreoixc` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `course` bigint DEFAULT NULL,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj247h96n50ncsj24228tk9h2s` (`course`),
  KEY `FKe0jsofq18lasn4rriad72nkjs` (`user`),
  CONSTRAINT `FKe0jsofq18lasn4rriad72nkjs` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `FKj247h96n50ncsj24228tk9h2s` FOREIGN KEY (`course`) REFERENCES `course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_course`
--

DROP TABLE IF EXISTS `category_course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category_course`
--

LOCK TABLES `category_course` WRITE;
/*!40000 ALTER TABLE `category_course` DISABLE KEYS */;
/*!40000 ALTER TABLE `category_course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_post`
--

DROP TABLE IF EXISTS `category_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `name` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category_post`
--

LOCK TABLES `category_post` WRITE;
/*!40000 ALTER TABLE `category_post` DISABLE KEYS */;
/*!40000 ALTER TABLE `category_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `check_init`
--

DROP TABLE IF EXISTS `check_init`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `check_init` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj94wmru8jpmjakbs7yxnh7519` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `check_init`
--

LOCK TABLES `check_init` WRITE;
/*!40000 ALTER TABLE `check_init` DISABLE KEYS */;
INSERT INTO `check_init` VALUES (1,'USER');
/*!40000 ALTER TABLE `check_init` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class`
--

DROP TABLE IF EXISTS `class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` longtext,
  `name` longtext,
  `status` enum('CANCELLED','COMPLETED','ONGOING','PLANNING') DEFAULT NULL,
  `subject` longtext,
  `title` longtext,
  `total_session` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `teacher` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdi3xjaumwkl9to2lcqd5eofh0` (`created_by`),
  KEY `FKqkls6p5e9vnvp6yphr5tnapy7` (`teacher`),
  KEY `FKda1yrt2apm4tkv3nuv1y18eej` (`updated_by`),
  CONSTRAINT `FKda1yrt2apm4tkv3nuv1y18eej` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKdi3xjaumwkl9to2lcqd5eofh0` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKqkls6p5e9vnvp6yphr5tnapy7` FOREIGN KEY (`teacher`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class`
--

LOCK TABLES `class` WRITE;
/*!40000 ALTER TABLE `class` DISABLE KEYS */;
/*!40000 ALTER TABLE `class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_member`
--

DROP TABLE IF EXISTS `class_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `join_date` datetime(6) DEFAULT NULL,
  `role_in_class` enum('CONSULTANT','MANAGER','STUDENT','TEACHER') DEFAULT NULL,
  `status` enum('ACTIVE','DROPPED') DEFAULT NULL,
  `class` bigint DEFAULT NULL,
  `member` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4meveucqe9ma6s7ta2kxv1qeu` (`class`),
  KEY `FKkm2rlgk5yblob4wl3gs7nvteq` (`member`),
  CONSTRAINT `FK4meveucqe9ma6s7ta2kxv1qeu` FOREIGN KEY (`class`) REFERENCES `class` (`id`),
  CONSTRAINT `FKkm2rlgk5yblob4wl3gs7nvteq` FOREIGN KEY (`member`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_member`
--

LOCK TABLES `class_member` WRITE;
/*!40000 ALTER TABLE `class_member` DISABLE KEYS */;
/*!40000 ALTER TABLE `class_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_notification`
--

DROP TABLE IF EXISTS `class_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` longtext,
  `from_date` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `is_pin` bit(1) DEFAULT NULL,
  `to_date` datetime(6) DEFAULT NULL,
  `type_notification` enum('EXAM','EXERCISE','NOTIFICATION') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `class_id` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo0kp7sfoq3iuvxdtllw6o39f8` (`class_id`),
  KEY `FK5rlq0pllsiyvld5cgnirlfd1d` (`created_by`),
  KEY `FKj4y5xpm486eej7t0krimvgvka` (`updated_by`),
  CONSTRAINT `FK5rlq0pllsiyvld5cgnirlfd1d` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKj4y5xpm486eej7t0krimvgvka` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKo0kp7sfoq3iuvxdtllw6o39f8` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_notification`
--

LOCK TABLES `class_notification` WRITE;
/*!40000 ALTER TABLE `class_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `class_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_schedule`
--

DROP TABLE IF EXISTS `class_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `end_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_attendance` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `start_at` datetime(6) DEFAULT NULL,
  `status` enum('ACTIVE','CANCELLED') DEFAULT NULL,
  `title` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  `class` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `room` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo0j7koeq1d20vi989rtsk5r20` (`class`),
  KEY `FKan04ph752seym1dtdr57hoew9` (`created_by`),
  KEY `FK930xhc9xp7b15wctombqcx0us` (`room`),
  KEY `FKto0n58dlkhak7ji2u3b3n94if` (`updated_by`),
  CONSTRAINT `FK930xhc9xp7b15wctombqcx0us` FOREIGN KEY (`room`) REFERENCES `room` (`id`),
  CONSTRAINT `FKan04ph752seym1dtdr57hoew9` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKo0j7koeq1d20vi989rtsk5r20` FOREIGN KEY (`class`) REFERENCES `class` (`id`),
  CONSTRAINT `FKto0n58dlkhak7ji2u3b3n94if` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_schedule`
--

LOCK TABLES `class_schedule` WRITE;
/*!40000 ALTER TABLE `class_schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `class_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultant`
--

DROP TABLE IF EXISTS `consultant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultant` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKp450qffjmg9sdyqhngsoatwdi` (`user`),
  CONSTRAINT `FK9osbwmo4lsfvs1unsea0hhi6v` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultant`
--

LOCK TABLES `consultant` WRITE;
/*!40000 ALTER TABLE `consultant` DISABLE KEYS */;
INSERT INTO `consultant` VALUES (1,2);
/*!40000 ALTER TABLE `consultant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_course` enum('LISTENING','READING','SPEAKING','WRITING') DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` longtext,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `price` bigint DEFAULT NULL,
  `thumbnail_url` longtext,
  `title` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  `author` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5ws6rpvnuht3aldw6uipsma1n` (`author`),
  KEY `FKcs54bsmb6dm2uwqc4jdpqulio` (`created_by`),
  KEY `FKf8ro015lsnj5ff2nob12bftyu` (`updated_by`),
  CONSTRAINT `FK5ws6rpvnuht3aldw6uipsma1n` FOREIGN KEY (`author`) REFERENCES `user` (`id`),
  CONSTRAINT `FKcs54bsmb6dm2uwqc4jdpqulio` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKf8ro015lsnj5ff2nob12bftyu` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollment`
--

DROP TABLE IF EXISTS `enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `completed_at` datetime(6) DEFAULT NULL,
  `enrolled_at` datetime(6) DEFAULT NULL,
  `progress` decimal(38,2) DEFAULT NULL,
  `course` bigint DEFAULT NULL,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf08p2e0d9xwlpfueoli77ldm2` (`course`),
  KEY `FKedfk6hxyu89ukc52wb94ctj8i` (`user`),
  CONSTRAINT `FKedfk6hxyu89ukc52wb94ctj8i` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `FKf08p2e0d9xwlpfueoli77ldm2` FOREIGN KEY (`course`) REFERENCES `course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollment`
--

LOCK TABLES `enrollment` WRITE;
/*!40000 ALTER TABLE `enrollment` DISABLE KEYS */;
/*!40000 ALTER TABLE `enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `explanation_question`
--

DROP TABLE IF EXISTS `explanation_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `explanation_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `explanation_english` longtext,
  `explanation_vietnamese` longtext,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `question_id` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnxrvv9eym0qutlxeg3sxjmaqf` (`question_id`),
  KEY `FKsywns1ucldachc34nkx77laiy` (`created_by`),
  KEY `FKmbwvct45d1nhf1h2844shnuox` (`updated_by`),
  CONSTRAINT `FKeklj55kq0a59i1ofkjrj6mj75` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`),
  CONSTRAINT `FKmbwvct45d1nhf1h2844shnuox` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKsywns1ucldachc34nkx77laiy` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `explanation_question`
--

LOCK TABLES `explanation_question` WRITE;
/*!40000 ALTER TABLE `explanation_question` DISABLE KEYS */;
/*!40000 ALTER TABLE `explanation_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forgot_password`
--

DROP TABLE IF EXISTS `forgot_password`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forgot_password` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_time` datetime(6) NOT NULL,
  `otp` int NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKss96nm4ed1jmllpxib14p1r7v` (`user_id`),
  CONSTRAINT `FK95rqabtnw8wouua80mbixrq4` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forgot_password`
--

LOCK TABLES `forgot_password` WRITE;
/*!40000 ALTER TABLE `forgot_password` DISABLE KEYS */;
/*!40000 ALTER TABLE `forgot_password` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invalidated_token`
--

DROP TABLE IF EXISTS `invalidated_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invalidated_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_time` datetime(6) DEFAULT NULL,
  `token` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invalidated_token`
--

LOCK TABLES `invalidated_token` WRITE;
/*!40000 ALTER TABLE `invalidated_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `invalidated_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lesson`
--

DROP TABLE IF EXISTS `lesson`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lesson` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext,
  `created_at` datetime(6) DEFAULT NULL,
  `duration` double DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `is_preview_able` bit(1) DEFAULT NULL,
  `order_index` int DEFAULT NULL,
  `title` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  `video_url` longtext,
  `course` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb17a13pfigs9wj87ph9ms4wjh` (`course`),
  KEY `FKk0jsiwfotnul5xtjbd2og4j4h` (`created_by`),
  KEY `FKeh3m60rxc70hhicic31glqbg` (`updated_by`),
  CONSTRAINT `FKb17a13pfigs9wj87ph9ms4wjh` FOREIGN KEY (`course`) REFERENCES `course` (`id`),
  CONSTRAINT `FKeh3m60rxc70hhicic31glqbg` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKk0jsiwfotnul5xtjbd2og4j4h` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lesson`
--

LOCK TABLES `lesson` WRITE;
/*!40000 ALTER TABLE `lesson` DISABLE KEYS */;
/*!40000 ALTER TABLE `lesson` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lesson_completion`
--

DROP TABLE IF EXISTS `lesson_completion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lesson_completion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `completed_at` datetime(6) DEFAULT NULL,
  `enrollment` bigint NOT NULL,
  `lesson` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfxfn657pwgpt71g6pdinkxexi` (`enrollment`),
  KEY `FK3cm2hlll3j7fawp428agdlt3p` (`lesson`),
  CONSTRAINT `FK3cm2hlll3j7fawp428agdlt3p` FOREIGN KEY (`lesson`) REFERENCES `lesson` (`id`),
  CONSTRAINT `FKfxfn657pwgpt71g6pdinkxexi` FOREIGN KEY (`enrollment`) REFERENCES `enrollment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lesson_completion`
--

LOCK TABLES `lesson_completion` WRITE;
/*!40000 ALTER TABLE `lesson_completion` DISABLE KEYS */;
/*!40000 ALTER TABLE `lesson_completion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `manager`
--

DROP TABLE IF EXISTS `manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manager` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK94eaojrxlydes7olioc80iofh` (`user`),
  CONSTRAINT `FKqy7p7yy4gqre8dywrxuaijqf4` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manager`
--

LOCK TABLES `manager` WRITE;
/*!40000 ALTER TABLE `manager` DISABLE KEYS */;
INSERT INTO `manager` VALUES (1,1);
/*!40000 ALTER TABLE `manager` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `noti_type` enum('ADD_TO_CLASS','NEW_COURSE','NEW_QUIZ_IN_CLASS','UPDATE_IN_CLASS') NOT NULL,
  `object_id` bigint DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_receive`
--

DROP TABLE IF EXISTS `notification_receive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_receive` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_read` bit(1) NOT NULL,
  `notification_id` bigint DEFAULT NULL,
  `receiver_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrum3erujtx745jm5yy4lku86w` (`notification_id`),
  KEY `FKha7ai1a24tj9swsjcxh0djmir` (`receiver_id`),
  CONSTRAINT `FKha7ai1a24tj9swsjcxh0djmir` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKrum3erujtx745jm5yy4lku86w` FOREIGN KEY (`notification_id`) REFERENCES `notification` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_receive`
--

LOCK TABLES `notification_receive` WRITE;
/*!40000 ALTER TABLE `notification_receive` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_receive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_detail`
--

DROP TABLE IF EXISTS `order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price_at_purchase` bigint DEFAULT NULL,
  `course` bigint DEFAULT NULL,
  `orders` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbei52wyppfkfw3kxakntb27y9` (`course`),
  KEY `FKgn4buybec6yic8a026fnk27g8` (`orders`),
  CONSTRAINT `FKbei52wyppfkfw3kxakntb27y9` FOREIGN KEY (`course`) REFERENCES `course` (`id`),
  CONSTRAINT `FKgn4buybec6yic8a026fnk27g8` FOREIGN KEY (`orders`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_detail`
--

LOCK TABLES `order_detail` WRITE;
/*!40000 ALTER TABLE `order_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `payment_method` enum('MOMO','VN_PAY','ZALO_PAY') DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','FAILED','PENDING') DEFAULT NULL,
  `total_amount` bigint DEFAULT NULL,
  `transaction_code` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmh56yymll54s83noc1v5dt535` (`user`),
  CONSTRAINT `FKmh56yymll54s83noc1v5dt535` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_post` enum('EVENT','EXAM_EXPERIENCE','GRAMMAR_AND_VOCABULARY','TIPS') NOT NULL,
  `content` longtext,
  `created_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `theme_url` longtext,
  `title` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  `author` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt7refuxfvwatrlcwobvc634fc` (`author`),
  CONSTRAINT `FKt7refuxfvwatrlcwobvc634fc` FOREIGN KEY (`author`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--

LOCK TABLES `post` WRITE;
/*!40000 ALTER TABLE `post` DISABLE KEYS */;
/*!40000 ALTER TABLE `post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext,
  `create_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `update_at` datetime(6) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `question_bank_id` bigint DEFAULT NULL,
  `range_topic` bigint DEFAULT NULL,
  `score_scale` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd3nt4mmki3b3mar44k5dj9ppx` (`create_by`),
  KEY `FKejbwnygbsv82ocl8dq6o2k6yq` (`question_bank_id`),
  KEY `FK5rdydr4heto6fal8ol7x19v09` (`range_topic`),
  KEY `FK7y9kiyax77vxi1oyswukjrts5` (`score_scale`),
  KEY `FKf4jasjuftaqc5bq8ymxxbnasv` (`update_by`),
  CONSTRAINT `FK5rdydr4heto6fal8ol7x19v09` FOREIGN KEY (`range_topic`) REFERENCES `range_topic` (`id`),
  CONSTRAINT `FK7y9kiyax77vxi1oyswukjrts5` FOREIGN KEY (`score_scale`) REFERENCES `score_scale` (`id`),
  CONSTRAINT `FKd3nt4mmki3b3mar44k5dj9ppx` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKejbwnygbsv82ocl8dq6o2k6yq` FOREIGN KEY (`question_bank_id`) REFERENCES `question_bank` (`id`),
  CONSTRAINT `FKf4jasjuftaqc5bq8ymxxbnasv` FOREIGN KEY (`update_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question`
--

LOCK TABLES `question` WRITE;
/*!40000 ALTER TABLE `question` DISABLE KEYS */;
/*!40000 ALTER TABLE `question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_bank`
--

DROP TABLE IF EXISTS `question_bank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_bank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `link_url` varchar(255) DEFAULT NULL,
  `title` longtext,
  `update_at` datetime(6) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK24tfkm7r7bggr0w3v9fiavld5` (`create_by`),
  KEY `FKygcjbgxislcd03xesqfueyoc` (`update_by`),
  CONSTRAINT `FK24tfkm7r7bggr0w3v9fiavld5` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKygcjbgxislcd03xesqfueyoc` FOREIGN KEY (`update_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_bank`
--

LOCK TABLES `question_bank` WRITE;
/*!40000 ALTER TABLE `question_bank` DISABLE KEYS */;
/*!40000 ALTER TABLE `question_bank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_quiz`
--

DROP TABLE IF EXISTS `question_quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_index` bigint DEFAULT NULL,
  `question` bigint DEFAULT NULL,
  `quiz` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK449488prcqrvi8dx5v93nm18a` (`question`),
  KEY `FKj89mu6m9p4jx32ob7v1wu6hqb` (`quiz`),
  CONSTRAINT `FK449488prcqrvi8dx5v93nm18a` FOREIGN KEY (`question`) REFERENCES `question` (`id`),
  CONSTRAINT `FKj89mu6m9p4jx32ob7v1wu6hqb` FOREIGN KEY (`quiz`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_quiz`
--

LOCK TABLES `question_quiz` WRITE;
/*!40000 ALTER TABLE `question_quiz` DISABLE KEYS */;
/*!40000 ALTER TABLE `question_quiz` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz`
--

DROP TABLE IF EXISTS `quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) DEFAULT NULL,
  `description` longtext,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `is_student_created` bit(1) DEFAULT NULL,
  `status` enum('OWNER','PRIVATE','PUBLIC') DEFAULT NULL,
  `title` longtext,
  `total_questions` bigint DEFAULT NULL,
  `update_at` datetime(6) DEFAULT NULL,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsyt7jj89k9dkhsx4eenbc1eh8` (`create_by`),
  KEY `FKg2vxm1ffjqimba9hv430m35u9` (`update_by`),
  CONSTRAINT `FKg2vxm1ffjqimba9hv430m35u9` FOREIGN KEY (`update_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKsyt7jj89k9dkhsx4eenbc1eh8` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz`
--

LOCK TABLES `quiz` WRITE;
/*!40000 ALTER TABLE `quiz` DISABLE KEYS */;
/*!40000 ALTER TABLE `quiz` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `range_topic`
--

DROP TABLE IF EXISTS `range_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `range_topic` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext,
  `created_at` datetime(6) DEFAULT NULL,
  `description` longtext,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `vietnamese` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `range_topic`
--

LOCK TABLES `range_topic` WRITE;
/*!40000 ALTER TABLE `range_topic` DISABLE KEYS */;
INSERT INTO `range_topic` VALUES (1,'VTVF',NULL,'Verb Tense / Verb Forms',_binary '',_binary '\0','2026-03-28 02:15:40.065000','Thì động từ / Dạng động từ'),(2,'SVA',NULL,'Subject – Verb Agreement',_binary '',_binary '\0','2026-03-28 02:15:40.080000','Sự hòa hợp giữa chủ ngữ và động từ'),(3,'NOUN',NULL,'Nouns',_binary '',_binary '\0','2026-03-28 02:15:40.087000','Danh từ'),(4,'PN',NULL,'Pronouns',_binary '',_binary '\0','2026-03-28 02:15:40.091000','Đại từ'),(5,'AA',NULL,'Adjectives & Adverbs',_binary '',_binary '\0','2026-03-28 02:15:40.097000','Tính từ và trạng từ'),(6,'AD',NULL,'Articles & Determiners',_binary '',_binary '\0','2026-03-28 02:15:40.103000','Mạo từ và từ hạn định'),(7,'PP',NULL,'Prepositions',_binary '',_binary '\0','2026-03-28 02:15:40.109000','Giới từ'),(8,'CC',NULL,'Conjunctions / Connectors',_binary '',_binary '\0','2026-03-28 02:15:40.115000','Liên từ và từ nối'),(9,'COMP',NULL,'Comparisons',_binary '',_binary '\0','2026-03-28 02:15:40.120000','Cấu trúc so sánh'),(10,'CS',NULL,'Conditional Sentences',_binary '',_binary '\0','2026-03-28 02:15:40.124000','Câu điều kiện'),(11,'RCEC',NULL,'Relative Clauses & Embedded Clauses',_binary '',_binary '\0','2026-03-28 02:15:40.131000','Mệnh đề quan hệ, mệnh đề danh ngữ'),(12,'PV',NULL,'Passive Voice',_binary '',_binary '\0','2026-03-28 02:15:40.135000','Câu bị động'),(13,'GI',NULL,'Gerunds & Infinitives',_binary '',_binary '\0','2026-03-28 02:15:40.138000','Danh động từ (V-ing) và động từ nguyên mẫu (to V)'),(14,'WF',NULL,'Word Forms',_binary '',_binary '\0','2026-03-28 02:15:40.141000','Chọn dạng đúng của từ loại');
/*!40000 ALTER TABLE `range_topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `name` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` longtext,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `name` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */;
/*!40000 ALTER TABLE `room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `score_scale`
--

DROP TABLE IF EXISTS `score_scale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `score_scale` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `from_score` int DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `title` longtext,
  `to_score` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `score_scale`
--

LOCK TABLES `score_scale` WRITE;
/*!40000 ALTER TABLE `score_scale` DISABLE KEYS */;
INSERT INTO `score_scale` VALUES (1,NULL,0,_binary '',_binary '\0','Muc 1',5,'2026-03-28 02:15:40.148000'),(2,NULL,6,_binary '',_binary '\0','Muc 2',10,'2026-03-28 02:15:40.150000'),(3,NULL,11,_binary '',_binary '\0','Muc 3',15,'2026-03-28 02:15:40.153000'),(4,NULL,16,_binary '',_binary '\0','Muc 4',20,'2026-03-28 02:15:40.156000'),(5,NULL,21,_binary '',_binary '\0','Muc 5',25,'2026-03-28 02:15:40.161000');
/*!40000 ALTER TABLE `score_scale` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shared_quiz`
--

DROP TABLE IF EXISTS `shared_quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shared_quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `end_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `start_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `class_id` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `quiz_id` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgrr8tos4lwtf39tlq3116ck6o` (`class_id`),
  KEY `FKkhy766hhd4oc3dnnufl2bcvkt` (`created_by`),
  KEY `FKqeb47gtxfo9x6dgmwca41nn6q` (`quiz_id`),
  KEY `FK7a93j3hxmybqa6joc732hp4mu` (`updated_by`),
  CONSTRAINT `FK7a93j3hxmybqa6joc732hp4mu` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKgrr8tos4lwtf39tlq3116ck6o` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`),
  CONSTRAINT `FKkhy766hhd4oc3dnnufl2bcvkt` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `FKqeb47gtxfo9x6dgmwca41nn6q` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shared_quiz`
--

LOCK TABLES `shared_quiz` WRITE;
/*!40000 ALTER TABLE `shared_quiz` DISABLE KEYS */;
/*!40000 ALTER TABLE `shared_quiz` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `education` longtext,
  `major` longtext,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKohpjoxdtxxdecqhsryxie9fo3` (`user`),
  CONSTRAINT `FK78n3830yjc550flubbt9mt4c2` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,'Dai hoc 1','Chuyen nganh 1',4);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_answer`
--

DROP TABLE IF EXISTS `student_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_correct` bit(1) DEFAULT NULL,
  `answer` bigint DEFAULT NULL,
  `question` bigint DEFAULT NULL,
  `student_quiz` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4uo1uiqgys9myt1di3e9uhjuh` (`answer`),
  KEY `FKj31q5fwk78cm6jg6ydu958bim` (`question`),
  KEY `FKbo2f6mt6dbuse20kxs4idyusv` (`student_quiz`),
  CONSTRAINT `FK4uo1uiqgys9myt1di3e9uhjuh` FOREIGN KEY (`answer`) REFERENCES `answer` (`id`),
  CONSTRAINT `FKbo2f6mt6dbuse20kxs4idyusv` FOREIGN KEY (`student_quiz`) REFERENCES `student_quiz` (`id`),
  CONSTRAINT `FKj31q5fwk78cm6jg6ydu958bim` FOREIGN KEY (`question`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_answer`
--

LOCK TABLES `student_answer` WRITE;
/*!40000 ALTER TABLE `student_answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_quiz`
--

DROP TABLE IF EXISTS `student_quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `des` varchar(255) DEFAULT NULL,
  `end_at` datetime(6) DEFAULT NULL,
  `score` decimal(38,2) DEFAULT NULL,
  `start_at` datetime(6) DEFAULT NULL,
  `clazz` bigint DEFAULT NULL,
  `quiz` bigint DEFAULT NULL,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo8r8oy07ycgucgwnxr2jxt5tw` (`clazz`),
  KEY `FKfswjsheob96r5quwj1p3326gx` (`quiz`),
  KEY `FKbk1nen9nvvn192m1qjfvkttaq` (`user`),
  CONSTRAINT `FKbk1nen9nvvn192m1qjfvkttaq` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  CONSTRAINT `FKfswjsheob96r5quwj1p3326gx` FOREIGN KEY (`quiz`) REFERENCES `quiz` (`id`),
  CONSTRAINT `FKo8r8oy07ycgucgwnxr2jxt5tw` FOREIGN KEY (`clazz`) REFERENCES `class` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_quiz`
--

LOCK TABLES `student_quiz` WRITE;
/*!40000 ALTER TABLE `student_quiz` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_quiz` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `submit_excercise_in_noti`
--

DROP TABLE IF EXISTS `submit_excercise_in_noti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submit_excercise_in_noti` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `link_url` longtext,
  `updated_at` datetime(6) DEFAULT NULL,
  `class_notification` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1m3fky0w9yhnqx07705lvaiwc` (`class_notification`),
  KEY `FKchacgm90e07ju13yr3usllnpp` (`created_by`),
  CONSTRAINT `FK1m3fky0w9yhnqx07705lvaiwc` FOREIGN KEY (`class_notification`) REFERENCES `class_notification` (`id`),
  CONSTRAINT `FKchacgm90e07ju13yr3usllnpp` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `submit_excercise_in_noti`
--

LOCK TABLES `submit_excercise_in_noti` WRITE;
/*!40000 ALTER TABLE `submit_excercise_in_noti` DISABLE KEYS */;
/*!40000 ALTER TABLE `submit_excercise_in_noti` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `degree` longtext,
  `education` longtext,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5fxn5quvreh3531rf8722whiw` (`user`),
  CONSTRAINT `FK7hh2nbg41t49rilna3meynk0x` FOREIGN KEY (`user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

LOCK TABLES `teacher` WRITE;
/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (1,'Bang cap 1','dai hoc 1',3);
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_shift`
--

DROP TABLE IF EXISTS `teacher_shift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_shift` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_at` datetime(6) DEFAULT NULL,
  `shift_type` enum('CONSULTING','OFFICE_HOURS','TEACHING') DEFAULT NULL,
  `start_at` datetime(6) DEFAULT NULL,
  `teacher` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKec0ikmv2rir5i91jvp48j2rox` (`teacher`),
  CONSTRAINT `FKec0ikmv2rir5i91jvp48j2rox` FOREIGN KEY (`teacher`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_shift`
--

LOCK TABLES `teacher_shift` WRITE;
/*!40000 ALTER TABLE `teacher_shift` DISABLE KEYS */;
/*!40000 ALTER TABLE `teacher_shift` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` longtext,
  `avatar_url` longtext,
  `code` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `dob` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` longtext,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_delete` bit(1) DEFAULT NULL,
  `last_name` longtext,
  `password` longtext NOT NULL,
  `phone` longtext,
  `role` enum('CONSULTANT','MANAGER','STUDENT','TEACHER') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh1vneshxbwkd1ailk02vdy2qu` (`code`),
  UNIQUE KEY `UKob8kqyqqgmefl0aco34akdtpe` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,NULL,NULL,'MAN838734241','2026-03-28 02:14:50.645000',NULL,'manager@gmail.com','User',NULL,_binary '',_binary '\0','Admin','$2a$10$LgR4oHAQtv/ejLFzP5QJJOQWUBt56oCGif2rvbhXE8RpKGOmLFjLa',NULL,'MANAGER',NULL),(2,NULL,NULL,'CON859615481','2026-03-28 02:14:50.805000',NULL,'consultant@gmail.com','User',NULL,_binary '',_binary '\0','Consultant','$2a$10$Ma17vihjCDELdR9sgvW/Ee/sSNaBT8CHKRtnpYFu/5QjoSk9zqIZC',NULL,'CONSULTANT',NULL),(3,NULL,NULL,'TEA738976595','2026-03-28 02:14:51.544000',NULL,'teacher@gmail.com','User',NULL,_binary '',_binary '\0','Teacher','$2a$10$f0woWQeW/jVrXLX6zdGq/eXI44A59hztLjpp3G36.brEG1rbKTTAW',NULL,'TEACHER',NULL),(4,NULL,NULL,'STU458996747','2026-03-28 02:14:51.670000',NULL,'student@gmail.com','User',NULL,_binary '',_binary '\0','Student','$2a$10$SSDW0fCH8WmTyKD9y3bwFu8hyOA3.7LZG6hwUN1a1He3lhyIrERfm',NULL,'STUDENT',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-28  9:21:51
