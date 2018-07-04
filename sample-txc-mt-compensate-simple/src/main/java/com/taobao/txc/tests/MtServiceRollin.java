package com.taobao.txc.tests;

import com.taobao.txc.resourcemanager.mt.MtBranch;


public interface MtServiceRollin {
	@MtBranch(name = "app1.compensate.rollin", commitMethod = "commitRollin", rollbackMethod = "rollbackRollin")
	public void rollin(final String txId, final long branchId, final int oid,final  int uid,final int pid,
					   final int pnum);

	public boolean commitRollin(String txId, long branchId, String udata) ;

	public boolean rollbackRollin(final String txId, final long branchId,
                                  final String udata) ;
}
