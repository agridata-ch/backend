package ch.agridata.common.security.actingrole;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.openapi.ActingRoleOpenApiFilter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.junit.jupiter.api.Test;

class ActingRoleOpenApiFilterTest {

  private ActingRoleOpenApiFilter newFilterWithFixtures(String operationId, ActingRoleEnum... roles) throws Exception {
    ActingRoleOpenApiFilter filter = new ActingRoleOpenApiFilter();
    Field f = ActingRoleOpenApiFilter.class.getDeclaredField("rolesByOperationId");
    f.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.Map<String, EnumSet<ActingRoleEnum>> map = (java.util.Map<String, EnumSet<ActingRoleEnum>>) f.get(filter);
    map.clear();
    EnumSet<ActingRoleEnum> roleSet = roles.length == 0
        ? EnumSet.noneOf(ActingRoleEnum.class)
        : EnumSet.copyOf(Arrays.asList(roles));
    map.put(operationId, roleSet);
    return filter;
  }

  @Test
  void operation_with_actingRole_gets_actingRole_query_parameter() throws Exception {
    ActingRoleOpenApiFilter filter = newFilterWithFixtures("getDataRequests",
        ActingRoleEnum.CONSUMER, ActingRoleEnum.PROVIDER, ActingRoleEnum.ADMIN);

    Operation op = OASFactory.createOperation().operationId("getDataRequests");
    Operation result = filter.filterOperation(op);

    assertThat(result.getParameters()).hasSize(1);
    Parameter p = result.getParameters().getFirst();
    assertThat(p.getName()).isEqualTo("actingRole");
    assertThat(p.getIn()).isEqualTo(Parameter.In.QUERY);
    assertThat(p.getRequired()).isFalse();
    assertThat(p.getDescription()).isNotBlank();
    Schema schema = p.getSchema();
    assertThat(schema.getType()).contains(Schema.SchemaType.STRING);
    assertThat(schema.getEnumeration()).containsExactly("CONSUMER", "PROVIDER", "ADMIN");
  }

  @Test
  void operation_without_actingRole_is_left_untouched() throws Exception {
    ActingRoleOpenApiFilter filter = newFilterWithFixtures("someOtherOperation",
        ActingRoleEnum.CONSUMER);

    Operation op = OASFactory.createOperation().operationId("untouchedOperation");
    Operation result = filter.filterOperation(op);

    assertThat(result.getParameters()).isNull();
  }

  @Test
  void operation_with_null_operationId_is_left_untouched() throws Exception {
    ActingRoleOpenApiFilter filter = newFilterWithFixtures("anything", ActingRoleEnum.CONSUMER);

    Operation op = OASFactory.createOperation();
    Operation result = filter.filterOperation(op);

    assertThat(result.getParameters()).isNull();
  }

  @Test
  void enum_values_exactly_match_annotation_values() throws Exception {
    ActingRoleOpenApiFilter filter = newFilterWithFixtures("op",
        ActingRoleEnum.PRODUCER, ActingRoleEnum.SUPPORT);

    Operation op = OASFactory.createOperation().operationId("op");
    Operation result = filter.filterOperation(op);

    assertThat(result.getParameters().getFirst().getSchema().getEnumeration())
        .containsExactly("PRODUCER", "SUPPORT");
  }

  @Test
  void parameter_is_not_added_twice_on_repeated_invocation() throws Exception {
    ActingRoleOpenApiFilter filter = newFilterWithFixtures("op", ActingRoleEnum.CONSUMER);

    Operation op = OASFactory.createOperation().operationId("op");
    filter.filterOperation(op);
    Operation result = filter.filterOperation(op);

    long count = result.getParameters().stream().filter(p -> "actingRole".equals(p.getName())).count();
    assertThat(count).isEqualTo(1);
  }

  @Test
  void existing_parameters_are_preserved() throws Exception {
    ActingRoleOpenApiFilter filter = newFilterWithFixtures("op", ActingRoleEnum.CONSUMER);

    Parameter existing = OASFactory.createParameter().name("page").in(Parameter.In.QUERY);
    Operation op = OASFactory.createOperation().operationId("op").parameters(List.of(existing));
    Operation result = filter.filterOperation(op);

    assertThat(result.getParameters()).extracting(Parameter::getName).contains("page", "actingRole");
  }
}
