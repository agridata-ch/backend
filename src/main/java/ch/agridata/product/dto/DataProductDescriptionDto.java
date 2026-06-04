package ch.agridata.product.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the multilangual description of a DataProduct.
 *
 * @CommentLastReviewed 2026-06-11
 */

@Builder
public record DataProductDescriptionDto(
    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "German description of the data product",
        examples = {
            "Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 03 Tierschutz mit den Ergebnissen pro "
                + "Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel."}
    )
    String de,

    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "French description of the data product",
        examples = {
            "Ce produit de données contient des informations sur les contrôles du domaine de contrôle 03 Protection des animaux avec les "
                + "résultats par point de contrôle. Maximum les 4 dernières années ou depuis le changement d'exploitant."}
    )
    String fr,

    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "Italian description of the data product",
        examples = {
            "Questo prodotto dati contiene informazioni sui controlli dell'area di controllo 03 Protezione degli animali con i risultati "
                + "per punto di controllo. Massimo gli ultimi 4 anni o dal cambio di gestore."}
    )
    String it
) {
}
