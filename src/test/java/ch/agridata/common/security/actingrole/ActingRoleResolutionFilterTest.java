package ch.agridata.common.security.actingrole;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActingRoleResolutionFilterTest {

  @Mock
  private SecurityIdentity securityIdentity;
  @Spy
  private final ActingRoleHolder actingRoleHolder = new ActingRoleHolder();
  @Mock
  private ResourceInfo resourceInfo;
  @Mock
  private UriInfo uriInfo;
  @InjectMocks
  private ActingRoleResolutionFilter filter;

  private final MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();

  static class TestResource {
    @RolesAllowed({CONSUMER_ROLE, PROVIDER_ROLE})
    @EnableActingRoleHolder
    @SuppressWarnings("unused")
    public void consumerOrProvider() {
      // empty because only method annotations are tested
    }

    @RolesAllowed({PRODUCER_ROLE})
    @EnableActingRoleHolder
    @SuppressWarnings("unused")
    public void producerOnly() {
      // empty because only method annotations are tested
    }

    @SuppressWarnings("unused")
    public void noAnnotation() {
      // empty because only method annotations are tested
    }
  }

  @BeforeEach
  void setUp() {
    filter.resourceInfo = resourceInfo;
    filter.uriInfo = uriInfo;
    lenient().when(uriInfo.getQueryParameters()).thenReturn(queryParams);
  }

  @Test
  void givenMethodWithoutEnableActingRoleHolder_whenFilter_thenNoResolution() throws Exception {
    givenResourceMethod("noAnnotation");
    givenUserRoles(CONSUMER_ROLE);
    queryParams.put("actingRole", List.of("CONSUMER"));

    filter.filter(mock(ContainerRequestContext.class));

    assertThat(actingRoleHolder.getRole()).isNull();
  }

  @Test
  void givenNoResourceMethod_whenFilter_thenNoResolution() {
    when(resourceInfo.getResourceMethod()).thenReturn(null);

    filter.filter(mock(ContainerRequestContext.class));

    assertThat(actingRoleHolder.getRole()).isNull();
  }

  @Test
  void givenValidActingRoleParamAndUserHasRole_whenFilter_thenRoleIsSet() throws Exception {
    givenResourceMethod("consumerOrProvider");
    givenUserRoles(CONSUMER_ROLE, PROVIDER_ROLE);
    queryParams.put("actingRole", List.of("PROVIDER"));

    filter.filter(mock(ContainerRequestContext.class));

    assertThat(actingRoleHolder.getRole()).isEqualTo(ActingRoleEnum.PROVIDER);
  }

  @Test
  void givenUnknownActingRoleParam_whenFilter_thenIllegalArgumentException() throws Exception {
    givenResourceMethod("consumerOrProvider");
    givenUserRoles(CONSUMER_ROLE);
    queryParams.put("actingRole", List.of("FOO"));

    assertThatThrownBy(() -> filter.filter(mock(ContainerRequestContext.class)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenActingRoleParamNotInAllowedList_whenFilter_thenIllegalArgumentException() throws Exception {
    givenResourceMethod("consumerOrProvider");
    givenUserRoles(CONSUMER_ROLE, ADMIN_ROLE);
    queryParams.put("actingRole", List.of("ADMIN"));

    assertThatThrownBy(() -> filter.filter(mock(ContainerRequestContext.class)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenActingRoleParamAndUserLacksRole_whenFilter_thenForbidden() throws Exception {
    givenResourceMethod("consumerOrProvider");
    givenUserRoles(CONSUMER_ROLE);
    queryParams.put("actingRole", List.of("PROVIDER"));

    assertThatThrownBy(() -> filter.filter(mock(ContainerRequestContext.class)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void givenNoActingRoleParamAndExactlyOneMatchingRole_whenFilter_thenRoleIsAutoResolved() throws Exception {
    givenResourceMethod("consumerOrProvider");
    givenUserRoles(CONSUMER_ROLE);

    filter.filter(mock(ContainerRequestContext.class));

    assertThat(actingRoleHolder.getRole()).isEqualTo(ActingRoleEnum.CONSUMER);
  }

  @Test
  void givenNoActingRoleParamAndMultipleMatchingRoles_whenFilter_thenIllegalArgumentException() throws Exception {
    givenResourceMethod("consumerOrProvider");
    givenUserRoles(CONSUMER_ROLE, PROVIDER_ROLE);

    assertThatThrownBy(() -> filter.filter(mock(ContainerRequestContext.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("required");
  }

  @Test
  void givenNoActingRoleParamAndNoMatchingRole_whenFilter_thenForbidden() throws Exception {
    givenResourceMethod("consumerOrProvider");
    givenUserRoles(ADMIN_ROLE);

    assertThatThrownBy(() -> filter.filter(mock(ContainerRequestContext.class)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void givenBlankActingRoleParam_whenFilter_thenTreatedAsAbsentAndAutoResolved() throws Exception {
    givenResourceMethod("consumerOrProvider");
    givenUserRoles(ADMIN_ROLE, PROVIDER_ROLE);
    queryParams.put("actingRole", List.of(""));

    filter.filter(mock(ContainerRequestContext.class));

    assertThat(actingRoleHolder.getRole()).isEqualTo(ActingRoleEnum.PROVIDER);
  }

  @Test
  void givenSupportUserImpersonatingProducer_whenFilterWithActingRoleProducer_thenForbidden() throws Exception {
    // Impersonation does not change SecurityIdentity#getRoles(): the support user holds SUPPORT but not CONSUMER,
    // so even when impersonating a consumer, actingRole=PRODUCER must yield 403.
    givenResourceMethod("producerOnly");
    givenUserRoles(SUPPORT_ROLE);
    queryParams.put("actingRole", List.of("PRODUCER"));

    assertThatThrownBy(() -> filter.filter(mock(ContainerRequestContext.class)))
        .isInstanceOf(ForbiddenException.class);
  }

  private void givenResourceMethod(String name) throws NoSuchMethodException {
    Method m = TestResource.class.getDeclaredMethod(name);
    when(resourceInfo.getResourceMethod()).thenReturn(m);
  }

  private void givenUserRoles(String... roles) {
    lenient().when(securityIdentity.getRoles()).thenReturn(Set.of(roles));
  }
}
