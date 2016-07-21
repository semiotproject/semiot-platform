package ru.semiot.services.tsdbservice.wamp;

import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import rx.Observer;

import java.io.StringReader;

public abstract class RDFMessageObserver implements Observer<String> {

  private Lang messageFormat;

  public RDFMessageObserver() {
    this(null);
  }

  public RDFMessageObserver(Lang messageFormat) {
    this.messageFormat = messageFormat != null ? messageFormat
        : RDFLanguages.nameToLang(CONFIG.wampMessageFormat());
  }

  @Override
  public void onCompleted() {
  }

  @Override
  public void onNext(String message) {
    try {
      Model model = ModelFactory.createDefaultModel()
          .read(new StringReader(message), null, messageFormat.getName());

      onNext(model);
    } catch (Throwable e) {
      onError(e);
    }
  }

  public abstract void onNext(Model model);

  protected ResultSet query(Model model, String query) {
    return query(model, QueryFactory.create(query));
  }

  protected ResultSet query(Model model, Query query) {
    return QueryExecutionFactory.create(query, model).execSelect();
  }
}
