
CREATE TABLE `orders` (
  `oid` bigint(11) NOT NULL,
  `uid` int(11) default NULL,
  `pid` int(11) default NULL,
  `pnum` int(11) default NULL,
  `ostatus` smallint(6) default NULL,
  `otime` timestamp NULL default NULL,
  PRIMARY KEY  (`oid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


CREATE TABLE `temp_orders` (
  `xid` char(50) NOT NULL,
  `branchid` bigint(20) NOT NULL,
  `oid` bigint(11) NOT NULL,
  PRIMARY KEY  (`branchid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `stock` (
  `pid` int(11) NOT NULL,
  `pname` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `number` int(11) DEFAULT NULL,
  `pmodel` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `temp_stock` (
  `xid` char(50) NOT NULL,
  `branchid` int(20) NOT NULL,
  `pid` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  PRIMARY KEY (`branchid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

