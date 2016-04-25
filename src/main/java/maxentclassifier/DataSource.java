package maxentclassifier;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.List;

public class DataSource {
  public List<Article> getArticles(String path) {
    Gson g = new Gson();
    try {
      return g.fromJson(g.newJsonReader(new FileReader(path)),
          new TypeToken<List<Article>>(){}.getType());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }
}
