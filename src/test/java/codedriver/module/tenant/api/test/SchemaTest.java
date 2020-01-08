package codedriver.module.tenant.api.test;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObjectPatch;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SchemaTest {
    @Test
    public void baseTest() {
        MultiAttrsObjectPatch patch = (MultiAttrsObjectPatch) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{MultiAttrsObjectPatch.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("commit".equals(name)) {
                            return null;
                        }
                        System.out.print(name + ": ");
                        for (Object el : args) {
                            System.out.print(el + " ");
                        }
                        System.out.println();
                        return proxy;
                    }
                });

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("TestJson.json");
        if (in == null) {
            return;
        }

        try {
            JSONObject json = JSONObject.parseObject(in, JSONObject.class);
            System.out.println("input JSON: " + json);
            TaskSchema.inflateSavePatch(patch, json);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
