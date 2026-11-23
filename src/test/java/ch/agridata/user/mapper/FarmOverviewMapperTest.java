package ch.agridata.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agis.dto.AgisAddressInformationType;
import ch.agridata.agis.dto.AgisMailAddressType;
import ch.agridata.agis.dto.AgisOrganisationMailAddressInfoType;
import ch.agridata.agis.dto.AgisPersonMailAddressInfoType;
import ch.agridata.user.dto.AddressDto;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FarmOverviewMapper}, covering the special mapping rules for names, zip codes and post office boxes.
 *
 * @CommentLastReviewed 2026-11-23
 */
class FarmOverviewMapperTest {
  private final FarmOverviewMapper mapper = new FarmOverviewMapperImpl();

  @Test
  void givenOrganisationAndPerson_whenToName_thenOrganisationNameWins() {
    var postalAddress = new AgisMailAddressType()
        .organisation(new AgisOrganisationMailAddressInfoType().organisationName("Testfirma GmbH"))
        .person(new AgisPersonMailAddressInfoType().firstName("Max").lastName("Mustermann"));

    assertThat(mapper.toName(postalAddress)).contains("Testfirma GmbH");
  }

  @Test
  void givenPersonOnly_whenToName_thenFullName() {
    var postalAddress = new AgisMailAddressType()
        .person(new AgisPersonMailAddressInfoType().firstName("Max").lastName("Mustermann"));

    assertThat(mapper.toName(postalAddress)).contains("Max Mustermann");
  }

  @Test
  void givenNeitherOrganisationNorPerson_whenToName_thenEmpty() {
    assertThat(mapper.toName(new AgisMailAddressType())).isEmpty();
  }

  @Test
  void givenSwissAndForeignZipCode_whenToAddressDto_thenSwissZipCodeWins() {
    var address = new AgisAddressInformationType().swissZipCode(3001).foreignZipCode("D-79100");

    assertThat(mapper.toAddressDto(address).zipCode()).isEqualTo("3001");
  }

  @Test
  void givenForeignZipCodeOnly_whenToAddressDto_thenForeignZipCode() {
    var address = new AgisAddressInformationType().foreignZipCode("D-79100");

    assertThat(mapper.toAddressDto(address).zipCode()).isEqualTo("D-79100");
  }

  @Test
  void givenPostOfficeBoxTextAndNumber_whenToAddressDto_thenJoined() {
    var address = new AgisAddressInformationType()
        .addressLine1("c/o Muster")
        .street("Musterweg")
        .houseNumber("1")
        .postOfficeBoxText("Postfach")
        .postOfficeBoxNumber(12)
        .town("Bern");

    assertThat(mapper.toAddressDto(address)).isEqualTo(AddressDto.builder()
        .addressLine("c/o Muster")
        .street("Musterweg")
        .houseNumber("1")
        .postOfficeBox("Postfach 12")
        .town("Bern")
        .build());
  }

  @Test
  void givenNoPostalAddress_whenToCompanyDto_thenAddressIsNull() {
    var company = mapper.toCompanyDto("ZZZ199984068", "Nico Demomann", null);

    assertThat(company.uid()).isEqualTo("ZZZ199984068");
    assertThat(company.name()).isEqualTo("Nico Demomann");
    assertThat(company.address()).isNull();
  }
}
