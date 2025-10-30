package com.gabrielspassos.service

import com.gabrielspassos.client.ReportClient
import com.gabrielspassos.dao.ReportDAO
import com.gabrielspassos.dao.repository.ReportRepository
import com.gabrielspassos.dto.{BadRequestErrorDTO, ErrorDTO, NotFoundErrorDTO}
import com.gabrielspassos.entity.ReportEntity
import com.gabrielspassos.logger.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.jdk.OptionConverters.*


@Service
class ReportService @Autowired()(private val logger: Logger,
                                 private val userService: UserService,
                                 private val reportClient: ReportClient,
                                 private val reportDAO: ReportDAO) {
  
  def createReport(userId: String): Either[ErrorDTO, ReportEntity] = {
    for {
      userOption <- userService.findUserByUserId(userId)
      _ <- if (userOption.isEmpty) {
        Left(NotFoundErrorDTO("Not found user"))
      } else { Right(()) }
      
      user = userOption.get
      _ <- if (!user.isActive) {
        Left(BadRequestErrorDTO("User not active to create report"))
      } else { Right(()) }
      
      content <- reportClient.generateReport()
      
      reportOption <- reportDAO.findByUserId(userId)
      report = reportOption match {
        case Some(report) =>
          report.copy(content = content)
        case None =>
          ReportEntity(
            id = null,
            userId = user.id,
            content = content
          )
      }
      savedReport <- reportDAO.save(report)
    } yield savedReport
  }
}
