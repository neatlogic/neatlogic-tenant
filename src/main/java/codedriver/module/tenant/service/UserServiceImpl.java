package codedriver.module.tenant.service;

import codedriver.framework.dao.mapper.UserMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * @Description: 根据用户uuid集合与分组uuid集合查询激活的用户uuid
     * @Author: laiwt
     * @Date: 2021/3/5 17:06
     * @Params: [userUuidList, teamUuidList]
     * @Returns: java.util.Set<java.lang.String>
    **/
    @Override
    public Set<String> getUserUuidSetByUserUuidListAndTeamUuidList(List<String> userUuidList, List<String> teamUuidList) {
        Set<String> uuidList = new HashSet<>();
        if (CollectionUtils.isNotEmpty(userUuidList)){
            List<String> existUserUuidList = userMapper.checkUserUuidListIsExists(userUuidList,1);
            if(CollectionUtils.isNotEmpty(existUserUuidList)){
                uuidList.addAll(existUserUuidList.stream().collect(Collectors.toSet()));
            }
        }
        if(CollectionUtils.isNotEmpty(teamUuidList)){
            List<String> list = userMapper.getUserUuidListByTeamUuidList(teamUuidList);
            if(CollectionUtils.isNotEmpty(list)){
                uuidList.addAll(list.stream().collect(Collectors.toSet()));
            }
        }
        return uuidList;
    }
}
