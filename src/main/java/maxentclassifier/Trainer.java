package maxentclassifier;

import com.google.common.base.Joiner;
import com.google.common.primitives.Doubles;
import opennlp.tools.doccat.*;
import opennlp.tools.util.TrainingParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

  private static double eval(DoccatModel m, List<Article> val_as, int iters, double targetPrecision) {
    DocumentCategorizerME c = new DocumentCategorizerME(m);

    double misses = 1;
    List<Double> missScores = new ArrayList<>();
    List<Double> hitScores = new ArrayList<>();

    for(Article a: val_as) {
      double[] scores = c.categorize(a.signals());
      String cat = c.getBestCategory(scores);
      double topScore = Doubles.max(scores);
      boolean hit = a.specs.contains(Integer.parseInt(cat));
      if (!hit) {
        misses++;
//        System.out.println(String.format("Miss %d(%s) as %s score %f",
//           a.id, Joiner.on(",").join(a.specs.toArray()), cat, topScore));
        missScores.add(topScore);
      } else {
        hitScores.add(topScore);
      }
    }

    try {
      Files.write(Paths.get("hits"), Joiner.on('\n').join(hitScores).getBytes());
      Files.write(Paths.get("misses"), Joiner.on('\n').join(missScores).getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }

    double precision = 1 - misses / val_as.size();

    try {
      Runtime.getRuntime().exec(String.format(
          "gnuplot -p -e \"TITLE='%d iterations precision %f'\" -p hist.plot", iters, precision));
    } catch (IOException e) {
      e.printStackTrace();
    }

    Collections.sort(hitScores);
    Collections.sort(missScores);

    return findRemainingArticles(targetPrecision, hitScores, missScores);
  }

  private static double findRemainingArticles(
      double targetPrec, List<Double> hits, List<Double> misses
  ) {
    assert targetPrec <= 1;
    final int th = hits.size();
    final int tm = misses.size();
    int hi = 0;
    int mi = 0;
    while (hi < tm) {
      final int rh = th - hi;
      final int rm = tm - mi;
      final double prec = (double)rh / (rh + rm);
      System.out.println(String.format("hi: %d mi: %d prec: %f", hi, mi, prec));
      if (prec >= targetPrec) {
        final double score = Double.min(hits.get(hi), misses.get(mi));
        final double remain = (double)(rh + rm) / (th + tm);
        System.out.println(String.format(
            "%f precision reached: score: %f, fraction of remaining articles: %f",
            targetPrec, score, remain));
        return remain;
      }
      if (hits.get(hi) <= misses.get(mi)) {
        hi++;
      } else {
        mi++;
      }
    }
    return 0;
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

        double acc = eval(m, val_as, iter, 0.8);
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
