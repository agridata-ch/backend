package ch.agridata.agreement.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the multilingual description of a data request. It ensures validation constraints and standardized schema annotations for
 * consistency across contexts.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Builder
public record DataRequestDescriptionDto(
    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "Description of the data request",
        examples = {
            "Ziel der Anfrage ist es, Informationen über Bodentypen und deren Qualität zu erhalten, um fundierte Entscheidungen zur "
                + "Fruchtfolge, Düngung und nachhaltigen Bewirtschaftung treffen zu können."
        }
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String de,

    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "Description de la demande de données",
        examples = {
            "L'objectif de la demande est d'obtenir des informations sur les types de sols et leur qualité afin de prendre des décisions"
                + " éclairées concernant la rotation des cultures, la fertilisation et la gestion durable."
        }
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String fr,

    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "Descrizione della richiesta di dati",
        examples = {
            "L'obiettivo della richiesta è ottenere informazioni sui tipi di suolo e sulla loro qualità per prendere decisioni"
                + " informate sulla rotazione delle colture, la fertilizzazione e la gestione sostenibile."
        }
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String it
) {

}
