package neatlogic.module.tenant.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.RC4Util;
import neatlogic.framework.crypto.handler.RSAAESCryptoHandler;
import neatlogic.framework.crypto.handler.RSACryptoHandler;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.exception.util.PasswordDecryptException;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/** 管理员解密接口回归；使用测试数据，断言失败时也不输出明文或密文。 */
public class DecryptCipherApiTest {
    private final DecryptCipherApi api = new DecryptCipherApi();
    private static final String TEST_TEXT = "Decrypt-Test@123";

    /** 验证接口元数据、管理员权限以及默认关闭审计的约定。 */
    @Test
    public void declaresAdminOnlyPrivateApiWithAuditDisabled() throws Exception {
        assertEquals("/cipher/decrypt", api.getToken());
        assertEquals("解密输入文本", api.getName());
        assertNull(api.getConfig());
        assertEquals(0, api.needAudit());
        assertEquals(ADMIN.class, DecryptCipherApi.class.getAnnotation(AuthAction.class).action());
        assertEquals(OperationTypeEnum.OPERATE,
                DecryptCipherApi.class.getAnnotation(OperationType.class).type());
        Param input = serviceMethod().getAnnotation(Input.class).value()[0];
        assertEquals("text", input.name());
        assertEquals(ApiParamType.STRING, input.type());
        assertTrue(input.isRequired());
        assertEquals(-1, input.maxLength());
        Param output = serviceMethod().getAnnotation(Output.class).value()[0];
        assertEquals("Return", output.name());
        assertEquals(ApiParamType.STRING, output.type());
    }

    /** 历史 RC4 前缀均通过工厂路由至原有处理器。 */
    @Test
    public void decryptsAllLegacyRc4Prefixes() throws Exception {
        String encrypted = RC4Util.encrypt(TEST_TEXT);
        String payload = encrypted.substring(RC4Util.PRE.length());
        for (String prefix : new String[]{"{RC4}", "RC4:", "{ENCRYPTED}"}) {
            assertTrue("历史 RC4 解密结果应与测试输入一致", TEST_TEXT.equals(decryptValidated(prefix + payload)));
        }
    }

    /** RSA 密文超过 200 字符时仍可通过参数校验并正确解密。 */
    @Test
    public void decryptsRsaCipherLongerThanTwoHundredCharacters() throws Exception {
        String encrypted = new RSACryptoHandler().encrypt(TEST_TEXT);
        assertTrue("RSA 测试密文应超过旧接口长度限制", encrypted.length() > 200);
        assertTrue("RSA 解密结果应与测试输入一致", TEST_TEXT.equals(decryptValidated(encrypted)));
    }

    /** 混合协议及其别名支持长密码，不截断密文或解密结果。 */
    @Test
    public void decryptsLongHybridCipherAndAliases() throws Exception {
        String text = TEST_TEXT.repeat(30);
        RSAAESCryptoHandler handler = new RSAAESCryptoHandler();
        String encrypted = handler.encrypt(text);
        assertTrue("混合协议测试密文应超过旧接口长度限制", encrypted.length() > 200);
        String payload = encrypted.substring(RSAAESCryptoHandler.ENCRYPTED_PREFIX.length());
        for (String prefix : handler.handlers()) {
            assertTrue("混合协议解密结果应与测试输入一致", text.equals(decryptValidated(prefix + payload)));
        }
    }

    /** 未识别前缀及普通文本保持工厂原样返回的兼容行为。 */
    @Test
    public void preservesUnknownPrefixAndPlainText() throws Exception {
        for (String text : new String[]{"UNKNOWN:example", "NO:example", TEST_TEXT}) {
            assertTrue("未识别格式应原样返回", text.equals(decryptValidated(text)));
        }
    }

    /** 缺失、null、空串和空白文本均由框架的必填校验拒绝。 */
    @Test
    public void rejectsMissingAndBlankInputThroughFrameworkValidation() throws Exception {
        Method method = serviceMethod();
        assertThrows(ParamNotExistsException.class, () -> api.validInput(method, new JSONObject()));
        for (String text : new String[]{null, "", " \t\n"}) {
            JSONObject paramObj = params(text);
            assertThrows(ParamNotExistsException.class, () -> api.validInput(method, paramObj));
        }
    }

    /** 直接调用业务方法时保留空值保护，不进入解密处理。 */
    @Test
    public void returnsNullForBlankInputWhenCalledDirectly() throws Exception {
        assertNull(api.myDoService(new JSONObject()));
        for (String text : new String[]{null, "", " \t\n"}) {
            assertNull(api.myDoService(params(text)));
        }
    }

    /** 错误格式和损坏载荷均转为无原始原因的安全业务异常。 */
    @Test
    public void rejectsMalformedCipherWithoutExposingCauseOrInput() {
        for (String text : new String[]{"{RC4}GG", "RSA:invalid-payload", "RSA.AES:invalid-payload",
                "RSA.AES:AAAA.AAAA", "AES:!!!!.!!!!"}) {
            PasswordDecryptException error = assertThrows(PasswordDecryptException.class,
                    () -> api.myDoService(params(text)));
            assertNull(error.getCause());
            assertEquals(0, error.getSuppressed().length);
            assertFalse("异常不得包含输入内容", String.valueOf(error.getMessage()).contains(text));
        }
    }

    /** 使用真实框架参数校验后执行接口，覆盖长密文的完整输入路径。 */
    private Object decryptValidated(String text) throws Exception {
        JSONObject paramObj = params(text);
        api.validInput(serviceMethod(), paramObj);
        return api.myDoService(paramObj);
    }

    /** 获取生产方法的参数声明，避免在测试中重复实现校验逻辑。 */
    private Method serviceMethod() throws NoSuchMethodException {
        return DecryptCipherApi.class.getMethod("myDoService", JSONObject.class);
    }

    /** 仅构造内存参数，不序列化或打印测试内容。 */
    private JSONObject params(String text) {
        JSONObject paramObj = new JSONObject();
        paramObj.put("text", text);
        return paramObj;
    }
}
