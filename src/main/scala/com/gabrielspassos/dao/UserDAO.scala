package com.gabrielspassos.dao

import com.gabrielspassos.dao.repository.UserRepository
import com.gabrielspassos.dto.{ErrorDTO, InternalErrorDTO}
import com.gabrielspassos.entity.UserEntity
import com.gabrielspassos.logger.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.UUID
import scala.jdk.OptionConverters.*

@Component
class UserDAO @Autowired()(private val logger: Logger,
                           private val userRepository: UserRepository) {

  def save(userEntity: UserEntity): Either[ErrorDTO, UserEntity] = {
    try {
      val savedEntity = userRepository.save(userEntity)
      Right(savedEntity)
    } catch {
      case ex: Exception =>
        logger.logError(s"Failure to save user with ssn=${userEntity.ssn}", ex)
        Left(InternalErrorDTO(s"Failure to save user with ssn=${userEntity.ssn}"))
    }
  }

  def findById(userId: UUID): Either[ErrorDTO, Option[UserEntity]] = {
    try {
      val result = userRepository.findById(userId).toScala
      Right(result)
    } catch {
      case ex: Exception =>
        logger.logError(s"Failure to fetch user by userId=$userId", ex)
        Left(InternalErrorDTO(s"Failure to fetch report by userId=$userId"))
    }
  }

  def findBySsn(ssn: String): Either[ErrorDTO, Option[UserEntity]] = {
    try {
      val result = userRepository.findBySsn(ssn).toScala
      Right(result)
    } catch {
      case ex: Exception =>
        logger.logError(s"Failure to fetch user by ssn=$ssn", ex)
        Left(InternalErrorDTO(s"Failure to fetch report by ssn=$ssn"))
    }
  }
}
