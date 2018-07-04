/* datasource1 */
CREATE TABLE temp_table ( xid char(50) not null, branchid bigint(20) not null,
  id bigint(20) not null, money bigint(20) default null,
  PRIMARY KEY (branchid));

CREATE TABLE account (id bigint(20) not null, money bigint(20) not null,
  PRIMARY KEY (id));

/* datasource2 */
CREATE TABLE temp_table (xid char(50) not null, branchid bigint(20) not null,
  id bigint(20) not null, money bigint(20) DEFAULT null,
  PRIMARY KEY (branchid));

CREATE TABLE account (id bigint(20) not null, money bigint(20) not null,
  PRIMARY KEY (id));