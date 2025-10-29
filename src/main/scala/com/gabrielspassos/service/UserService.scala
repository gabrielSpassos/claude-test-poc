package com.gabrielspassos.service

import com.gabrielspassos.contracts.v1.request.UserRequest
import com.gabrielspassos.entity.UserEntity
import com.gabrielspassos.exception.BadRequestException
import com.gabrielspassos.repository.UserRepository
import com.gabrielspassos.validator.UUIDValidator
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Service

import java.security.MessageDigest
import java.util.UUID
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

@Service
class UserService @Autowired()(private val userRepository: UserRepository) {
  
  def createUser(userRequest: UserRequest): UserEntity = {
    val encryptedSsn = encryptString(userRequest.getSsn)
    
    userRepository.findBySsn(encryptedSsn).toScala match {
      case Some(existingUser) => throw new BadRequestException("User already exists")
      case None => ()
    }

    val entity = UserEntity(
      id = null,
      ssn = encryptedSsn,
      status = UserEntity.activeStatus
    )
    userRepository.save(entity)
  }
  
  def findUserByUserId(userId: String): Option[UserEntity] = {
    val (isValid, userIdOption) = UUIDValidator.isValidUUID(userId)

    if (!isValid) {
      throw new BadRequestException("Invalid userId")
    }
    
    userRepository.findById(userIdOption.get).toScala
  }

  private def encryptString(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    digest.map("%02x".format(_)).mkString
  }

}
