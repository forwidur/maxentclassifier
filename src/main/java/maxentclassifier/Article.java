package maxentclassifier;

import opennlp.tools.doccat.DocumentSample;

public class Article extends DocumentSample {
  public String title;
  public String abs;
  public Integer spec;
  public String body;

  public Article(String title, String abs, Integer topic, String text) {
    super(Integer.toString(topic), text);
    this.title = title;
    this.abs = abs;
    spec = topic;
    body = text;
  }
}
