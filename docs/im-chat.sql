/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : im-chat

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2019-08-27 18:10:02
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for im_message
-- ----------------------------
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message` (
  `id` char(32) NOT NULL DEFAULT '',
  `sender` varchar(32) NOT NULL DEFAULT '',
  `version` bigint(20) unsigned NOT NULL DEFAULT '0',
  `reception` varchar(16) NOT NULL DEFAULT '',
  `receiver` varchar(32) NOT NULL DEFAULT '',
  `sequence` char(32) NOT NULL DEFAULT '',
  `type` varchar(16) NOT NULL DEFAULT '',
  `content` text NOT NULL,
  `time` datetime(3) DEFAULT NULL,
  `withdraw` datetime(3) DEFAULT NULL,
  `delete` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_version` (`sender`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for im_scatter
-- ----------------------------
DROP TABLE IF EXISTS `im_scatter`;
CREATE TABLE `im_scatter` (
  `message_id` char(32) NOT NULL DEFAULT '',
  `receiver` varchar(32) NOT NULL DEFAULT '',
  `version` bigint(20) unsigned NOT NULL DEFAULT '0',
  `read` datetime(3) DEFAULT NULL,
  `delete` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`message_id`,`receiver`),
  UNIQUE KEY `uni_version` (`receiver`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for im_user
-- ----------------------------
DROP TABLE IF EXISTS `im_user`;
CREATE TABLE `im_user` (
  `id` varchar(32) NOT NULL DEFAULT '',
  `token` char(32) NOT NULL DEFAULT '',
  `block` datetime(3) DEFAULT NULL,
  `pushable` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
