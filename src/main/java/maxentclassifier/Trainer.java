package maxentclassifier;

import opennlp.tools.doccat.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.TrainingParameters;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Trainer {
  public static DoccatModel train(List<Article> as) {
    FeatureGenerator[] fs = {new BagOfWordsFeatureGenerator()};
    TrainingParameters param = new TrainingParameters();
    param.put("Threads", "8");
    param.put("Algorithm", "MAXENT");
    param.put("Cutoff", "2");
    param.put("Iterations", "300");

    DoccatModel res = null;
    try {
      res = DocumentCategorizerME.train("en",
          new ArticleStream(as), param, new DoccatFactory(Tokenizer.INSTANCE, fs));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(2);
    }
    return res;
  }

  private static void eval(DoccatModel m, List<Article> val_as) {
    DocumentCategorizerEvaluator eval =
        new DocumentCategorizerEvaluator(new DocumentCategorizerME(m));

    for(Article a: val_as) {
      eval.processSample(a);
    }

    System.out.println(eval);
  }

  public static void main(String[] args) {
    DataSource s = new DataSource();

    final List<Article> as = s.getArticles("WHERE specialty_ids IS NOT NULL");
    System.out.println(String.format("Read: %d", as.size()));

    // Otherwise can't split out a validation set.
    Collections.shuffle(as);

    final double split = 0.95;

    int toIndex = (int) Math.floor(split * as.size());
    List<Article> train_as = as.subList(0, toIndex);
    List<Article> val_as = as.subList(toIndex, as.size());

    DoccatModel m = train(train_as);

    eval(m, val_as);
  }

}
