package com.gabrielspassos.service

import com.gabrielspassos.contracts.v1.request.UserRequest
import com.gabrielspassos.dao.UserDAO
import com.gabrielspassos.dao.repository.UserRepository
import com.gabrielspassos.dto.{BadRequestErrorDTO, ErrorDTO}
import com.gabrielspassos.entity.UserEntity
import com.gabrielspassos.logger.Logger
import com.gabrielspassos.validator.UUIDValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.security.MessageDigest
import scala.jdk.OptionConverters.*

@Service
class UserService @Autowired()(private val logger: Logger, private val userDAO: UserDAO) {
  
  def createUser(userRequest: UserRequest): Either[ErrorDTO, UserEntity] = {
    val encryptedSsn = encryptString(userRequest.getSsn)
    
    for {
      alreadyExistingUser <- userDAO.findBySsn(encryptedSsn)
      _ <- if (alreadyExistingUser.isDefined) {
        Left(BadRequestErrorDTO("User already exists"))
      } else { Right(()) }
      entityToSave = UserEntity(
        id = null,
        ssn = encryptedSsn,
        status = UserEntity.activeStatus
      )
      savedEntity <- userDAO.save(entityToSave)
    } yield savedEntity
  }
  
  def findUserByUserId(userId: String): Either[ErrorDTO, Option[UserEntity]] = {
    val (isValid, userIdOption) = UUIDValidator.isValidUUID(userId)

    if (!isValid) {
      return Left(BadRequestErrorDTO("Invalid userId"))
    }
    
    userDAO.findById(userIdOption.get)
  }

  private def encryptString(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    digest.map("%02x".format(_)).mkString
  }

}
