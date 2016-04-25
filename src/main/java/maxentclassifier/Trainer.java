package maxentclassifier;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import opennlp.tools.doccat.*;
import opennlp.tools.util.TrainingParameters;

import java.io.IOException;
import java.util.*;

public class Trainer {
  public static DoccatModel train(List<Article> as, int iterations) {
    FeatureGenerator[] fs = {new BagOfWordsFeatureGenerator()};
    TrainingParameters param = new TrainingParameters();
    param.put("Threads", "8");
    param.put("Algorithm", "MAXENT");
    param.put("Cutoff", "2");
    param.put("Iterations", Integer.toString(iterations));

    DoccatModel res = null;
    try {
      res = DocumentCategorizerME.train("en",
          new ArticleStream(as), param, new DoccatFactory(null, fs));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(2);
    }
    return res;
  }

  private static double eval(DoccatModel m, List<Article> val_as) {
    DocumentCategorizerME c = new DocumentCategorizerME(m);

    double misses = 1;

    for(Article a: val_as) {
      double[] scores = c.categorize(a.signals());
      String cat = c.getBestCategory(scores);
      double topScore = Doubles.max(scores);
      boolean hit = a.specs.contains(Integer.parseInt(cat));
      if (!hit) {
        misses++;
        System.out.println(String.format("Miss %d(%s) as %s score %f",
            a.id, Joiner.on(",").join(a.specs.toArray()), cat, topScore));
      }
    }

    return 1 - misses / val_as.size();
  }

  private static int runLoop(List<Article> as, int reps, int step, int maxIter) {
    SortedMap<Integer, Double> iterResults = new TreeMap<>();
    int bestIter = 0;
    double bestIterAcc = 0;
    for(int iter = step; iter <= maxIter; iter += step) {
      double accAcum = 0;
      for (int rep = 0; rep < reps; ++rep) {
        System.out.println(String.format("======= ITER: %d REP: %d", iter, rep));
        // Otherwise can't split out a validation set.
        Collections.shuffle(as);

        final double split = 0.97;

        int toIndex = (int) Math.floor(split * as.size());
        List<Article> train_as = as.subList(0, toIndex);
        List<Article> val_as = as.subList(toIndex, as.size());

        DoccatModel m = train(train_as, iter);

        double acc = eval(m, val_as);
        accAcum += acc;
      }
      double avgAcc = accAcum / reps;

      iterResults.put(iter, avgAcc);

      if (avgAcc > bestIterAcc) {
        bestIter = iter;
        bestIterAcc = avgAcc;
      }

      System.out.println(String.format(
          "|||||| ITER: %d AVG ACC: %f (BEST %d %f)", iter, avgAcc, bestIter, bestIterAcc));
    }

    iterResults.forEach((k,v)->{
      System.out.println(String.format("ITER %d ACC %f", k, v));
    });

    System.out.println(String.format( "|||||| BEST ITER %d ACC %f", bestIter, bestIterAcc));
    return bestIter;
  }

  public static void main(String[] args) {
    DataSource s = new DataSource();

    String f = "../article-signal-gen/articles.json";
    System.out.print(String.format("Reading from: %s. ", f));
    final List<Article> as = s.getArticles(f);
    System.out.println(String.format("Read: %d articles.", as.size()));

    runLoop(as, 1, 200, 1000);
  }
}
