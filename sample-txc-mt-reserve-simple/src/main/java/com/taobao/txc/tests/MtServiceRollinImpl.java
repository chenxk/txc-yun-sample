package com.taobao.txc.tests;

import com.taobao.txc.common.LoggerInit;
import com.taobao.txc.common.LoggerWrap;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

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
	public void rollin(final String txId, final long branchId, final int id,
			final int money) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				jdbcTemplate.update("insert into temp_table (xid,branchid,id,money) values (?, ?, ?, ?)",
						new Object[] { txId, branchId, id, money });
			}
		});
	}

	public boolean commitRollin(final String txId, final long branchId, String udata) {
		logger.info("commitRollin udata is:" + udata + ",branchId:" + branchId);
		try {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					//query temp, if no record, just throw exception and ignore
					Map map = jdbcTemplate.queryForMap("select id, money from temp_table where branchid="
							+ branchId + " and xid='" + txId + "'");

					long id = (long) map.get("id");
					long money = (long) map.get("money");

					//update user data
					jdbcTemplate.update("update account set money=money+" +
							money + " where id=" + id);

					//delete temp
					jdbcTemplate.update("delete from temp_table where branchid=? and xid=?",
							new Object[] {branchId, txId});
				}
			});
		} catch (Exception e) {
			//ignore
		}
		return true;
	}

	public boolean rollbackRollin(final String txId, final long branchId,
			final String udata) {
		logger.info("rollbackRollin udata is:" + udata + ",branchId:" + branchId);
		try {
			jdbcTemplate.update("delete from temp_table where branchid=? and xid=?",
					new Object[] {branchId, txId});
		} catch (EmptyResultDataAccessException e) { //由于预留记录已处理，会跑出此异常，为保证幂等，故直接返回真
			return true;
		}
		return true;
	}

}
