package ch.agridata.datatransferv2.service.task;

import ch.agridata.datatransferv2.service.AgridataContext;
import com.google.common.collect.Range;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts the requested date from the request parameters and sets it on the context.
 *
 * @CommentLastReviewed 2026-02-25
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveRequestedDateTask implements UnaryOperator<AgridataContext> {

  @Override
  public AgridataContext apply(final AgridataContext context) {
    var requestedDateRange = Range.closed(Optional.ofNullable(context.getRequestParameters().get("date"))
            .or(() -> Optional.ofNullable(context.getRequestParameters().get("dateFrom")))
            .map(LocalDate::parse)
            .orElseGet(LocalDate::now),
        Optional.ofNullable(context.getRequestParameters().get("date"))
            .or(() -> Optional.ofNullable(context.getRequestParameters().get("dateTo")))
            .map(LocalDate::parse)
            .orElseGet(LocalDate::now));
    context.setRequestedDateRange(requestedDateRange);

    log.debug("Resolved requestedDateRange from request: {}", requestedDateRange);

    return context;
  }
}
