package codedriver.module.tenant.service;

import codedriver.framework.dto.TeamVo;

public interface TeamService {
	
	public void rebuildLeftRightCode();
	/**
	 * 
	* @Time:2020年5月21日
	* @Description: 判断是否已经现有的用户组是否已经建立左右编码，root节点的lft=1,rht=总节点数x2代表已经建立左右编码
	* @return boolean
	 */
	public boolean checkLeftRightCodeIsExists();

	public TeamVo buildRootTeam();

}
