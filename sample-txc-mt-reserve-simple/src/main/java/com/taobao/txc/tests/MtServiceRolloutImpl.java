package com.taobao.txc.tests;

import com.taobao.txc.common.LoggerInit;
import com.taobao.txc.common.LoggerWrap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

public class MtServiceRolloutImpl implements MtServiceRollout {
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
	public void rollout(final String txId, final long branchId, final int id,
			final int money) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				// query account , if account.money - reserve_money >= money , update reserve_money.
				int affectedRows = jdbcTemplate.update("update account set reserve_money = reserve_money + "+
								money+" where id = " + id +" and money - reserve_money >=  "+money);
				if (affectedRows == 0) {
					System.out.println("the balance is not enough");
					throw new RuntimeException("the balance is not enough");
				}

				jdbcTemplate.update("insert into temp_table (xid,branchid,id,money) values (?,?,?,?)",
						new Object[] { txId, branchId, id, money });
			}
		});
	}

	public boolean commitRollout(final String txId, final long branchId, String udata) {
		logger.info("commitRollout udata is:" + udata + ",branchId:" + branchId);
		try {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					//query temp, if no record, just throw exception and ignore
					Map map = jdbcTemplate.queryForMap("select id, money from temp_table where branchid="
							+ branchId + " and xid='" + txId + "'");

					long id = (long) map.get("id");
					long money = (long) map.get("money");

					//update  money and reserve_money
					jdbcTemplate.update("update account set money = money - " +
							money + ",reserve_money = reserve_money - "+ money +" where id = " + id);

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

	public boolean rollbackRollout(final String txId, final long branchId,
			final String udata) {
		logger.info("rollbackRollout udata is:" + udata + ",branchId:" + branchId);
		try {
			jdbcTemplate.update("delete from temp_table where branchid=? and xid=?",
					new Object[]{branchId, txId});
		} catch (Exception e) {
			return true;
		}
		return true;
	}
}
