package ch.agridata.common.utils;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.quarkus.panache.common.Sort;
import java.util.List;
import org.junit.jupiter.api.Test;

class JpaUtilTest {

  @Test
  void givenMultipleFields_createContainsWhereClause_generateSqlFilter() {
    var whereClause =
        JpaUtil.createContainsWhereClause(" test  input search ", List.of("field1", "field2", "field3", "field4", "field5"),
            List.of(List.of("field3", "field4", "field5")));
    assertThat(whereClause.parameters()).hasFieldOrProperty(JpaUtil.PARAM_FULL_SEARCH_STRING);
    assertThat(whereClause.parameters()).hasFieldOrProperty("param0");
    assertThat(whereClause.parameters()).hasFieldOrProperty("param1");
    assertThat(whereClause.parameters()).hasFieldOrProperty("param2");

    assertThat(whereClause.clause()).contains("LOWER(field1) LIKE :" + JpaUtil.PARAM_FULL_SEARCH_STRING);
    assertThat(whereClause.clause()).contains("LOWER(field2) LIKE :" + JpaUtil.PARAM_FULL_SEARCH_STRING);
    assertThat(whereClause.clause()).contains("LOWER(field4) LIKE :" + JpaUtil.PARAM_FULL_SEARCH_STRING);
    assertThat(whereClause.clause()).contains("LOWER(field5) LIKE :" + JpaUtil.PARAM_FULL_SEARCH_STRING);
    assertThat(whereClause.clause()).contains("LOWER(field3) LIKE :param0 AND LOWER(field4) LIKE :param1 AND LOWER(field5) LIKE :param2");
    assertThat(whereClause.clause()).contains("LOWER(field3) LIKE :" + JpaUtil.PARAM_FULL_SEARCH_STRING);
    assertThat(whereClause.clause()).contains("LOWER(field3) LIKE :param0 AND LOWER(field5) LIKE :param1 AND LOWER(field4) LIKE :param2");
    assertThat(whereClause.clause()).contains("LOWER(field5) LIKE :param0 AND LOWER(field3) LIKE :param1 AND LOWER(field4) LIKE :param2");
    assertThat(whereClause.clause()).contains("LOWER(field5) LIKE :param0 AND LOWER(field4) LIKE :param1 AND LOWER(field3) LIKE :param2");
    assertThat(whereClause.clause()).contains("LOWER(field4) LIKE :param0 AND LOWER(field5) LIKE :param1 AND LOWER(field3) LIKE :param2");
    assertThat(whereClause.clause()).contains("LOWER(field4) LIKE :param0 AND LOWER(field3) LIKE :param1 AND LOWER(field5) LIKE :param2");
  }

  @Test
  void givenNoFields_createContainsWhereClause_createDummy() {
    var whereClause =
        JpaUtil.createContainsWhereClause(" test  input search ", List.of(), List.of());
    assertThat(whereClause.parameters().size()).isZero();
    assertThat(whereClause.clause()).isEqualTo(JpaUtil.DUMMY_WHERE_CLAUSE);
  }

  @Test
  void parseSort() {
    var sort = JpaUtil.parseSort(List.of("field1", "-field2", " field3 ", " ", "- field4"));
    assertThat(sort).isNotNull();
    assertThat(sort.getColumns()).hasSize(4).extracting(Sort.Column::getName, Sort.Column::getDirection).containsExactlyInAnyOrder(
        tuple("field1", Sort.Direction.Ascending),
        tuple("field2", Sort.Direction.Descending),
        tuple("field3", Sort.Direction.Ascending),
        tuple("field4", Sort.Direction.Descending)
    );

  }
}