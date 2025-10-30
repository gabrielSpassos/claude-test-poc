package com.gabrielspassos.dao

import com.gabrielspassos.dao.repository.ReportRepository
import com.gabrielspassos.dto.{ErrorDTO, InternalErrorDTO}
import com.gabrielspassos.entity.ReportEntity
import com.gabrielspassos.logger.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.jdk.OptionConverters.*

@Component
class ReportDAO @Autowired()(private val reportRepository: ReportRepository,
                             private val logger: Logger) {
  
  def save(reportEntity: ReportEntity): Either[ErrorDTO, ReportEntity] = {
    try {
      val savedEntity = reportRepository.save(reportEntity)
      Right(savedEntity)
    } catch {
      case ex: Exception =>
        logger.logError(s"Failure to save report for userId=${reportEntity.userId}", ex)
        Left(InternalErrorDTO(s"Failure to save report for userId=${reportEntity.userId}"))
    }
  }

  def findByUserId(userId: String): Either[ErrorDTO, Option[ReportEntity]] = {
    try {
      val result = reportRepository.findByUserId(userId).toScala
      Right(result)
    } catch {
      case ex: Exception =>
        logger.logError(s"Failure to fetch report by userId=$userId", ex)
        Left(InternalErrorDTO(s"Failure to fetch report by userId=$userId"))
    }
  }

}
