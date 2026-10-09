package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.persistence.ConsentRequestExportProjection;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

/**
 * Exports the active consent requests of a data request as CSV. The format targets Excel in Swiss, German, French and Spanish
 * locales: semicolon-separated, UTF-8 with BOM and locale-neutral {@code yyyy-MM-dd HH:mm:ss} timestamps.
 *
 * @CommentLastReviewed 2026-10-09
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestExportService {

  private static final String BOM = "﻿";
  private static final String DELIMITER = ";";
  private static final String LINE_SEPARATOR = "\r\n";
  private static final String HEADER = String.join(DELIMITER, "uid", "bur", "requestDate", "lastChangeDate", "stateCode");
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final ConsentRequestRepository consentRequestRepository;
  private final DataRequestRepository dataRequestRepository;
  private final AgridataSecurityIdentity identity;

  @RolesAllowed(CONSUMER_ROLE)
  public String exportConsentRequestsOfDataRequestOfCurrentConsumerAsCsv(UUID dataRequestId) {
    if (dataRequestRepository.findByIdAndDataConsumerUid(dataRequestId, identity.getUidOrElseThrow()).isEmpty()) {
      throw new NotFoundException(dataRequestId.toString());
    }
    var rows = consentRequestRepository.findActiveExportProjectionsByDataRequestId(dataRequestId).stream()
        .map(this::toCsvRow);

    return BOM + Stream.concat(Stream.of(HEADER), rows)
        .map(row -> row + LINE_SEPARATOR)
        .collect(Collectors.joining());
  }

  private String toCsvRow(ConsentRequestExportProjection consentRequest) {
    return String.join(DELIMITER,
        consentRequest.dataProducerUid(),
        Objects.toString(consentRequest.dataProducerBur(), ""),
        format(consentRequest.requestDate()),
        format(consentRequest.lastStateChangeDate()),
        consentRequest.stateCode().name()
    );
  }

  private static String format(LocalDateTime dateTime) {
    return dateTime == null ? "" : DATE_TIME_FORMATTER.format(dateTime);
  }
}
