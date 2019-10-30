package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonsDto
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService

@Api(tags = ["absence-reasons"])
@RestController
@RequestMapping(value = ["absence-reasons"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AbsentReasonsController(private val attendanceService: AttendanceService) {

  @GetMapping
  fun reasons(): AbsentReasonsDto = attendanceService.absenceReasons
}