CREATE TABLE `account` (
  `cardNum` int(11) NOT NULL,
  `name` varchar(11) DEFAULT NULL,
  `balance` int(11) DEFAULT NULL,
  PRIMARY KEY (`cardNum`)
) dbpartition by hash(cardNum);

