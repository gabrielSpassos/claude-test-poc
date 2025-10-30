package com.gabrielspassos.dao

import com.gabrielspassos.entity.UserEntity
import com.gabrielspassos.{BaseIntegrationTest, RandomSSNGenerator}
import org.junit.jupiter.api.Assertions.{assertNotNull, fail}
import org.junit.jupiter.api.{AfterEach, Test, TestInstance}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jdbc.core.JdbcAggregateTemplate

import java.security.MessageDigest
import scala.collection.mutable.ListBuffer

@SpringBootTest
@ComponentScan(Array("com.*"))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOIntegrationTest @Autowired()(private val userDAO: UserDAO,
                                          private val jdbcTemplate: JdbcAggregateTemplate) extends BaseIntegrationTest {

  private val userList: ListBuffer[UserEntity] = ListBuffer[UserEntity]()

  @AfterEach
  def afterEach(): Unit = {
    userList.foreach { user =>
      jdbcTemplate.delete(user)
    }
  }

  @Test
  def shouldSaveUser(): Unit = {
    val userToSave = createUser()

    val eitherResult = userDAO.save(userToSave)

    eitherResult match {
      case Left(_) => fail("failure on save user test")
      case Right(savedUser) =>
        assertNotNull(savedUser)
        assertNotNull(savedUser.id)
    }
  }

  private def createUser(): UserEntity = {
    val ssn = RandomSSNGenerator.generate()
    val encryptedSSN = encryptString(ssn)
    UserEntity(
      id = null,
      ssn = encryptedSSN,
      status = UserEntity.activeStatus
    )
  }

  private def encryptString(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    digest.map("%02x".format(_)).mkString
  }

}
