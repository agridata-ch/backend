package ch.agridata.product.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the multilingual extended description of a DataProduct.
 *
 * @CommentLastReviewed 2026-07-23
 */

@Builder
public record DataProductExtendedDescriptionDto(
    @Size(max = 10000, groups = {Default.class, ValidationSchemaGenerator.Submit.class})
    @Schema(
        description = "German extended description of the data product",
        examples = {
            """
                Konkret werden folgende Daten übermittelt:
                - Datum der Erfassung bzw. der letzten Änderung dieses Datensatzes im kantonalen System
                - KT_ID_B Kantonale Identifikationsnummer der Betriebsform auf der Stufe, auf der die nachfolgenden Daten..."""}
    )
    String de,

    @Size(max = 10000, groups = {Default.class, ValidationSchemaGenerator.Submit.class})
    @Schema(
        description = "French extended description of the data product",
        examples = {
            """
                Concrètement, les données suivantes sont transmises :
                - Date resp. la saisie ou la dernière modification du jeu de données dans le système cantonal
                - KT_ID_B Numéro cantonal de la forme d’exploitation au niveau de laquelle les données suivantes ont été saisies..."""}
    )
    String fr,

    @Size(max = 10000, groups = {Default.class, ValidationSchemaGenerator.Submit.class})
    @Schema(
        description = "Italian extended description of the data product",
        examples = {
            """
                Concretamente vengono trasmessi i dati seguenti:
                - Data di registrazione o dell’ultima modifica di questo set di dati nel sistema cantonale
                - KT_ID_B Numero d’identificazione cantonale della forma di azienda al livello a cui sono stati registrati i dati..."""}
    )
    String it
) {
}
