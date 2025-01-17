package uk.gov.justice.digital.hmpps.whereabouts.services.court

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

@Component
class ApplicationInsightsEventListener(
  private val authenticationFacade: AuthenticationFacade,
  private val telemetryClient: TelemetryClient,
) : VideoLinkBookingEventListener {
  override fun bookingCreated(booking: VideoLinkBooking, specification: VideoLinkBookingSpecification, agencyId: String) {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.main.bookingId.toString(),
      "court" to specification.court,
      "user" to authenticationFacade.currentUsername,
      "agencyId" to agencyId,
      "madeByTheCourt" to specification.madeByTheCourt.toString(),
    )

    properties.putAll(appointmentDetail(booking.main, specification.main))
    booking.pre?.also { properties.putAll(appointmentDetail(it, specification.pre!!)) }
    booking.post?.also { properties.putAll(appointmentDetail(it, specification.post!!)) }

    telemetryClient.trackEvent("VideoLinkBookingCreated", properties, null)
  }

  override fun bookingUpdated(booking: VideoLinkBooking, specification: VideoLinkBookingUpdateSpecification) {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.main.bookingId.toString(),
      "court" to booking.main.court,
      "user" to authenticationFacade.currentUsername,
    )

    properties.putAll(appointmentDetail(booking.main, specification.main))
    booking.pre?.also { properties.putAll(appointmentDetail(it, specification.pre!!)) }
    booking.post?.also { properties.putAll(appointmentDetail(it, specification.post!!)) }

    telemetryClient.trackEvent("VideoLinkBookingUpdated", properties, null)
  }

  override fun bookingDeleted(booking: VideoLinkBooking) {
    telemetryClient.trackEvent("VideoLinkBookingDeleted", telemetryProperties(booking), null)
  }

  private fun telemetryProperties(booking: VideoLinkBooking): MutableMap<String, String?> {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.main.bookingId.toString(),
      "court" to booking.main.court,
      "user" to authenticationFacade.currentUsername,
    )

    properties.putAll(appointmentDetail(booking.main))
    booking.pre?.also { properties.putAll(appointmentDetail(it)) }
    booking.post?.also { properties.putAll(appointmentDetail(it)) }
    return properties
  }

  private fun appointmentDetail(
    appointment: VideoLinkAppointment,
    specification: VideoLinkAppointmentSpecification
  ): Map<String, String> {
    val prefix = appointment.hearingType.name.toLowerCase()
    return mapOf(
      "${prefix}AppointmentId" to appointment.appointmentId.toString(),
      "${prefix}Id" to appointment.id.toString(),
      "${prefix}Start" to specification.startTime.toString(),
      "${prefix}End" to specification.endTime.toString(),
    )
  }

  private fun appointmentDetail(appointment: VideoLinkAppointment): Map<String, String> {
    val prefix = appointment.hearingType.name.toLowerCase()
    return mapOf(
      "${prefix}AppointmentId" to appointment.appointmentId.toString(),
      "${prefix}Id" to appointment.id.toString(),
    )
  }
}
