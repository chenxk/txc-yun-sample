package com.taobao.txc.tests;

import com.taobao.txc.resourcemanager.mt.MtBranch;

public interface MtServiceRollout {
	@MtBranch(name = "app1.compensate.rollout", commitMethod = "commitRollout", rollbackMethod = "rollbackRollout")
	public void rollout(final String txId, final long branchId, final int pid, final int num);

	public boolean commitRollout(String txId, long branchId, String udata);

	public boolean rollbackRollout(final String txId, final long branchId, final String udata);
}
