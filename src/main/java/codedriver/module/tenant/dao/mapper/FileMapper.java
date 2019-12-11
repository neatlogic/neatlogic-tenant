package codedriver.module.tenant.dao.mapper;

import codedriver.framework.file.dto.FileTypeVo;
import codedriver.framework.file.dto.FileVo;

public interface FileMapper {

	public FileTypeVo getFileTypeConfigByType(String name);

	public int insertFile(FileVo fileVo);
}
