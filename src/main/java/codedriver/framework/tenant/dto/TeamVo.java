package codedriver.framework.tenant.dto;

import java.util.List;

import codedriver.framework.dto.UserVo;

public class TeamVo {

	private Long id;
	private Integer lft;
	private Integer rht;
	private Integer layer;
	private Integer isOnline;
	private Integer isShow;
	private Long roleId;
	private String roleName;
	private Long timeId;
	private Long parentId;
	private String importId;
	private String idString;
	private String name;
	private String userView;
	private String description;
	//private ChannelVo channelVo;
	private int diff;
	private int childCount;
	private int isHandleChildtask;
	private Integer moduleId ;
	private List<Long> idList;
	private List<Integer> moduleIdList;
	private List<RoleVo> roleList;
	private List<UserVo> userList;
	private Long regionId;
	private Long templateId;//区域模板id,flow_regiontemplate表
	private Integer pageSize;
	private Integer componentId;
	private Long worktimeId ; 
	private Integer searchFlag = 0 ; //0 模糊匹配， 1 全文匹配
	private String parentName;
	private int relateCount;
	private int isDelete ;
	/**
	 * 执行组id
	 * 
	 * @return
	 */
	// private String execTeamID;
	/**
	 * 执行组name
	 */
	// private String execTeamName;

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof TeamVo))
			return false;

		final TeamVo team = (TeamVo) other;
		try {
			if (getId().equals(team.getId()))
				return true;
		} catch (Exception ex) {
			return false;
		}
		return false;
	}

	public Integer getIsShow() {
		return isShow;
	}

	public void setIsShow(Integer isShow) {
		this.isShow = isShow;
	}

	public Integer getComponentId() {
		return componentId;
	}

	public void setComponentId(Integer componentId) {
		this.componentId = componentId;
	}

	public int getChildCount() {
		return childCount;
	}

	public void setChildCount(int childCount) {
		this.childCount = childCount;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getImportId() {
		return importId;
	}

	public void setImportId(String importId) {
		this.importId = importId;
	}

	public Integer getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(Integer isOnline) {
		this.isOnline = isOnline;
	}

	public List<Integer> getModuleIdList() {
		return moduleIdList;
	}

	public void setModuleIdList(List<Integer> moduleIdList) {
		this.moduleIdList = moduleIdList;
	}

	@Override
	public int hashCode() {
		int result = getName().hashCode();
		result = 29 * result + getId().hashCode();
		return result;
	}

	public TeamVo(Long teamId) {
		this.id = teamId;
	}

	public TeamVo() {

	}

	public String getIdString() {
		if (this.id != null) {
			return id.toString();
		} else {
			return "";
		}
	}

	public TeamVo(int left, int right) {
		this.lft = left;
		this.rht = right;
	}

	public TeamVo(int diff, List<Long> idList) {
		this.diff = diff;
		this.idList = idList;
	}

	public String getUserView() {
		return userView;
	}

	public void setUserView(String userView) {
		this.userView = userView;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getLft() {
		return lft;
	}

	public void setLft(Integer lft) {
		this.lft = lft;
	}

	public Integer getRht() {
		return rht;
	}

	public void setRht(Integer rht) {
		this.rht = rht;
	}

	public Integer getLayer() {
		return layer;
	}

	public void setLayer(Integer layer) {
		this.layer = layer;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Long getTimeId() {
		return timeId;
	}

	public void setTimeId(Long timeId) {
		this.timeId = timeId;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

//	public ChannelVo getChannelVo() {
//		return channelVo;
//	}
//
//	public void setChannelVo(ChannelVo channelVo) {
//		this.channelVo = channelVo;
//	}

	public int getDiff() {
		return diff;
	}

	public void setDiff(int diff) {
		this.diff = diff;
	}

	public List<Long> getIdList() {
		return idList;
	}

	public void setIdList(List<Long> idList) {
		this.idList = idList;
	}

	public List<RoleVo> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<RoleVo> roleList) {
		this.roleList = roleList;
	}

	public List<UserVo> getUserList() {
		return userList;
	}

	public void setUserList(List<UserVo> userList) {
		this.userList = userList;
	}

	public int getIsHandleChildtask() {
		return isHandleChildtask;
	}

	public void setIsHandleChildtask(int isHandleChildtask) {
		this.isHandleChildtask = isHandleChildtask;
	}

	public Integer getModuleId() {
		return moduleId;
	}

	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}

	public Long getRegionId() {
		return regionId;
	}

	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public Long getWorktimeId() {
		return worktimeId;
	}

	public void setWorktimeId(Long worktimeId) {
		this.worktimeId = worktimeId;
	}

	public Integer getSearchFlag() {
		return searchFlag;
	}

	public void setSearchFlag(Integer searchFlag) {
		this.searchFlag = searchFlag;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public int getRelateCount() {
		return relateCount;
	}

	public void setRelateCount(int relateCount) {
		this.relateCount = relateCount;
	}

	public int getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(int isDelete) {
		this.isDelete = isDelete;
	}

	
}
