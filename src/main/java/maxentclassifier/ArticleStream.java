package maxentclassifier;

import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.ObjectStream;

import java.io.IOException;
import java.util.List;

public class ArticleStream implements ObjectStream<DocumentSample> {
  private final List<Article> as_;
  private int cnt_ = 0;


  public ArticleStream(List<Article> as) {
    this.as_ = as;
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
