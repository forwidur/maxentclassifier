package maxentclassifier;

import opennlp.tools.util.Span;
import opennlp.tools.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Tokenizer implements opennlp.tools.tokenize.Tokenizer {

  public static final Tokenizer INSTANCE;

  static {
    INSTANCE = new Tokenizer();
  }

  private HashSet<String> stopwords_ = new HashSet<>();

  public Tokenizer() {
    InputStream in = this.getClass().getClassLoader()
                                .getResourceAsStream("excluded_keywords.txt");
    try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
      String l;
      while ((l = br.readLine()) != null) {
        stopwords_.add(l);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  enum ChrType {
    WHITESPACE, ALPHANUMERICDASH, NUMERIC, OTHER
  }

  public String[] tokenize(String s) {
    return Span.spansToStrings(tokenizePos(s), s);
  }

  int cnt;

  public Span[] tokenizePos(String s) {
    ChrType charType = ChrType.WHITESPACE;
    ChrType state = charType;

    List<Span> tokens = new ArrayList<Span>();
    int sl = s.length();
    int start = -1;
    char pc = 0;
    for (int i = 0; i < sl; i++) {
      char c = s.charAt(i);
      if (StringUtil.isWhitespace(c)) {
        charType = ChrType.WHITESPACE;
      } else if (Character.isLetter(c) || c == '-') {
        // Allow digits in words that are not only digits.
        if (state == ChrType.NUMERIC) {
          state = ChrType.ALPHANUMERICDASH;
        }
        charType = ChrType.ALPHANUMERICDASH;
      } else if (Character.isDigit(c)) {
        // Allow digits in words that are not only digits.
        charType = state == ChrType.ALPHANUMERICDASH ?
            ChrType.ALPHANUMERICDASH : ChrType.NUMERIC;
      } else {
        charType = ChrType.WHITESPACE;
      }
      if (state == ChrType.WHITESPACE) {
        if (charType != ChrType.WHITESPACE) {
          start = i;
        }
      } else {
        if (charType != state) {
          // We only want alphanum with dash, longer than 2 characters.
          if (state == ChrType.ALPHANUMERICDASH && i - start > 2) {
            if (!stopwords_.contains(s.substring(start,i))) {
              tokens.add(new Span(start, i));
            }
          }
          start = i;
        }
      }
      state = charType;
      pc = c;
    }
    if (charType != ChrType.WHITESPACE) {
      tokens.add(new Span(start, sl));
    }

    /*
    System.out.println(s);
    System.out.println("===============================");
    System.out.println(String.join("|", Span.spansToStrings(tokens.toArray(new Span[tokens.size()]), s)));
    System.out.println("===============================");

    if (cnt++ > 10) {
      System.exit(1);
    }
    */

    return tokens.toArray(new Span[tokens.size()]);
  }
}
