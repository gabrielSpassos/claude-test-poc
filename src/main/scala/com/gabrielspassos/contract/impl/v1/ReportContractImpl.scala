package com.gabrielspassos.contract.impl.v1

import com.gabrielspassos.contracts.v1
import com.gabrielspassos.contracts.v1.response.ReportResponse
import com.gabrielspassos.exception.HttpException
import com.gabrielspassos.service.ReportService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ReportContractImpl @Autowired(private val reportService: ReportService) extends v1.ReportContract {

  override def createReport(userId: String): ReportResponse = {
    reportService.createReport(userId) match {
      case Left(error) =>
        throw HttpException(message = error.getMessage, httpStatus = error.getStatus)
      case Right(entity) =>
        val response = ReportResponse()
        response.setContent(entity.content)
        response
    }
  }

}
