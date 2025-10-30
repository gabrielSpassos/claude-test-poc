package com.gabrielspassos.service

import com.gabrielspassos.RandomSSNGenerator
import com.gabrielspassos.contracts.v1.request.UserRequest
import com.gabrielspassos.dao.UserDAO
import com.gabrielspassos.entity.UserEntity
import com.gabrielspassos.logger.Logger
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.when
import org.mockito.junit.jupiter.MockitoExtension

import java.security.MessageDigest
import java.util.UUID
import scala.compiletime.uninitialized

@ExtendWith(Array(classOf[MockitoExtension]))
class UserServiceTest {

  @Mock
  private var userDAO: UserDAO = uninitialized

  @Test
  def shouldCreateUser(): Unit = {
    val ssn = RandomSSNGenerator.generate()
    val encryptedSsn = encryptString(ssn)
    val request = UserRequest(ssn)
    val userEntity = UserEntity(
      id = UUID.randomUUID(),
      ssn = encryptedSsn,
      status = UserEntity.activeStatus
    )
    val userService = createUserService()

    when(userDAO.findBySsn(encryptedSsn)).thenReturn(Right(None))
    when(userDAO.save(any(classOf[UserEntity]))).thenReturn(Right(userEntity))

    val eitherResult = userService.createUser(request)

    eitherResult match {
      case Left(_) => fail("Should have success return")
      case Right(response) =>
        assertEquals(userEntity.id, response.id)
    }
  }

  @Test
  def shouldFindUserById(): Unit = {
    val userId = UUID.randomUUID().toString
    val userEntity = UserEntity(
      id = UUID.fromString(userId),
      ssn = "encrypted-ssn",
      status = UserEntity.activeStatus
    )
    val userService = createUserService()

    when(userDAO.findById(UUID.fromString(userId))).thenReturn(Right(Option(userEntity)))

    val eitherResult = userService.findUserByUserId(userId)

    eitherResult match {
      case Left(_) => fail("Should have success return")
      case Right(None) => fail("Should have found user")
      case Right(Some(user)) =>
        assertEquals(userId, user.id.toString)
    }
  }

  private def createUserService(): UserService = {
    val logger = Logger()
    UserService(logger = logger, userDAO = userDAO)
  }

  private def encryptString(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    digest.map("%02x".format(_)).mkString
  }
}
