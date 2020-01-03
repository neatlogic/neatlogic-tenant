package codedriver.module.tenant.dto;

import java.util.List;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.EntityField;

public class MenuVo {
	@EntityField(name = "菜单项id",
			type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "菜单项名称",
			type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "父菜单项id",
			type = ApiParamType.LONG)
	private Long parentId;
	@EntityField(name = "父菜单项名称",
			type = ApiParamType.STRING)
	private String parentName;
	@EntityField(name = "菜单项图标",
			type = ApiParamType.STRING)
	private String icon;
	@EntityField(name = "菜单项超链接",
			type = ApiParamType.STRING)
	private String url;
	@EntityField(name = "排序",
			type = ApiParamType.INTEGER)
	private Integer sort;
	@EntityField(name = "是否激活",
			type = ApiParamType.INTEGER)
	private Integer isActive;
	@EntityField(name = "所属模块",
			type = ApiParamType.STRING)
	private String module;
	@EntityField(name = "描述",
			type = ApiParamType.STRING)
	private String description;
	private int isAuto = 0;
	private String openMode = "tab";
	private List<RoleVo> roleList;
	private List<String> roleNameList;
	private List<MenuVo> childMenuList;

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public List<String> getRoleNameList() {
		return roleNameList;
	}

	public void setRoleNameList(List<String> roleNameList) {
		this.roleNameList = roleNameList;
	}

	public Long getId() {
		return id;
	}

	public MenuVo() {

	}

	public MenuVo(Long _id) {
		this.id = _id;
	}

	public MenuVo(Long _id, Long _parentId, Integer _isActive,List<String> _roleNameList) {
		this.id = _id;
		this.parentId = _parentId;
		this.roleNameList = _roleNameList;
		this.isActive = _isActive != null?_isActive:0;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public int getIsActive() {
		return isActive;
	}

	public void setIsActive(int isActive) {
		this.isActive = isActive;
	}

	public List<RoleVo> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<RoleVo> roleList) {
		this.roleList = roleList;
	}

	public List<MenuVo> getChildMenuList() {
		return childMenuList;
	}

	public void setChildMenuList(List<MenuVo> childMenuList) {
		this.childMenuList = childMenuList;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getIsAuto() {
		return isAuto;
	}

	public void setIsAuto(int isAuto) {
		this.isAuto = isAuto;
	}

	public String getOpenMode() {
		return openMode;
	}

	public void setOpenMode(String openMode) {
		this.openMode = openMode;
	}

}
