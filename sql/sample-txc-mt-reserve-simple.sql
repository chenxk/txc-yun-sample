-- ----------------------------
--  Table structure for `account`
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` bigint(20) NOT NULL,
  `money` bigint(20) NOT NULL,
  `reserve_money` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


-- ----------------------------
--  Table structure for `temp_table`
-- ----------------------------
DROP TABLE IF EXISTS `temp_table`;
CREATE TABLE `temp_table` (
  `xid` char(50) NOT NULL,
  `branchid` bigint(20) NOT NULL,
  `id` bigint(20) NOT NULL,
  `money` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`branchid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
