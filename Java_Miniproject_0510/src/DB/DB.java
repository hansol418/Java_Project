package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DB {
    public Connection connect = null;

    private String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE"; // Oracle JDBC URL
    private String DB_PW = null;
    private String DB_USERNAME = null;

    public DB() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver"); // Oracle 드라이버 클래스 이름
            Map<String, String> env = System.getenv();
            this.DB_USERNAME = env.get("DB_ID");
            this.DB_PW = env.get("DB_PW");
            System.out.println(this.DB_USERNAME);
            System.out.println(this.DB_PW);

            if (this.DB_USERNAME == null || this.DB_PW == null || this.DB_USERNAME.isEmpty() || this.DB_PW.isEmpty()) {
                System.out.println("환경변수를 입력하셨나요?");
            }

            this.connect = this.getConnection();
            System.out.println("연결 성공");

        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로딩 실패");
        } catch (SQLException e) {
            System.out.println("에러: " + e);
        } finally {
            try {
                if (this.connect != null && !this.connect.isClosed()) {
                    this.connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.DB_URL, this.DB_USERNAME, this.DB_PW);
    }

    public List<Map<String, Object>> query(String query) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            connect = this.getConnection();
            PreparedStatement pstmt = connect.prepareStatement(query);
            System.out.println(query);

            try {
                ResultSet res = pstmt.executeQuery();
                return this.resultSetToArrayList(res);
            } catch (SQLException e) {
                System.out.println(e);
                System.out.println("쿼리전달 시 오류발생");
                return list;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        } finally {
            try {
                if (connect != null && !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int update(String query) {
        try {
            connect = this.getConnection();
            PreparedStatement pstmt = connect.prepareStatement(query);
            System.out.println(query);

            try {
                int res = pstmt.executeUpdate(query);
                return res;
            } catch (SQLException e) {
                System.out.println(e);
                System.out.println("쿼리전달 시 오류발생");
                return -1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (connect != null && !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Map<String, Object>> resultSetToArrayList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        System.out.println(md);

        int columns = md.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; ++i) {
                String columnName = md.getColumnName(i);
                Object tuple = rs.getObject(i);
                row.put(columnName, tuple);
            }
            list.add(row);
        }
        return list;
    }

    public void disconnect() {
        try {
            this.connect.close();
        } catch (SQLException e) {
            System.out.println("close시 에러발생");
        }
    }
}
