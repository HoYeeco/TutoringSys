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
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `role` enum('STUDENT','TEACHER','ADMIN') NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','管理员','ADMIN','https://img.tuxiangyan.com/zb_users/upload/2023/04/202304061680747618388645.jpg','admin@edu.com','19809906688',1,'2026-03-05 06:13:56','2026-03-29 12:25:26',0),(2,'t2016001','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','王甲','TEACHER','https://tse4.mm.bing.net/th/id/OIP.3Em8aAWh6Z5W7tUyRbs17QAAAA?rs=1&pid=ImgDetMain&o=7&rm=3','t2016001@edu.com','18109906687',1,'2026-03-05 22:39:03','2026-03-29 12:25:26',0),(3,'t2020002','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','张乙','TEACHER','https://tse4.mm.bing.net/th/id/OIP.G6faaxG5oIWySHzUsHTwIQAAAA?rs=1&pid=ImgDetMain&o=7&rm=3','t2020002@edu.com','18209906688',1,'2026-03-05 22:39:03','2026-03-29 12:25:26',0),(4,'s2026001','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','李一凡','STUDENT','https://img.tuxiangyan.com/zb_users/upload/2023/04/202304061680747621391567.jpg','s2026001@edu.com','16109906688',1,'2026-03-05 22:39:03','2026-03-29 12:25:26',0),(5,'s2026002','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','宋双','STUDENT','https://tse2.mm.bing.net/th/id/OIP.rlcgwt3Z_xYBJqNlE1lLiQAAAA?rs=1&pid=ImgDetMain&o=7&rm=3','s2026002@edu.com','16209906688',1,'2026-03-05 22:39:03','2026-03-29 12:25:26',0),(6,'s2026003','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','唐珊','STUDENT','https://tse1.mm.bing.net/th/id/OIP.oyI2AIOb5jrnf80YNWEazgAAAA?rs=1&pid=ImgDetMain&o=7&rm=3','s2026003@edu.com','16309906688',1,'2026-03-05 22:39:03','2026-03-29 12:25:26',0),(11,'t2024003','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','刘丙','TEACHER','https://tse3.mm.bing.net/th/id/OIP.iRUwMzBoxV2U1y_zKl0bSQAAAA?rs=1&pid=ImgDetMain&o=7&rm=3','t2024003@edu.com','18309906688',1,'2026-03-11 15:11:28','2026-03-29 12:25:26',0),(12,'s2026004','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','陈四','STUDENT','https://tse1.mm.bing.net/th/id/OIP.EvboQ51ObvHzNDHFoDjm7gAAAA?rs=1&pid=ImgDetMain&o=7&rm=3','s2026004@edu.com','16409906688',1,'2026-03-11 15:11:28','2026-03-29 12:25:26',0),(13,'s2026005','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','杨伍','STUDENT','https://tse1.mm.bing.net/th/id/OIP.lqIs9cPjxWHM8LpibAfBtwAAAA?rs=1&pid=ImgDetMain&o=7&rm=3','s2026005@edu.com','16509906688',1,'2026-03-11 15:11:28','2026-03-29 12:25:26',0),(14,'s2026006','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','方璐','STUDENT','https://www.keaitupian.cn/cjpic/frombd/0/253/1596532101/3110108931.jpg','s2026006@edu.com','16609906688',1,'2026-03-11 15:11:28','2026-03-29 12:25:26',0),(15,'s2026007','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','吴琪','STUDENT','https://tse3.mm.bing.net/th/id/OIP.2PtVPJ-js2mU5lnzfW_mEAHaHa?rs=1&pid=ImgDetMain&o=7&rm=3','s2026007@edu.com','16709906688',1,'2026-03-11 15:11:28','2026-03-29 12:25:26',0),(16,'s2026008','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','胡捌','STUDENT',NULL,'s2026008@edu.com','16809906688',0,'2026-03-13 16:12:42','2026-03-29 12:25:26',0),(17,'testuser','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','????','STUDENT',NULL,'test@example.com','13800138000',1,'2026-03-27 22:55:08','2026-03-29 12:30:05',1),(18,'teststudent','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','????','STUDENT',NULL,'test@test.com','13900000001',0,'2026-03-28 01:59:40','2026-03-29 12:25:26',1),(19,'admintest','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','Admin Test','ADMIN',NULL,NULL,NULL,1,'2026-03-28 15:45:43','2026-03-29 12:30:08',1),(21,'teststudent2','$2a$10$5ToSMocaMg6IMCl9AyYp.OLZSZw.HX1Ng3sj.oszR9WazG8tKP/X2','Test Student 2','STUDENT',NULL,NULL,NULL,1,'2026-03-28 15:59:37','2026-03-29 12:30:11',1);
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

-- Dump completed on 2026-04-07 18:06:02
