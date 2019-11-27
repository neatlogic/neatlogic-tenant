package codedriver.framework.tenant.dto;

import java.util.List;

public class MenuVo {

	private Long id;
	private String name;
	private Long parentId;
	private String parentName;
	private String icon;
	private String url;
	private Integer sort;
	private int isActive;
	private String module ; 
	private String description ;
	private int isAuto = 0;
	private String openMode = "tab";
	private List<RoleVo> roleList;
	private List<String> roleIdList;
	private List<MenuVo> childMenuList; 

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public List<String> getRoleIdList() {
		return roleIdList;
	}

	public void setRoleIdList(List<String> roleIdList) {
		this.roleIdList = roleIdList;
	}

	public Long getId() {
		return id;
	}
	
	public MenuVo() {
		
	}
	
	public MenuVo(Long id ) {
		this.id = id ; 
	}
	
	public MenuVo(Long id,Long parentId) {
		this.id = id;
		this.parentId = parentId;
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

