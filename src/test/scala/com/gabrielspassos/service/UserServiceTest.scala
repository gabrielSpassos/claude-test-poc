package com.gabrielspassos.service

import com.gabrielspassos.dao.UserDAO
import com.gabrielspassos.entity.UserEntity
import com.gabrielspassos.logger.Logger
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.when
import org.mockito.junit.jupiter.MockitoExtension

import java.util.UUID
import scala.compiletime.uninitialized

@ExtendWith(Array(classOf[MockitoExtension]))
class UserServiceTest {
  
  @Mock
  private var userDAO: UserDAO = uninitialized
  
  @Test
  def shouldFindUserById(): Unit = {
    val userId = UUID.randomUUID().toString
    val userEntity = UserEntity(
      id = UUID.fromString(userId),
      ssn = "encrypted-ssn",
      status = UserEntity.activeStatus
    )
    
    when(userDAO.findById(UUID.fromString(userId))).thenReturn(Right(Option(userEntity)))
    
    val userService = createUserService()
    
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
}
