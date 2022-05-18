package server;
import java.sql.*;

public class AuthService {
    private static Connection connection;
   private static Statement statement;

   public static void connect() {
       try {
           Class.forName("org.sqlite.JDBC");
           connection = DriverManager.getConnection("jdbc:sqlite:main.db");
           statement = connection.createStatement();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

    public static String authentication(String login, String password) {
        String request = String.format("SELECT root FROM users WHERE login = '%s' AND password = '%s'", login, password);
        try {
            ResultSet resultSet = statement.executeQuery(request);
             if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addAccount(String login, String password, String root) throws SQLException {
       String account = getAccount(login, password);
        if (account == null){
            try {
                String request = String.format("INSERT INTO users (login, password, root) VALUES " +
                        "('%s', '%s', '%s');", login, password, root);
                PreparedStatement ps = connection.prepareStatement(request);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (account != null){
            if (account.equals(account)){
                throw new SQLException("Аккаунт уже зарегистрирован");
            }
        }
    }

    public static String getAccount(String login, String password) {
       try {
           String request = String.format("SELECT login, password FROM users WHERE login = '%s' AND password = '%s' ;", login, password);
           ResultSet resultSet = statement.executeQuery(request);
           if (resultSet.next()) {
               String account = resultSet.getString(1);
               return account;
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
        return null;
    }

    public static String checkAccount(String login) {
       try {
           String request = String.format("SELECT login FROM users WHERE login = '%s';", login);
           ResultSet resultSet = statement.executeQuery(request);
           System.out.println(resultSet);
           if(resultSet.next()) {
               String account = resultSet.toString();
               return account;
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
    }


   public static void disconnect() {
       try {
           connection.close();
       } catch (SQLException e) {
           e.printStackTrace();
       }
   }
}
