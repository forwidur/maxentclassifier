package maxentclassifier;

import java.util.Arrays;
import java.util.List;

public class Article {
  public Integer id;
  public String title;
  public String[] titlet;
  public String[] titlets;
  public String abs;
  public String[] abst;
  public String[] absts;
  public List<Integer> specs;
  public String[] kws;
  public String[] kwss;
//  public String body = null;
//  public String[] bodyt;
//  public String[] bodyts;

  public static <T> T[] concatAll(T[] first, T[]... rest) {
    int totalLength = first.length;
    for (T[] array : rest) {
      totalLength += array.length;
    }
    T[] result = Arrays.copyOf(first, totalLength);
    int offset = first.length;
    for (T[] array : rest) {
      System.arraycopy(array, 0, result, offset, array.length);
      offset += array.length;
    }
    return result;
  }

  public String[] signals() {
    return concatAll(titlets, absts, kwss);
  }
}
