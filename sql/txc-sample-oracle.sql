--oracle建表语句
CREATE TABLE txc_undo_log (
  id number(20) NOT NULL,
  gmt_create date NOT NULL,
  gmt_modified date NOT NULL,
  xid varchar(100) NOT NULL,
  branch_id number(20) NOT NULL,
  rollback_info blob NOT NULL,
  status number(11) NOT NULL,
  server varchar(32) NOT NULL,
  PRIMARY KEY  (id)
);
alter table txc_undo_log add constraint txc_undo_log_unionkey unique(xid, branch_id);

--ids2表
CREATE TABLE "IDS2"
(	"ID" NUMBER NOT NULL ENABLE,
	"VALUE" NUMBER(*,0),
	 PRIMARY KEY ("ID")
);