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
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint NOT NULL COMMENT '所属作业ID',
  `type` varchar(20) NOT NULL COMMENT '题型：SINGLE/MULTIPLE/JUDGE/SUBJECTIVE',
  `content` text NOT NULL COMMENT '题目内容（富文本）',
  `options` json DEFAULT NULL COMMENT '选项，单选题和多选题使用，JSON数组格式',
  `answer` text COMMENT '客观题标准答案',
  `reference_answer` text COMMENT '主观题参考答案（供AI评分参考）',
  `score` int NOT NULL DEFAULT '0' COMMENT '分值',
  `min_words` int DEFAULT NULL COMMENT '主观题最少字数',
  `max_words` int DEFAULT NULL COMMENT '主观题最多字数',
  `analysis` text COMMENT '题目解析',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '题目在作业中的顺序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_assignment_id` (`assignment_id`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `fk_question_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question`
--

LOCK TABLES `question` WRITE;
/*!40000 ALTER TABLE `question` DISABLE KEYS */;
INSERT INTO `question` VALUES (32,8,'single','<p>下列哪种技术用于定义网页的结构，描述页面中元素的排列和内容（  ）</p>','[\"CSS\", \"JavaScript\", \"HTML\", \"Vue.js\"]','C','',25,0,0,NULL,1,'2026-04-01 14:40:24','2026-04-01 14:40:24'),(33,8,'multiple','<p>下列属于CSS3新特性的有（  ）</p>','[\"弹性盒模型（Flexbox） \", \"媒体查询（Media Query）\", \"渐变（Gradient）\", \"伪元素::before\"]','A,B,C','',15,0,0,NULL,2,'2026-04-01 14:40:24','2026-04-01 14:40:24'),(34,8,'judgment','<p>JavaScript是解释型语言，不需要提前编译，由浏览器或JS引擎逐行解释执行。</p>','[]','true','',20,0,0,NULL,3,'2026-04-01 14:40:24','2026-04-01 14:40:24'),(35,8,'essay','<p>请简述Web前端开发中“盒模型”的概念，并说明标准盒模型与怪异盒模型（IE盒模型）的核心区别与控制方式。</p>','[]','','<p>1. 盒模型概念：Web页面中的所有元素都可以看作一个“盒子”，盒模型由内容区（content）、内边距（padding）、边框（border）、外边距（margin）四部分组成，用于控制元素的尺寸、间距和布局。</p><p>2. 核心区别（关键在于元素总宽度/高度的计算方式）：</p><p>（1）标准盒模型：元素总宽度 = content宽度 + padding + border + margin；元素总高度 = content高度 + padding + border + margin，即padding和border不包含在content尺寸内。</p><p>（2）怪异盒模型（IE盒模型）：元素总宽度 = content宽度（包含padding和border） + margin；元素总高度 = content高度（包含padding和border） + margin，即padding和border包含在content尺寸内。</p><p>3. 控制方式：通过CSS的box-sizing属性控制，box-sizing: content-box（标准盒模型，默认值），box-sizing: border-box（怪异盒模型）。</p>',40,1,100,NULL,4,'2026-04-01 14:40:24','2026-04-01 14:40:24'),(36,4,'single','<p>选A</p>','[\"1\", \"2\", \"3\"]','A','',50,0,0,NULL,1,'2026-04-01 14:42:15','2026-04-01 14:42:15'),(37,4,'judgment','<p>正确</p>','[]','true','',30,0,0,NULL,2,'2026-04-01 14:42:15','2026-04-01 14:42:15'),(38,4,'judgment','<p>正确</p>','[]','true','',20,0,0,NULL,3,'2026-04-01 14:42:15','2026-04-01 14:42:15'),(43,9,'essay','<p>简述Vue的生命周期。</p>','[]','','<ol><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">beforeCreate：实例刚创建，data/methods 未初始化。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">created：实例创建完成，可访问 data，</span><strong style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">DOM 未生成</strong><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">beforeMount：挂载开始前，template 编译完成。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">mounted：</span><strong style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">DOM 挂载完成</strong><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">，可操作 DOM、发起请求。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">beforeUpdate：数据更新，视图未更新。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">updated：视图更新完成。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">beforeDestroy：实例销毁前。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(31, 35, 41);\">destroyed：实例销毁，清理定时器 / 事件。</span></li></ol>',100,1,500,NULL,1,'2026-04-01 21:32:30','2026-04-01 21:32:30'),(44,1,'judgment','<p>正确</p>','[]','true','',55,0,0,NULL,1,'2026-04-03 13:38:06','2026-04-03 13:38:06'),(45,1,'single','<p>选A</p>','[\"1\", \"2\", \"3\", \"4\"]','A','',29,0,0,NULL,2,'2026-04-03 13:38:06','2026-04-03 13:38:06'),(46,1,'multiple','<p>123456<s>选AB</s></p>','[\"1\", \"2\", \"3\", \"4\"]','B,A','',10,0,0,NULL,3,'2026-04-03 13:38:06','2026-04-03 13:38:06'),(47,1,'judgment','<p>正确</p>','[]','true','',6,0,0,NULL,4,'2026-04-03 13:38:06','2026-04-03 13:38:06'),(48,3,'judgment','<p>测试</p>','[]','true','',100,0,0,NULL,1,'2026-04-03 13:46:30','2026-04-03 13:46:30'),(49,7,'single','<p>设 A 为 n 阶可逆矩阵，则下列结论<strong style=\"color: rgb(0, 0, 0); background-color: rgba(0, 0, 0, 0);\">错误</strong>的是（  ）。</p><div class=\"ql-katex\" data-formula=\"\\vec{A}\"><div><span class=\"katex-display\"><span class=\"katex\"><span class=\"katex-mathml\"><math xmlns=\"http://www.w3.org/1998/Math/MathML\" display=\"block\"><semantics><mrow><mover accent=\"true\"><mi>A</mi><mo>⃗</mo></mover></mrow><annotation encoding=\"application/x-tex\">\\vec{A}</annotation></semantics></math></span><span class=\"katex-html\" aria-hidden=\"true\"><span class=\"base\"><span class=\"strut\" style=\"height: 0.9663em;\"></span><span class=\"mord accent\"><span class=\"vlist-t\"><span class=\"vlist-r\"><span class=\"vlist\" style=\"height: 0.9663em;\"><span class=\"\" style=\"top: -3em;\"><span class=\"pstrut\" style=\"height: 3em;\"></span><span class=\"mord mathnormal\">A</span></span><span class=\"\" style=\"top: -3.2523em;\"><span class=\"pstrut\" style=\"height: 3em;\"></span><span class=\"accent-body\" style=\"left: -0.0966em;\"><span class=\"overlay\" style=\"height: 0.714em; width: 0.471em;\"><svg width=\"0.471em\" height=\"0.714em\" style=\"width:0.471em\" viewBox=\"0 0 471 714\" preserveAspectRatio=\"xMinYMin\"><path d=\"M377 20c0-5.333 1.833-10 5.5-14S391 0 397 0c4.667 0 8.667 1.667 12 5\n3.333 2.667 6.667 9 10 19 6.667 24.667 20.333 43.667 41 57 7.333 4.667 11\n10.667 11 18 0 6-1 10-3 12s-6.667 5-14 9c-28.667 14.667-53.667 35.667-75 63\n-1.333 1.333-3.167 3.5-5.5 6.5s-4 4.833-5 5.5c-1 .667-2.5 1.333-4.5 2s-4.333 1\n-7 1c-4.667 0-9.167-1.833-13.5-5.5S337 184 337 178c0-12.667 15.667-32.333 47-59\nH213l-171-1c-8.667-6-13-12.333-13-19 0-4.667 4.333-11.333 13-20h359\nc-16-25.333-24-45-24-59z\"></path></svg></span></span></span></span></span></span></span></span></span></span></span></div></div>','[\"| A |≠ 0\", \"A 的秩为n\", \"齐次线性方程组 Ax = 0 只有零解\", \"A 的行向量组线性相关\"]','D','',20,0,0,NULL,1,'2026-04-03 15:50:40','2026-04-03 15:50:40'),(50,7,'multiple','<p>下列关于向量组线性相关性的说法，正确的有（  ）。</p>','[\"含零向量的向量组一定线性相关\", \"单个非零向量线性无关\", \"线性无关组的部分向量组也线性无关\", \"向量个数大于向量维数时必线性相关\"]','A,B,C,D','',30,0,0,NULL,2,'2026-04-03 15:50:40','2026-04-03 15:50:40'),(51,7,'judgment','<p>若矩阵<span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(0, 0, 0);\">A</span>与<span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(0, 0, 0);\">B</span>相似，则<span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(0, 0, 0);\">A</span>与<span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(0, 0, 0);\">B</span>一定等价。 </p>','[]','true','',10,0,0,NULL,3,'2026-04-03 15:50:40','2026-04-03 15:50:40'),(52,7,'essay','<p>简述<u>矩阵的秩</u>在解线性方程组中的作用。</p>','[]','','<ol><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(0, 0, 0);\">判断线性方程组是否有解：比较系数矩阵的秩与增广矩阵的秩是否相等。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(0, 0, 0);\">判断有解时是唯一解还是无穷多解：秩等于未知量个数则唯一解，小于则无穷多解。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(0, 0, 0);\">确定自由未知量的个数：未知量个数减去系数矩阵的秩，即为自由未知量数量。</span></li><li data-list=\"ordered\"><span class=\"ql-ui\" contenteditable=\"false\"></span><span style=\"background-color: rgba(0, 0, 0, 0); color: rgb(0, 0, 0);\">确定基础解系中解向量的个数：齐次方程组中，基础解系所含向量个数等于自由未知量个数。</span></li></ol><p><br></p>',40,5,100,NULL,4,'2026-04-03 15:50:40','2026-04-03 15:50:40');
/*!40000 ALTER TABLE `question` ENABLE KEYS */;
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
