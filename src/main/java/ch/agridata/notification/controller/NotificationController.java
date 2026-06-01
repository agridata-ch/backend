package ch.agridata.notification.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.MOBILE_APP;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.notification.controller.NotificationController.PATH;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.notification.dto.InboxEntryDto;
import ch.agridata.notification.dto.MarkAsReadRequestDto;
import ch.agridata.notification.service.NotificationInboxService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Provides endpoints for notification inbox operations and queuing notifications.
 *
 * @CommentLastReviewed 2026-05-06
 */
@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Notifications",
    description = "Provides access to user notifications including inbox listing, marking as read, and queuing notifications."
)
@RunOnVirtualThread
public class NotificationController {

  public static final String PATH = "/api/notification/v1";

  private final NotificationInboxService inboxService;
  private final AgridataSecurityIdentity identity;

  @GET
  @Path("/inbox")
  @ApiSubset({WEB_APP, MOBILE_APP})
  @Operation(
      operationId = "getInbox", description = "Retrieves all inbox notification entries for the currently authenticated user."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Authenticated
  public PageResponseDto<InboxEntryDto> getInbox(@BeanParam @Valid ResourceQueryDto query) {
    return inboxService.getInboxForUser(identity.getUserId(), query);
  }

  @PUT
  @Path("/inbox/mark-as-read")
  @ApiSubset({WEB_APP, MOBILE_APP})
  @Operation(
      operationId = "markInboxAsRead", description = "Marks one or multiple inbox entries as read for the currently authenticated user."
  )
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Authenticated
  public void markAsRead(@NotNull @Valid MarkAsReadRequestDto request) {
    inboxService.markReadStatus(identity.getUserId(), request, true);
  }

  @PUT
  @Path("/inbox/mark-as-unread")
  @ApiSubset({WEB_APP, MOBILE_APP})
  @Operation(
      operationId = "markInboxAsUnread", description = "Marks one or multiple inbox entries as unread for the currently authenticated user."
  )
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Authenticated
  public void markAsUnread(@NotNull @Valid MarkAsReadRequestDto request) {
    inboxService.markReadStatus(identity.getUserId(), request, false);
  }
}
