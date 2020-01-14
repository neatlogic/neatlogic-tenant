package codedriver.module.tenant.api.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Util {

    public static List<String> getList(JSONObject json, String key) {
        Object tmp = json.get(key);
        if (tmp instanceof String) {
            String[] arr = ((String) tmp).trim().split("\\s+");
            List<String> tags = new ArrayList<>(arr.length);
            for (String s : arr) {
                if (s == null || StringUtils.isBlank(s)) {
                    continue;
                }
                tags.add(s);
            }
            return tags.isEmpty() ? Collections.emptyList() : tags;
        } else if (tmp instanceof JSONArray) {
            JSONArray arr = (JSONArray) tmp;
            List<String> tags = new ArrayList<>(arr.size());
            for (Object el : arr) {
                if (!(el instanceof String)) {
                    continue;
                }
                String str = (String) el;
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                tags.add(str);
            }
            return tags.isEmpty() ? Collections.emptyList() : tags;
        }
        return Collections.emptyList();
    }
}
