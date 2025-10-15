package integration.common;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.jsonfieldrewrite.JsonFieldRewriteConfigExtractor;
import ch.agridata.common.jsonfieldrewrite.JsonFieldRewriteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class JsonFieldRewriteServiceTest {

  private static final String JSON_FIELD_REWRITE_CONFIG = """
              [
                {
                  "path": "/agis/register-data/1/register",
                  "method": "POST",
                  "direction": "OUTBOUND",
                  "whitelistProperties": [],
                  "fallbackRewriteRule": {},
                  "rewriteRules": {
                    "uid": [
                      {
                        "pattern": "^CHE\\\\*\\\\*\\\\*789",
                        "replaceWith": "CHE123456789"
                      },
                      {
                        "pattern": "^CHE\\\\*\\\\*\\\\*321$",
                        "replaceWith": "CHE987654321"
                      }
                    ],
                    "ktIdP": [
                      {
                        "pattern": "^\\\\*\\\\*\\\\*456$",
                        "replaceWith": "VS123456"
                      }
                    ],
                    "ber": [
                      {
                        "pattern": "^\\\\*\\\\*\\\\*321$",
                        "replaceWith": "A654321"
                      }
                    ]
                  }
                },
                {
                  "path": "/agis/register-data/1/register",
                  "method": "POST",
                  "direction": "INBOUND",
                  "whitelistProperties": [
                    "legalForm",
                    "farmType"
                  ],
                  "fallbackRewriteRule": {
                    "replaceWith": null
                  },
                  "rewriteRules": {
                    "uid": [
                      {
                        "pattern": "^(.{3}).*(.{3})$",
                        "replaceWith": "$1***$2"
                      }
                    ],
                    "ktIdP": [
                      {
                        "pattern": "^.*(.{3})$",
                        "replaceWith": "***$1"
                      }
                    ],
                    "organisationName": [
                      {
                        "pattern": "^.*$",
                        "replaceWith": "***"
                      }
                    ],
                    "firstName": [
                      {
                        "pattern": "^.*$",
                        "replaceWith": "***"
                      }
                    ],
                    "lastName": [
                      {
                        "pattern": "^.*$",
                        "replaceWith": "***"
                      }
                    ],
                    "ber": [
                      {
                        "pattern": "^.*(.{3})$",
                        "replaceWith": "***$1"
                      }
                    ],
                    "ktIdB": [
                      {
                        "pattern": "^.*(.{3})$",
                        "replaceWith": "***$1"
                      }
                    ]
                  }
                }
              ]
      """;
  private final JsonFieldRewriteService jsonFieldRewriteService;
  private final JsonFieldRewriteConfigExtractor jsonFieldRewriteConfigExtractor;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void givenInboundRequest_jsonRewrittenCorrectly() throws JsonProcessingException {

    // Given
    String originalJsonString = """
        {
          "uid": "CHE123456789",
          "ktIdP": "BE123456",
          "organisationName": "Agridata AG",
          "firstName": "Peter",
          "lastName": "Muster",
          "legalForm": "01",
          "nested": {
            "ktIdB": "123456627",
            "ber": "123456693"
          },
          "farmType": "01",
          "other": "data",
          "other2": null
        }
        """;

    String expectedRewrittenJsonString = """
        {
          "uid": "CHE***789",
          "ktIdP": "***456",
          "organisationName": "***",
          "firstName": "***",
          "lastName": "***",
          "legalForm": "01",
          "nested": {
            "ktIdB": "***627",
            "ber": "***693"
          },
          "farmType": "01",
          "other": null,
          "other2": null
        }
        """;

    String path = "/agis/register-data/1/register";
    String method = "POST";
    String direction = "INBOUND";

    var config = jsonFieldRewriteConfigExtractor.extractConfig(JSON_FIELD_REWRITE_CONFIG, path, method, direction);

    String rewrittenJsonString = jsonFieldRewriteService.rewriteJson(originalJsonString, config);

    JsonNode expectedRewrittenJson = objectMapper.readTree(expectedRewrittenJsonString);
    JsonNode actualRewrittenJson = objectMapper.readTree(rewrittenJsonString);

    assertThat(actualRewrittenJson).isEqualTo(expectedRewrittenJson);
  }

  @Test
  void givenOutboundRequest_jsonRewrittenCorrectly() throws JsonProcessingException {

    // Given
    String originalJsonString = """
        {
          "personSearchParameters": {
            "uid": "CHE***789"
          },
            "dataRequestType": {
                "relationDepth": "allRelations"
          }
        }
        """;

    String expectedRewrittenJsonString = """
        {
          "personSearchParameters": {
            "uid": "CHE123456789"
          },
            "dataRequestType": {
                "relationDepth": "allRelations"
          }
        }
        """;

    String path = "/agis/register-data/1/register";
    String method = "POST";
    String direction = "OUTBOUND";

    var config = jsonFieldRewriteConfigExtractor.extractConfig(JSON_FIELD_REWRITE_CONFIG, path, method, direction);

    String rewrittenJsonString = jsonFieldRewriteService.rewriteJson(originalJsonString, config);

    JsonNode expectedRewrittenJson = objectMapper.readTree(expectedRewrittenJsonString);
    JsonNode actualRewrittenJson = objectMapper.readTree(rewrittenJsonString);

    assertThat(actualRewrittenJson).isEqualTo(expectedRewrittenJson);
  }
}
