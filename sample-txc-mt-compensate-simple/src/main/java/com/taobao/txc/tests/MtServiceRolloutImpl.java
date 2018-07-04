package com.taobao.txc.tests;

import com.taobao.txc.common.LoggerInit;
import com.taobao.txc.common.LoggerWrap;
import org.springframework.dao.EmptyResultDataAccessException;
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
	public void rollout(final String txId, final long branchId, final int pid,final int num)
	{
		transactionTemplate.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			public void doInTransactionWithoutResult(TransactionStatus status)
			{
				jdbcTemplate.update("insert into temp_stock(xid,branchid,pid,number) values (?,?,?,?)",new Object[] { txId, branchId, pid, num});
				Map map = jdbcTemplate.queryForMap("select  number from stock where pid=" + pid );
				int number = (int) map.get("number");//获取当前库存
				if((number-num) < 0)        //库存不足抛异常
					throw new RuntimeException("product："+pid+" is not adequate in quantity");
				int i = jdbcTemplate.update("update stock set number=number-" + num + " where pid=" + pid );
			}
		});
	}

	public boolean commitRollout(String txId, long branchId, String udata)
	{
		logger.info("commitRollout udata is:" + udata + ",branchId:" + branchId);
		try
		{
			jdbcTemplate.update("delete from temp_stock where branchid=? and xid=?", new Object[] { branchId, txId });
		}
		catch (EmptyResultDataAccessException e)
		{
			//由于预留记录已处理，会跑出此异常，为保证幂等,此异常直接不处理，后面直接返回真
		}
		return true;
	}

	public boolean rollbackRollout(final String txId, final long branchId, final String udata) {
		logger.info("rollbackRollout udata is:" + udata + ",branchId:" + branchId);
		final int pid, num;
		try
		{
			@SuppressWarnings("rawtypes")
			Map map = jdbcTemplate.queryForMap("select pid, number from temp_stock where branchid=" + branchId + " and xid='" + txId + "'");
			pid = (int) map.get("pid");
			num = (int) map.get("number");
		}
		catch (EmptyResultDataAccessException e)
		{ //由于预留记录已处理，会跑出此异常，为保证幂等，故直接返回真
			return true;
		}
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(TransactionStatus status) {
				jdbcTemplate.update("update stock set number=number+" + num + " where pid=" + pid);
				logger.info("update for " + pid + " is done.");
				jdbcTemplate.update("delete from temp_stock where branchid=? and xid=?", new Object[] { branchId, txId });
				logger.info("delete for " + pid + " is done.");
			}
		});
		return true;
	}
}
