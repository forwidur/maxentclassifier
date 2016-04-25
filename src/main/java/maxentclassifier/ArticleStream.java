package maxentclassifier;

import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.ObjectStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleStream implements ObjectStream<DocumentSample> {
  private final List<DocumentSample> as_ = new ArrayList<>();
  private int cnt_ = 0;
  private int specCnt_;


  public ArticleStream(List<Article> as) {
    as.stream().forEach(a -> {
      for(Integer s: a.specs) {
        as_.add(new DocumentSample(s.toString(), a.signals()));
      }
    });
  }

  @Override
  public DocumentSample read() throws IOException {
    if (cnt_ == as_.size()) {
      return null;
    }
    return as_.get(cnt_++);
  }

  @Override
  public void reset() throws IOException, UnsupportedOperationException {
    cnt_ = 0;
  }

  @Override
  public void close() throws IOException {}
}
