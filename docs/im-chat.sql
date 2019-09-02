/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : im-chat

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2019-09-02 11:47:32
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for im_message
-- ----------------------------
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message` (
  `id` char(32) NOT NULL DEFAULT '',
  `version` bigint(20) unsigned NOT NULL DEFAULT '0',
  `sequence` char(32) NOT NULL DEFAULT '',
  `sender` varchar(32) NOT NULL DEFAULT '',
  `reception` varchar(16) NOT NULL DEFAULT '',
  `receiver` varchar(32) NOT NULL DEFAULT '',
  `type` varchar(16) NOT NULL DEFAULT '',
  `content` text NOT NULL,
  `time` bigint(20) NOT NULL DEFAULT '0',
  `withdraw` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_version` (`version`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for im_user
-- ----------------------------
DROP TABLE IF EXISTS `im_user`;
CREATE TABLE `im_user` (
  `id` varchar(32) NOT NULL DEFAULT '',
  `token` char(32) NOT NULL DEFAULT '',
  `block` bigint(20) NOT NULL DEFAULT '0',
  `pushable` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
