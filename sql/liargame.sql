-- --------------------------------------------------------
-- 호스트:                          127.0.0.1
-- 서버 버전:                        12.3.3-MariaDB - MariaDB Server
-- 서버 OS:                        Win64
-- HeidiSQL 버전:                  12.20.0.7320
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- liargame 데이터베이스 구조 내보내기
CREATE DATABASE IF NOT EXISTS `liargame` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci */;
USE `liargame`;

-- 테이블 liargame.categories 구조 내보내기
CREATE TABLE IF NOT EXISTS `categories` (
  `category_id` int(11) NOT NULL AUTO_INCREMENT,
  `category` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='카테고리';

-- 테이블 데이터 liargame.categories:~2 rows (대략적) 내보내기
INSERT INTO `categories` (`category_id`, `category`) VALUES
	(1, '과일'),
	(2, '동물');

-- 테이블 liargame.keywords 구조 내보내기
CREATE TABLE IF NOT EXISTS `keywords` (
  `keyword_id` int(11) NOT NULL AUTO_INCREMENT,
  `category_id` int(11) NOT NULL,
  `keyword` varchar(50) NOT NULL,
  `difficulty` varchar(50) NOT NULL,
  PRIMARY KEY (`keyword_id`),
  KEY `fk_keywords_category` (`category_id`),
  CONSTRAINT `fk_keywords_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='키워드';

-- 테이블 데이터 liargame.keywords:~8 rows (대략적) 내보내기
INSERT INTO `keywords` (`keyword_id`, `category_id`, `keyword`, `difficulty`) VALUES
	(1, 1, '사과', 'EASY'),
	(2, 1, '포도', 'EASY'),
	(3, 1, '두리안', 'HARD'),
	(4, 1, '한라봉', 'HARD'),
	(5, 2, '강아지', 'EASY'),
	(6, 2, '고양이', 'EASY'),
	(7, 2, '오리너구리', 'HARD'),
	(8, 2, '두더지', 'HARD');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
