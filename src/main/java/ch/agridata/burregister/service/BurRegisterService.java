package ch.agridata.burregister.service;

import ch.agridata.burregister.api.BurRegisterServiceApi;
import ch.agridata.burregister.dto.BurLocalUnitDto;
import ch.agridata.burregister.dto.BurRegisterOrganisationDto;
import ch.agridata.burregister.mapper.BurRegisterMapper;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.regex.Pattern;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Implements the BurRegisterServiceApi. It validates the UID, calls the BurWeb method {@code GetLocalUnitByUid} and maps the returned
 * local unit to an organisation via {@link BurRegisterMapper}.
 *
 * @CommentLastReviewed 2026-10-12
 */
@ApplicationScoped
public class BurRegisterService implements BurRegisterServiceApi {

  public static final String UID_REGEX = "^CHE\\d{9}$";
  private static final Pattern UID_PATTERN = Pattern.compile(UID_REGEX);

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final BurRegisterSoapClient soapClient;
  private final BurRegisterSoapSupport soapSupport;
  private final BurRegisterMapper burRegisterMapper;
  private final BurRegisterServiceApi burRegisterServiceApi;

  @Inject
  public BurRegisterService(
      AgridataSecurityIdentity agridataSecurityIdentity,
      @RestClient BurRegisterSoapClient soapClient,
      BurRegisterSoapSupport soapSupport,
      BurRegisterMapper burRegisterMapper,
      BurRegisterServiceApi burRegisterServiceApi) {
    this.agridataSecurityIdentity = agridataSecurityIdentity;
    this.soapClient = soapClient;
    this.soapSupport = soapSupport;
    this.burRegisterMapper = burRegisterMapper;
    this.burRegisterServiceApi = burRegisterServiceApi;
  }

  @Override
  public BurRegisterOrganisationDto getByUidOfCurrentUser() {
    // Self-reference resolved to the CDI proxy of this bean so the cache interceptor applies.
    return burRegisterServiceApi.getByUid(agridataSecurityIdentity.getUidOrElseThrow());
  }

  @Override
  @CacheResult(cacheName = "bur-register")
  public BurRegisterOrganisationDto getByUid(String uid) {
    if (uid == null || !UID_PATTERN.matcher(uid).matches()) {
      throw new ValidationException("Invalid UID format. Expected format is 'CHE' followed by 9 digits.");
    }

    BurLocalUnitDto localUnit;
    try {
      localUnit = soapSupport.extractResult(call(soapSupport.buildEnvelope(uid)))
          .orElseThrow(() -> new NotFoundException("Organisation not found in BUR for uid=" + uid));
    } catch (SoapFaultException | IllegalStateException | ProcessingException e) {
      throw new ExternalWebServiceException("BUR lookup failed for uid=" + uid + " error=" + e.getMessage(), e);
    }

    if (localUnit.getName() == null && localUnit.getLegalName() == null) {
      throw new NotFoundException("BUR returned no name for uid=" + uid);
    }
    return burRegisterMapper.toOrganisationDto(localUnit);
  }

  private String call(String envelope) {
    try (Response response = soapClient.getLocalUnitByUid(envelope)) {
      return response.readEntity(String.class);
    } catch (WebApplicationException e) {
      // SOAP faults come with HTTP 500; parse the body so the fault reason is not lost.
      return e.getResponse().readEntity(String.class);
    }
  }
}
