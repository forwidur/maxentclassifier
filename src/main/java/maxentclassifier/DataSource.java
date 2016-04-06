package maxentclassifier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataSource {
  private static Connection c_ = newConnection();
  private static Connection newConnection() {
    Connection res = null;
    try {
      Class.forName("org.postgresql.Driver");
      return DriverManager.getConnection(
          "jdbc:postgresql://localhost:5432/casedb", "caseuser", "casepwd");
    } catch (Exception e) {
      System.err.println("Failed to connect to the DB: " + e);
      System.exit(1);
    }
    return res;
  }

  public List<Article> getArticles(String query) {
    ArrayList<Article> res = new ArrayList<>();

    try {
      Statement s = c_.createStatement();
      ResultSet rs = s.executeQuery(String.format(
          "SELECT title, abstract, specialty_ids, text_only FROM articles %s;", query));

      while (rs.next()) {
        String title = rs.getString("title");
        String abs = rs.getString("abstract");
        String text = rs.getString("text_only");
        for(Integer topic: (Integer[])rs.getArray("specialty_ids").getArray()) {
          res.add(new Article(title, abs, topic, text));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return res;
  }
}
