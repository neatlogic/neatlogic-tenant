package neatlogic.module.tenant.api.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class JdbcExampleApi extends PrivateApiComponentBase {
    @Override
    public String getName() {
        return "JDBC连接数据库";
    }

    @Input({})
    @Output({})
    @Description(desc = "JDBC连接数据库")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        // 数据库连接参数
        String url = "jdbc:mysql://192.168.1.93:2881/oceanbase";
        String username = "root";
        String password = "root123";
//        String url = "jdbc:mysql://192.168.3.90:4000/neatlogic";
//        String username = "root";
//        String password = "neatlogic@901";
        // 加载驱动程序
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        // 建立连接
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            int databaseMajorVersion = metaData.getDatabaseMajorVersion();
            int databaseMinorVersion = metaData.getDatabaseMinorVersion();
            String driverVersion = metaData.getDriverVersion();
            String driverName = metaData.getDriverName();
            System.out.println("driverName = " + driverName);
            System.out.println("driverVersion = " + driverVersion);
            System.out.println("databaseMinorVersion = " + databaseMinorVersion);//0
            System.out.println("databaseMajorVersion = " + databaseMajorVersion);//8
            System.out.println("databaseProductVersion = " + databaseProductVersion);// 8.0.11-TiDB-v7.4.0
            System.out.println("databaseProductName = " + databaseProductName);// MySQL
            // 创建 Statement 对象来执行 SQL 语句
            Statement statement = connection.createStatement();
            // 执行 SQL 查询并获取结果集
            ResultSet resultSet = statement.executeQuery("SELECT @@version");
            // 处理结果集
            while (resultSet.next()) {
//                System.out.println("ID: " + resultSet.getInt("user_id"));
                System.out.println("Name: " + resultSet.getString(1));
//                System.out.println("Email: " + resultSet.getString("email"));
            }
            // 关闭结果集和连接
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getToken() {
        return "jdbc";
    }
}
