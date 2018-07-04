package com.taobao.txc.tests;

import com.taobao.txc.resourcemanager.mt.MtBranch;


public interface MtServiceRollin {
	@MtBranch(name = "app1.reserve.rollin", commitMethod = "commitRollin", rollbackMethod = "rollbackRollin")
	public void rollin(String txId, long branchId, int id, int money);

	public boolean commitRollin(String txId, long branchId, String udata) ;

	public boolean rollbackRollin(final String txId, final long branchId,
                                  final String udata) ;
}
