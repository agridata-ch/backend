package ch.agridata.workflowpoc.service.task;

import ch.agridata.workflowpoc.service.workflowengine.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dummy for workflow poc
 *
 * @CommentLastReviewed 2026-01-07
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class DummyTaskOne implements UnaryOperator<AgridataContext> {

  @Override
  public AgridataContext apply(final AgridataContext context) {

    context.setExampleAttribute1(context.getRequestQueryParameters().get("exampleQueryParam"));

    log.info("DummyTaskOne executed. ExampleAttribute1={}, ExampleAttribute2={}",
        context.getExampleAttribute1(),
        context.getExampleAttribute2());

    return context;
  }
}
