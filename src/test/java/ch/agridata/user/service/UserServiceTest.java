package ch.agridata.user.service;

import static ch.agridata.auditing.api.ActionEnum.AGBS_ACCEPTED;
import static ch.agridata.auditing.api.EntityTypeEnum.AGB_REVISION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.auditing.api.AuditingApi;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.mapper.UserMapper;
import ch.agridata.user.persistence.UserEntity;
import ch.agridata.user.persistence.UserRepository;
import io.quarkus.oidc.UserInfo;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link UserService}.
 *
 * @CommentLastReviewed 2026-06-04
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService service;

  @Mock
  private AgridataSecurityIdentity identity;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private AgbRevisionService agbRevisionService;

  @Mock
  private AuditingApi auditingApi;

  @Spy
  private final Clock clock = Clock.fixed(Instant.parse("2026-07-20T10:00:00Z"), ZoneOffset.UTC);

  // ── helpers ──────────────────────────────────────────────────────────────

  /**
   * Stubs every {@code userInfo.getString()} call made by {@code updateUserData()} so that
   * Mockito strict-stubbing does not report a PotentialStubbingProblem for unmatched args.
   */
  private UserEntity setUpUpdateUserData(String locale) {
    var userId = UUID.randomUUID();
    var user = new UserEntity();
    var userInfo = mock(UserInfo.class);

    when(identity.getUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(user);
    when(identity.getUserInfoOrElseThrow()).thenReturn(userInfo);
    when(userInfo.getString("KT_ID_P")).thenReturn("KT-123");
    when(userInfo.getString("uid")).thenReturn("CHE123456789");
    when(userInfo.getString("email")).thenReturn("user@example.com");
    when(userInfo.getString("given_name")).thenReturn("Hans");
    when(userInfo.getString("family_name")).thenReturn("Muster");
    when(userInfo.getString("phone_number")).thenReturn("+41331234567");
    when(userInfo.getString("mobile_number")).thenReturn("+41791234567");
    when(userInfo.getString("locale")).thenReturn(locale);
    when(identity.getRoles()).thenReturn(Set.of("ROLE_CONSUMER"));
    when(userMapper.toUserInfoDto(any())).thenReturn(null);

    return user;
  }

  // ── updateUserData ────────────────────────────────────────────────────────

  @Test
  void givenFullUserInfo_whenUpdateUserData_thenAllUserFieldsSet() {
    var user = setUpUpdateUserData("de");

    service.updateUserData();

    assertThat(user.getKtIdP()).isEqualTo("KT-123");
    assertThat(user.getUid()).isEqualTo("CHE123456789");
    assertThat(user.getEmail()).isEqualTo("user@example.com");
    assertThat(user.getGivenName()).isEqualTo("Hans");
    assertThat(user.getFamilyName()).isEqualTo("Muster");
    assertThat(user.getPhoneNumber()).isEqualTo("+41331234567");
    assertThat(user.getRolesAtLastLogin()).isEqualTo(Set.of("ROLE_CONSUMER"));
    assertThat(user.getLastLoginDate()).isNotNull();
    assertThat(user.getLanguage()).isEqualTo(SupportedLanguage.DE);
  }

  @Test
  void givenFrLocale_whenUpdateUserData_thenLanguageSetToFr() {
    var user = setUpUpdateUserData("fr");

    service.updateUserData();

    assertThat(user.getLanguage()).isEqualTo(SupportedLanguage.FR);
  }

  @Test
  void givenItLocale_whenUpdateUserData_thenLanguageSetToIt() {
    var user = setUpUpdateUserData("it");

    service.updateUserData();

    assertThat(user.getLanguage()).isEqualTo(SupportedLanguage.IT);
  }

  @Test
  void givenNullLocale_whenUpdateUserData_thenLanguageDefaultsToDe() {
    var user = setUpUpdateUserData(null);

    service.updateUserData();

    assertThat(user.getLanguage()).isEqualTo(SupportedLanguage.DE);
  }

  @Test
  void givenUnknownLocale_whenUpdateUserData_thenLanguageDefaultsToDe() {
    var user = setUpUpdateUserData("en");

    service.updateUserData();

    assertThat(user.getLanguage()).isEqualTo(SupportedLanguage.DE);
  }

  // ── acceptCurrentAgb ──────────────────────────────────────────────────────

  @Test
  void whenAcceptCurrentAgb_thenUserFieldsStampedAndActionAudited() {
    var userId = UUID.randomUUID();
    var user = new UserEntity();
    var revisionId = UUID.randomUUID();
    var revision = AgbRevisionDto.builder().id(revisionId).build();

    when(identity.getUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(user);
    when(agbRevisionService.getCurrentRevision()).thenReturn(revision);

    service.acceptCurrentAgb(revisionId);

    assertThat(user.getLastAcceptedAgbDate()).isEqualTo(LocalDateTime.now(clock));
    assertThat(user.getLastAcceptedAgbRevisionId()).isEqualTo(revisionId);
    verify(auditingApi).logUserAction(AGBS_ACCEPTED, AGB_REVISION, revisionId);
  }

  @Test
  void givenMismatchedId_whenAcceptCurrentAgb_thenConflictAndUserNotStamped() {
    var revision = AgbRevisionDto.builder().id(UUID.randomUUID()).build();
    var mismatchedId = UUID.randomUUID();

    when(agbRevisionService.getCurrentRevision()).thenReturn(revision);

    assertThatThrownBy(() -> service.acceptCurrentAgb(mismatchedId))
        .isInstanceOf(WebApplicationException.class)
        .satisfies(ex -> assertThat(((WebApplicationException) ex).getResponse().getStatus())
            .isEqualTo(Response.Status.CONFLICT.getStatusCode()));

    verifyNoInteractions(userRepository, auditingApi);
  }
}
