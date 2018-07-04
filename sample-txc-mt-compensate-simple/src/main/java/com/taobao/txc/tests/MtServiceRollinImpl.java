package com.taobao.txc.tests;

import com.taobao.txc.common.LoggerInit;
import com.taobao.txc.common.LoggerWrap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Date;
public class MtServiceRollinImpl implements MtServiceRollin {
	private JdbcTemplate jdbcTemplate;
	private static final LoggerWrap logger = LoggerInit.logger;
	private TransactionTemplate transactionTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	@Override
	public void rollin(final String txId, final long branchId, final int oid,final  int uid,final int pid,
			final int pnum) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(TransactionStatus status) {
				jdbcTemplate.update(
						"insert into temp_orders (xid,branchid,oid) values (?,?,?)",
						new Object[] { txId, branchId, oid});
				Timestamp tp = new Timestamp(new Date().getTime());
				int ostatus = 1;
				int i = jdbcTemplate.update("insert into orders(oid,uid,pid,pnum,ostatus,otime) values(?,?,?,?,?,?)",new Object[]{oid,uid,pid,pnum,ostatus,tp});
				if (i == 0)
					throw new Error();

			}
		});
	}

	public boolean commitRollin(String txId, long branchId, String udata) {
		logger.info("commitRollin udata is:" + udata + ",branchId:" + branchId);
		try {
			jdbcTemplate.update("delete from temp_orders where branchid=? and xid=?",
					new Object[] { branchId, txId });
		} catch (Exception e) {
			// ignore
		}
		return true;
	}

	public boolean rollbackRollin(final String txId, final long branchId,
			final String udata) {
		logger.info("rollbackRollin udata is:" + udata + ",branchId:" + branchId);
		final long oid;
		try {
			@SuppressWarnings("rawtypes")
			Map map = jdbcTemplate
					.queryForMap("select oid from temp_orders where branchid="
							+ branchId + " and xid='" + txId + "'");
			oid = (long) map.get("oid");

		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(TransactionStatus status) {
				jdbcTemplate.update("delete from orders" + " where oid=" + oid);
				jdbcTemplate.update("delete from temp_orders where branchid=? and xid=?",
						new Object[] { branchId, txId });
			}
		});
		return true;
	}

}
