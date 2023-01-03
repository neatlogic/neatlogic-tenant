package codedriver.module.tenant.api.notify;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.exception.file.FileExtNotAllowedException;
import codedriver.framework.exception.file.FileNotUploadException;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyMoreThanOneException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@OperationType(type = OperationTypeEnum.UPDATE)
public class ImportNotifyPolicyApi extends PrivateBinaryStreamApiComponentBase {

    private Logger logger = LoggerFactory.getLogger(ImportNotifyPolicyApi.class);

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/import";
    }
    @Override
    public String getName() {
        return "导入通知策略";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({})
    @Description(desc = "导入通知策略")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        //如果没有导入文件, 抛异常
        if (multipartFileMap.isEmpty()) {
            throw new FileNotUploadException();
        }
        byte[] buf = new byte[1024];
        //遍历导入文件
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            MultipartFile multipartFile = entry.getValue();
            //反序列化获取对象
            try (ZipInputStream zipis = new ZipInputStream(multipartFile.getInputStream());
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ZipEntry zipEntry;
                while ((zipEntry = zipis.getNextEntry()) != null) {
                    int len;
                    while ((len = zipis.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    NotifyPolicyVo notifyPolicyVo = JSONObject.parseObject(new String(out.toByteArray(), StandardCharsets.UTF_8), NotifyPolicyVo.class);
                    save(notifyPolicyVo);
                    out.reset();
                    break;
                }
            } catch (Exception e) {
                logger.debug(e.getMessage(), e);
                throw new FileExtNotAllowedException(multipartFile.getOriginalFilename());
            }
        }
        return null;
    }

    private void save(NotifyPolicyVo notifyPolicyVo) {
        Long id = notifyPolicyVo.getId();
        String name = notifyPolicyVo.getName();
        String handler = notifyPolicyVo.getHandler();
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(handler);
        }
        if (notifyMapper.getNotifyPolicyByHandlerLimitOne(handler) != null && notifyPolicyHandler.isAllowMultiPolicy() == 0) {
            throw new NotifyPolicyMoreThanOneException(notifyPolicyHandler.getName());
        }
        int i = 1;
        while (notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
            notifyPolicyVo.setName(name + "_" + i);
            i++;
        }
        if (notifyMapper.getNotifyPolicyById(id) != null) {
            notifyPolicyVo.setLcu(UserContext.get().getUserUuid(true));
            notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
        } else {
            notifyPolicyVo.setFcu(UserContext.get().getUserUuid(true));
            notifyMapper.insertNotifyPolicy(notifyPolicyVo);
        }
    }
}
