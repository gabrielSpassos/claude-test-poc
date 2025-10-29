package com.gabrielspassos.controller.v1

import com.gabrielspassos.contract.impl.v1.ReportContractImpl
import com.gabrielspassos.contracts.v1.response.ReportResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Array("/v2/reports"))
class ReportController @Autowired()(private val reportContract: ReportContractImpl) {

  @PostMapping(Array("/{userId}"))
  def createReport(@PathVariable(name = "userId", required = true) userId: String): ResponseEntity[ReportResponse] = {
    val reportResponse = reportContract.createReport(userId)
    ResponseEntity.status(HttpStatus.OK).body(reportResponse)
  }

}
