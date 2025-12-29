package ch.agridata.tvd.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TvdEquidOwnerUidDtoTest {

  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource(
      value = {
          // desc | name | nameAddOn1 | nameAddOn2 | firstName | lastName | expectedDisplayName
          "'First+last name take precedence, incl. trim'| 'Company'| 'AddOn1'| 'AddOn2'| '  Max  '| ' Muster ' | 'Max Muster'",
          "'Only first name set'| ''| ''| ''| 'Max'| ''| 'Max'",
          "'Only last name set, first name is blank'| ''| ''| ''| '   '| 'Muster'| 'Muster'",
          "'First name set, fallback fields ignored'| 'Company AG'| 'Dept. X'| ''| 'Max'| ''| 'Max'",
          "'Fallback to name, incl. trim'| '  Agro Data  '| ''| ''| ''| ''| 'Agro Data'",
          "'Fallback to name + add-ons, blanks are filtered'| 'Company'| '  Dept. 1  '| '   '| ''| ''| 'Company Dept. 1'",
          "'Fallback only add-ons when name is blank'| '   '| ' c/o  '| ' Team '| ''| ''| 'c/o Team'",
          "'All null or blank results in empty string'| '   '| ''| '  '| ''| '   '| ''"
      },
      delimiter = '|'
  )
  void testGetDisplayName(
      String description,
      String name,
      String nameAddOn1,
      String nameAddOn2,
      String firstName,
      String lastName,
      String expectedDisplayName
  ) {
    var data = new TvdEquidOwnerUidDto.Data(
        "uid",
        "de",
        name,
        nameAddOn1,
        nameAddOn2,
        firstName,
        lastName
    );

    assertThat(data.getDisplayName()).isEqualTo(expectedDisplayName);
  }

}
