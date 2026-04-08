-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: tutoringsys_db
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `llm_call_log`
--

DROP TABLE IF EXISTS `llm_call_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `llm_call_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `request_id` varchar(64) NOT NULL COMMENT '请求唯一标识',
  `user_id` bigint DEFAULT NULL COMMENT '调用用户ID（可为空）',
  `model_name` varchar(50) NOT NULL COMMENT '模型名称，如Qwen2.5-Max',
  `prompt_tokens` int DEFAULT '0' COMMENT '输入token数',
  `completion_tokens` int DEFAULT '0' COMMENT '输出token数',
  `total_tokens` int DEFAULT '0' COMMENT '总token数',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1-成功,0-失败',
  `error_message` text COMMENT '错误信息',
  `request_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `response_time` datetime DEFAULT NULL COMMENT '响应时间',
  PRIMARY KEY (`id`),
  KEY `idx_request_id` (`request_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_request_time` (`request_time`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `llm_call_log`
--

LOCK TABLES `llm_call_log` WRITE;
/*!40000 ALTER TABLE `llm_call_log` DISABLE KEYS */;
INSERT INTO `llm_call_log` VALUES (1,'req_20260327_001',4,'Qwen2.5-Max',250,120,370,1,NULL,'2026-03-27 10:15:00','2026-03-27 10:15:02'),(2,'req_20260327_002',5,'Qwen2.5-Max',300,150,450,1,NULL,'2026-03-27 11:20:00','2026-03-27 11:20:03'),(3,'req_20260327_003',NULL,'Qwen2.5-Max',100,80,180,0,'Connection timeout','2026-03-27 12:00:00',NULL),(4,'req_955cbf8479a144de',NULL,'qwen2.5-max',0,0,0,0,'400 Bad Request: \"{\"code\":\"InvalidParameter\",\"message\":\"Model not exist.\",\"request_id\":\"3cae4a7a-0ebc-46ea-b2cc-47f9e8c70236\"}\"','2026-03-28 15:23:47','2026-03-28 15:23:48'),(5,'req_59080a82899f4907',NULL,'qwen-max',0,0,0,1,NULL,'2026-03-28 15:25:13','2026-03-28 15:25:15'),(6,'req_dd5c1eec0913454c',NULL,'qwen-max',0,0,0,0,'Unrecognized field \"finish_reason\" (class com.tutoring.dto.QwenResponse$Output), not marked as ignorable (2 known properties: \"choices\", \"finishReason\"])\n at [Source: (String)\"{\"output\":{\"finish_reason\":\"stop\",\"text\":\"你好！有什么我能帮助你的吗？\"},\"usage\":{\"input_tokens\":19,\"output_tokens\":8,\"prompt_tokens_details\":{\"cached_tokens\":0},\"total_tokens\":27},\"request_id\":\"f85e323f-b815-4bc6-9e0a-4b00a816648d\"}\"; line: 1, column: 29] (through reference chain: com.tutoring.dto.QwenResponse[\"output\"]->com.tutoring.dto.QwenResponse$Output[\"finish_reason\"])','2026-03-28 15:30:40','2026-03-28 15:30:42'),(7,'req_c781835c677d4034',NULL,'qwen-max',0,0,0,0,'Unrecognized field \"prompt_tokens_details\" (class com.tutoring.dto.QwenResponse$Usage), not marked as ignorable (3 known properties: \"output_tokens\", \"total_tokens\", \"input_tokens\"])\n at [Source: (String)\"{\"output\":{\"finish_reason\":\"stop\",\"text\":\"你好！有什么我能帮助你的吗？\"},\"usage\":{\"input_tokens\":19,\"output_tokens\":8,\"prompt_tokens_details\":{\"cached_tokens\":0},\"total_tokens\":27},\"request_id\":\"eed474f3-5293-4263-904f-ada6635b5848\"}\"; line: 1, column: 130] (through reference chain: com.tutoring.dto.QwenResponse[\"usage\"]->com.tutoring.dto.QwenResponse$Usage[\"prompt_tokens_details\"])','2026-03-28 15:48:48','2026-03-28 15:48:50'),(8,'req_5fdccf2ae45c446e',NULL,'qwen-max',19,8,27,1,NULL,'2026-03-28 15:51:44','2026-03-28 15:51:46'),(9,'req_ae778dfb62814992',NULL,'qwen-max',194,139,333,1,NULL,'2026-03-28 15:52:01','2026-03-28 15:52:11'),(10,'req_e11556e0645f4513',NULL,'qwen-max',0,0,0,0,'I/O error on POST request for \"https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation\": Read timed out','2026-03-28 16:07:59','2026-03-28 16:08:29'),(11,'req_7af640eb53774a33',NULL,'qwen-max',19,8,27,1,NULL,'2026-03-28 16:09:35','2026-03-28 16:09:37'),(12,'req_16aa8842e1b04d19',NULL,'qwen-max',178,165,343,1,NULL,'2026-03-28 16:19:08','2026-03-28 16:19:21'),(13,'req_1a86cee05df542f2',NULL,'qwen-max',240,188,428,1,NULL,'2026-03-29 18:41:57','2026-03-29 18:42:09'),(14,'req_7b78cf76faa4417f',NULL,'qwen-max',981,50,1031,1,NULL,'2026-04-02 13:44:24','2026-04-02 13:44:37'),(15,'req_786f3a6b16d24dd5',NULL,'qwen-max',981,50,1031,1,NULL,'2026-04-02 14:17:38','2026-04-02 14:17:50'),(16,'req_186e59e561be4118',NULL,'qwen-max',981,47,1028,1,NULL,'2026-04-02 14:29:09','2026-04-02 14:29:14'),(17,'req_c9759e87549d4989',NULL,'qwen-max',981,52,1033,1,NULL,'2026-04-02 14:36:12','2026-04-02 14:36:17'),(18,'req_fa4d18fc111d4fb6',NULL,'qwen-max',981,52,1033,1,NULL,'2026-04-02 16:53:45','2026-04-02 16:53:52'),(19,'req_2b5e640a7bac43e1',NULL,'qwen-max',981,51,1032,1,NULL,'2026-04-02 17:04:48','2026-04-02 17:04:53'),(20,'req_c4c9bb3c0d9448b7',NULL,'qwen-max',981,50,1031,1,NULL,'2026-04-02 17:05:06','2026-04-02 17:05:10'),(21,'req_7328a0a5bb854a2c',NULL,'qwen-max',981,51,1032,1,NULL,'2026-04-02 17:20:57','2026-04-02 17:21:01'),(22,'req_d701f5e4bfad40eb',NULL,'qwen-max',981,54,1035,1,NULL,'2026-04-02 17:27:50','2026-04-02 17:28:01'),(23,'req_42f57f7b87be41d2',NULL,'qwen-max',1069,45,1114,1,NULL,'2026-04-02 18:35:56','2026-04-02 18:36:01'),(24,'req_a0a2a1f4fb024075',NULL,'qwen-max',1069,41,1110,1,NULL,'2026-04-02 18:36:08','2026-04-02 18:36:14'),(25,'req_5c6066183eb344e9',NULL,'qwen-max',1079,41,1120,1,NULL,'2026-04-02 19:28:34','2026-04-02 19:28:37'),(26,'req_421db34b60d64d81',NULL,'qwen-max',1079,41,1120,1,NULL,'2026-04-02 19:29:10','2026-04-02 19:29:15'),(27,'req_48015f5b53534df6',NULL,'qwen-max',1079,41,1120,1,NULL,'2026-04-02 20:31:22','2026-04-02 20:31:26'),(28,'req_02c2e6831be94016',NULL,'qwen-max',1142,42,1184,1,NULL,'2026-04-02 20:32:37','2026-04-02 20:32:41'),(29,'req_c089178a39774870',NULL,'qwen-max',1659,27,1686,1,NULL,'2026-04-02 20:35:03','2026-04-02 20:35:07'),(30,'req_e38f191c009244b9',NULL,'qwen-max',1659,27,1686,1,NULL,'2026-04-02 20:37:32','2026-04-02 20:37:42'),(31,'req_caa9bc561d1b4f84',NULL,'qwen-max',1659,27,1686,1,NULL,'2026-04-02 20:45:00','2026-04-02 20:45:03'),(32,'req_4a25db0536a74e55',NULL,'qwen-max',1659,27,1686,1,NULL,'2026-04-02 20:57:52','2026-04-02 20:57:55');
/*!40000 ALTER TABLE `llm_call_log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-07 18:06:04
