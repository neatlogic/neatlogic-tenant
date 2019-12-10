package codedriver.module.tenant.dao.mapper;

import codedriver.framework.file.dto.FileTypeVo;

public interface FileMapper {

	public FileTypeVo getFileTypeConfigByType(String name);

}
