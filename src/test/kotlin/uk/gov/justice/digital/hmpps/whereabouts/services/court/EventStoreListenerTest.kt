package uk.gov.justice.digital.hmpps.whereabouts.services.court

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class EventStoreListenerTest {
  private val repository: VideoLinkBookingEventRepository = mock()
  private val clock: Clock = Clock.fixed(Instant.parse("2020-10-01T00:00:00Z"), ZoneId.of("UTC"))
  private val authenticationFacade: AuthenticationFacade = mock()
  private val booking = EventListenerTestData.booking
  private val createSpecification = EventListenerTestData.createSpecification
  private val updateSpecification = EventListenerTestData.updateSpecification

  private val listener = EventStoreListener(repository, clock, authenticationFacade)

  @BeforeEach
  fun programMocks() {
    whenever(authenticationFacade.currentUsername).thenReturn("A_USER")
  }

  @Test
  fun `booking created`() {
    listener.bookingCreated(booking, createSpecification, "WWI")

    verify(repository).save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.CREATE,
        timestamp = LocalDateTime.now(clock),
        userId = "A_USER",
        videoLinkBookingId = booking.id!!,
        agencyId = "WWI",
        court = createSpecification.court,
        comment = createSpecification.comment,
        offenderBookingId = createSpecification.bookingId,
        madeByTheCourt = createSpecification.madeByTheCourt,
        mainNomisAppointmentId = booking.main.appointmentId,
        mainLocationId = createSpecification.main.locationId,
        mainStartTime = createSpecification.main.startTime,
        mainEndTime = createSpecification.main.endTime,
        preNomisAppointmentId = booking.pre!!.appointmentId,
        preLocationId = createSpecification.pre!!.locationId,
        preStartTime = createSpecification.pre!!.startTime,
        preEndTime = createSpecification.pre!!.endTime,
        postLocationId = createSpecification.post!!.locationId,
        postNomisAppointmentId = booking.post!!.appointmentId,
        postStartTime = createSpecification.post!!.startTime,
        postEndTime = createSpecification.post!!.endTime
      )
    )
  }

  @Test
  fun `booking updated`() {
    listener.bookingUpdated(booking, updateSpecification)

    verify(repository).save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.UPDATE,
        timestamp = LocalDateTime.now(clock),
        userId = "A_USER",
        videoLinkBookingId = booking.id!!,
        comment = updateSpecification.comment,
        mainNomisAppointmentId = booking.main.appointmentId,
        mainLocationId = updateSpecification.main.locationId,
        mainStartTime = updateSpecification.main.startTime,
        mainEndTime = updateSpecification.main.endTime,
        preNomisAppointmentId = booking.pre!!.appointmentId,
        preLocationId = updateSpecification.pre!!.locationId,
        preStartTime = updateSpecification.pre!!.startTime,
        preEndTime = updateSpecification.pre!!.endTime,
        postLocationId = updateSpecification.post!!.locationId,
        postNomisAppointmentId = booking.post!!.appointmentId,
        postStartTime = updateSpecification.post!!.startTime,
        postEndTime = updateSpecification.post!!.endTime
      )
    )
  }

  @Test
  fun `booking deleted`() {
    listener.bookingDeleted(booking)

    verify(repository).save(
      VideoLinkBookingEvent(
        eventType = VideoLinkBookingEventType.DELETE,
        timestamp = LocalDateTime.now(clock),
        userId = "A_USER",
        videoLinkBookingId = booking.id!!
      )
    )
  }
}
