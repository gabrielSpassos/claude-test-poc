# Generating Tests

## Stack
- Scala 3.7.3
- Java 25
- Spring Boot 3.5.7
- SBT 1.11.7

## Prompts

1. 
- Input: 
```
create unit tests for class UserService covering happy and unhappy paths and cover all the code branches, do not hard code ids

Do this fast
```
- Test:
```bash
sbt clean "testOnly com.gabrielspassos.service.UserServiceTest"
```
- Output: 
```scala
+       @Test
74 +    def shouldReturnErrorWhenUserAlreadyExists(): Unit = {
75 +      val ssn = RandomSSNGenerator.generate()
76 +      val encryptedSsn = encryptString(ssn)
77 +      val request = UserRequest(ssn)
78 +      val existingUser = UserEntity(
79 +        id = UUID.randomUUID(),
80 +        ssn = encryptedSsn,
81 +        status = UserEntity.activeStatus
82 +      )
83 +      val userService = createUserService()
84 +  
85 +      when(userDAO.findBySsn(encryptedSsn)).thenReturn(Right(Option(existingUser)))
86 +  
87 +      val eitherResult = userService.createUser(request)
88 +  
89 +      eitherResult match {
90 +        case Left(error) =>
91 +          assertTrue(error.isInstanceOf[BadRequestErrorDTO])
92 +          assertEquals("User already exists", error.getMessage)
93 +        case Right(_) => fail("Should have returned error")
94 +      }
95 +    }
96 +  
97 +    @Test
98 +    def shouldReturnErrorWhenFindBySsnFails(): Unit = {
99 +      val ssn = RandomSSNGenerator.generate()
100 +      val encryptedSsn = encryptString(ssn)
101 +      val request = UserRequest(ssn)
102 +      val userService = createUserService()
103 +  
104 +      when(userDAO.findBySsn(encryptedSsn)).thenReturn(Left(InternalErrorDTO("Database error")))
105 +  
106 +      val eitherResult = userService.createUser(request)
107 +  
108 +      eitherResult match {
109 +        case Left(error) =>
110 +          assertTrue(error.isInstanceOf[InternalErrorDTO])
111 +          assertEquals("Database error", error.getMessage)
112 +        case Right(_) => fail("Should have returned error")
113 +      }
114 +    }
115 +  
116 +    @Test
117 +    def shouldReturnErrorWhenSaveFails(): Unit = {
118 +      val ssn = RandomSSNGenerator.generate()
119 +      val encryptedSsn = encryptString(ssn)
120 +      val request = UserRequest(ssn)
121 +      val userService = createUserService()
122 +  
123 +      when(userDAO.findBySsn(encryptedSsn)).thenReturn(Right(None))
124 +      when(userDAO.save(any(classOf[UserEntity]))).thenReturn(Left(InternalErrorDTO("Save failed")))
125 +  
126 +      val eitherResult = userService.createUser(request)
127 +  
128 +      eitherResult match {
129 +        case Left(error) =>
130 +          assertTrue(error.isInstanceOf[InternalErrorDTO])
131 +          assertEquals("Save failed", error.getMessage)
132 +        case Right(_) => fail("Should have returned error")
133 +      }
134 +    }
135 +  
136 +    @Test
137 +    def shouldReturnErrorWhenUserIdIsInvalid(): Unit = {
138 +      val invalidUserId = "invalid-uuid"
139 +      val userService = createUserService()
140 +  
141 +      val eitherResult = userService.findUserByUserId(invalidUserId)
142 +  
143 +      eitherResult match {
144 +        case Left(error) =>
145 +          assertTrue(error.isInstanceOf[BadRequestErrorDTO])
146 +          assertEquals("Invalid userId", error.getMessage)
147 +        case Right(_) => fail("Should have returned error")
148 +      }
149 +    }
150 +  
151 +    @Test
152 +    def shouldReturnNoneWhenUserNotFound(): Unit = {
153 +      val userId = UUID.randomUUID().toString
154 +      val userService = createUserService()
155 +  
156 +      when(userDAO.findById(UUID.fromString(userId))).thenReturn(Right(None))
157 +  
158 +      val eitherResult = userService.findUserByUserId(userId)
159 +  
160 +      eitherResult match {
161 +        case Left(_) => fail("Should have success return")
162 +        case Right(None) => assertTrue(true)
163 +        case Right(Some(_)) => fail("Should not have found user")
164 +      }
165 +    }
166 +  
167 +    @Test
168 +    def shouldReturnErrorWhenFindByIdFails(): Unit = {
169 +      val userId = UUID.randomUUID().toString
170 +      val userService = createUserService()
171 +  
172 +      when(userDAO.findById(UUID.fromString(userId))).thenReturn(Left(InternalErrorDTO("Database error")))
173 +  
174 +      val eitherResult = userService.findUserByUserId(userId)
175 +  
176 +      eitherResult match {
177 +        case Left(error) =>
178 +          assertTrue(error.isInstanceOf[InternalErrorDTO])
179 +          assertEquals("Database error", error.getMessage)
180 +        case Right(_) => fail("Should have returned error")
181 +      }
182 +    }
183 +  
```

2.
- Input: 
```
create integration tests for class ReportService covering happy and unhappy paths and all code branches

Please: 
1. Do this fast
```
- Test:
```bash
./run-tests.sh
```
- Output: 
```scala
package com.gabrielspassos.service

import com.gabrielspassos.client.ReportClient
import com.gabrielspassos.dao.repository.{ReportRepository, UserRepository}
import com.gabrielspassos.dto.InternalErrorDTO
import com.gabrielspassos.entity.{ReportEntity, UserEntity}
import com.gabrielspassos.{Application, BaseIntegrationTest, RandomSSNGenerator}
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue, fail}
import org.junit.jupiter.api.{AfterEach, Test, TestInstance}
import org.mockito.Mockito.when
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan

import java.security.MessageDigest
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.jdk.OptionConverters.*

@SpringBootTest(classes = Array(classOf[Application]))
@EnableAutoConfiguration
@ComponentScan(Array("com.*"))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportServiceIntegrationTest @Autowired()(
  private val reportService: ReportService,
  private val userRepository: UserRepository,
  private val reportRepository: ReportRepository
) extends BaseIntegrationTest {

  @MockBean
  private val reportClient: ReportClient = null

  private val userIds = ListBuffer[UUID]()

  @AfterEach
  def cleanUp(): Unit = {
    userIds.foreach(userId => {
      reportRepository.findByUserId(userId.toString).toScala match
        case Some(report) =>
          reportRepository.delete(report)
        case None => ()

      userRepository.findById(userId).toScala match
        case Some(user) =>
          userRepository.delete(user)
        case None => ()
    })
    userIds.clear()
  }

  @Test
  def shouldCreateReportForActiveUser(): Unit = {
    val userId = createActiveUser()
    val reportContent = "Report content for active user"

    when(reportClient.generateReport()).thenReturn(Right(reportContent))

    val eitherResult = reportService.createReport(userId.toString)

    eitherResult match {
      case Left(error) => fail(s"Should have created report successfully: ${error.getMessage}")
      case Right(report) =>
        assertNotNull(report)
        assertNotNull(report.id)
        assertEquals(userId, report.userId)
        assertEquals(reportContent, report.content)
    }
  }

  @Test
  def shouldUpdateExistingReport(): Unit = {
    val userId = createActiveUser()
    val initialContent = "Initial report content"
    val updatedContent = "Updated report content"

    val initialReport = ReportEntity(
      id = null,
      userId = userId,
      content = initialContent
    )
    val savedInitialReport = reportRepository.save(initialReport)

    when(reportClient.generateReport()).thenReturn(Right(updatedContent))

    val eitherResult = reportService.createReport(userId.toString)

    eitherResult match {
      case Left(error) => fail(s"Should have updated report successfully: ${error.getMessage}")
      case Right(report) =>
        assertNotNull(report)
        assertEquals(savedInitialReport.id, report.id)
        assertEquals(userId, report.userId)
        assertEquals(updatedContent, report.content)
    }
  }

  @Test
  def shouldFailWhenUserNotFound(): Unit = {
    val nonExistentUserId = UUID.randomUUID().toString

    when(reportClient.generateReport()).thenReturn(Right("Content"))

    val eitherResult = reportService.createReport(nonExistentUserId)

    eitherResult match {
      case Left(error) =>
        assertEquals(404, error.getStatus)
        assertEquals("Not found user", error.getMessage)
      case Right(_) => fail("Should have failed with user not found error")
    }
  }

  @Test
  def shouldFailWhenUserIsInactive(): Unit = {
    val userId = createInactiveUser()

    when(reportClient.generateReport()).thenReturn(Right("Content"))

    val eitherResult = reportService.createReport(userId.toString)

    eitherResult match {
      case Left(error) =>
        assertEquals(400, error.getStatus)
        assertEquals("User not active to create report", error.getMessage)
      case Right(_) => fail("Should have failed with user not active error")
    }
  }

  @Test
  def shouldFailWithInvalidUserId(): Unit = {
    val invalidUserId = "invalid-uuid-format"

    when(reportClient.generateReport()).thenReturn(Right("Content"))

    val eitherResult = reportService.createReport(invalidUserId)

    eitherResult match {
      case Left(error) =>
        assertEquals(400, error.getStatus)
        assertEquals("Invalid userId", error.getMessage)
      case Right(_) => fail("Should have failed with invalid userId error")
    }
  }

  @Test
  def shouldFailWhenReportClientFails(): Unit = {
    val userId = createActiveUser()
    val clientError = InternalErrorDTO("Failed to generate report")

    when(reportClient.generateReport()).thenReturn(Left(clientError))

    val eitherResult = reportService.createReport(userId.toString)

    eitherResult match {
      case Left(error) =>
        assertEquals(500, error.getStatus)
        assertEquals("Failed to generate report", error.getMessage)
      case Right(_) => fail("Should have failed when report client fails")
    }
  }

  private def createActiveUser(): UUID = {
    val ssn = RandomSSNGenerator.generate()
    val encryptedSsn = encryptString(ssn)
    val user = UserEntity(
      id = null,
      ssn = encryptedSsn,
      status = UserEntity.activeStatus
    )
    val savedUser = userRepository.save(user)
    userIds += savedUser.id
    savedUser.id
  }

  private def createInactiveUser(): UUID = {
    val ssn = RandomSSNGenerator.generate()
    val encryptedSsn = encryptString(ssn)
    val user = UserEntity(
      id = null,
      ssn = encryptedSsn,
      status = UserEntity.inactiveStatus
    )
    val savedUser = userRepository.save(user)
    userIds += savedUser.id
    savedUser.id
  }

  private def encryptString(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    digest.map("%02x".format(_)).mkString
  }
}
```

3.
- Input:
```
verify if there is any service class that needs unit test to implement and create then, covering happy, unhappy and all code branches

Please Do this fast
```
- Test:
```bash
sbt clean "testOnly com.gabrielspassos.service.ReportServiceTest"
```
- Output: 
```scala
│ src/test/scala/com/gabrielspassos/service/ReportServiceTest.scala                                                                                                                                                                                                           │
│                                                                                                                                                                                                                                                                             │
│ package com.gabrielspassos.service                                                                                                                                                                                                                                          │
│                                                                                                                                                                                                                                                                             │
│ import com.gabrielspassos.client.ReportClient                                                                                                                                                                                                                               │
│ import com.gabrielspassos.dao.ReportDAO                                                                                                                                                                                                                                     │
│ import com.gabrielspassos.dto.{BadRequestErrorDTO, InternalErrorDTO, NotFoundErrorDTO}                                                                                                                                                                                      │
│ import com.gabrielspassos.entity.{ReportEntity, UserEntity}                                                                                                                                                                                                                 │
│ import com.gabrielspassos.logger.Logger                                                                                                                                                                                                                                     │
│ import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue, fail}                                                                                                                                                                                     │
│ import org.junit.jupiter.api.Test                                                                                                                                                                                                                                           │
│ import org.junit.jupiter.api.extension.ExtendWith                                                                                                                                                                                                                           │
│ import org.mockito.ArgumentMatchers.any                                                                                                                                                                                                                                     │
│ import org.mockito.Mock                                                                                                                                                                                                                                                     │
│ import org.mockito.Mockito.when                                                                                                                                                                                                                                             │
│ import org.mockito.junit.jupiter.MockitoExtension                                                                                                                                                                                                                           │
│                                                                                                                                                                                                                                                                             │
│ import java.util.UUID                                                                                                                                                                                                                                                       │
│ import scala.compiletime.uninitialized                                                                                                                                                                                                                                      │
│                                                                                                                                                                                                                                                                             │
│ @ExtendWith(Array(classOf[MockitoExtension]))                                                                                                                                                                                                                               │
│ class ReportServiceTest {                                                                                                                                                                                                                                                   │
│                                                                                                                                                                                                                                                                             │
│   @Mock                                                                                                                                                                                                                                                                     │
│   private var userService: UserService = uninitialized                                                                                                                                                                                                                      │
│                                                                                                                                                                                                                                                                             │
│   @Mock                                                                                                                                                                                                                                                                     │
│   private var reportClient: ReportClient = uninitialized                                                                                                                                                                                                                    │
│                                                                                                                                                                                                                                                                             │
│   @Mock                                                                                                                                                                                                                                                                     │
│   private var reportDAO: ReportDAO = uninitialized                                                                                                                                                                                                                          │
│                                                                                                                                                                                                                                                                             │
│   @Test                                                                                                                                                                                                                                                                     │
│   def shouldCreateNewReport(): Unit = {                                                                                                                                                                                                                                     │
│     val userId = UUID.randomUUID()                                                                                                                                                                                                                                          │
│     val userIdString = userId.toString                                                                                                                                                                                                                                      │
│     val user = UserEntity(                                                                                                                                                                                                                                                  │
│       id = userId,                                                                                                                                                                                                                                                          │
│       ssn = "encrypted-ssn",                                                                                                                                                                                                                                                │
│       status = UserEntity.activeStatus                                                                                                                                                                                                                                      │
│     )                                                                                                                                                                                                                                                                       │
│     val reportContent = "Generated report content"                                                                                                                                                                                                                          │
│     val savedReport = ReportEntity(                                                                                                                                                                                                                                         │
│       id = UUID.randomUUID(),                                                                                                                                                                                                                                               │
│       userId = userId,                                                                                                                                                                                                                                                      │
│       content = reportContent                                                                                                                                                                                                                                               │
│     )                                                                                                                                                                                                                                                                       │
│     val reportService = createReportService()                                                                                                                                                                                                                               │
│                                                                                                                                                                                                                                                                             │
│     when(userService.findUserByUserId(userIdString)).thenReturn(Right(Some(user)))                                                                                                                                                                                          │
│     when(reportClient.generateReport()).thenReturn(Right(reportContent))                                                                                                                                                                                                    │
│     when(reportDAO.findByUserId(userIdString)).thenReturn(Right(None))                                                                                                                                                                                                      │
│     when(reportDAO.save(any(classOf[ReportEntity]))).thenReturn(Right(savedReport))                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│     val eitherResult = reportService.createReport(userIdString)                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                             │
│     eitherResult match {                                                                                                                                                                                                                                                    │
│       case Left(_) => fail("Should have success return")                                                                                                                                                                                                                    │
│       case Right(report) =>                                                                                                                                                                                                                                                 │
│         assertNotNull(report)                                                                                                                                                                                                                                               │
│         assertEquals(savedReport.id, report.id)                                                                                                                                                                                                                             │
│         assertEquals(reportContent, report.content)                                                                                                                                                                                                                         │
│     }                                                                                                                                                                                                                                                                       │
│   }                                                                                                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│   @Test                                                                                                                                                                                                                                                                     │
│   def shouldUpdateExistingReport(): Unit = {                                                                                                                                                                                                                                │
│     val userId = UUID.randomUUID()                                                                                                                                                                                                                                          │
│     val userIdString = userId.toString                                                                                                                                                                                                                                      │
│     val user = UserEntity(                                                                                                                                                                                                                                                  │
│       id = userId,                                                                                                                                                                                                                                                          │
│       ssn = "encrypted-ssn",                                                                                                                                                                                                                                                │
│       status = UserEntity.activeStatus                                                                                                                                                                                                                                      │
│     )                                                                                                                                                                                                                                                                       │
│     val existingReport = ReportEntity(                                                                                                                                                                                                                                      │
│       id = UUID.randomUUID(),                                                                                                                                                                                                                                               │
│       userId = userId,                                                                                                                                                                                                                                                      │
│       content = "Old content"                                                                                                                                                                                                                                               │
│     )                                                                                                                                                                                                                                                                       │
│     val newContent = "New generated content"                                                                                                                                                                                                                                │
│     val updatedReport = existingReport.copy(content = newContent)                                                                                                                                                                                                           │
│     val reportService = createReportService()                                                                                                                                                                                                                               │
│                                                                                                                                                                                                                                                                             │
│     when(userService.findUserByUserId(userIdString)).thenReturn(Right(Some(user)))                                                                                                                                                                                          │
│     when(reportClient.generateReport()).thenReturn(Right(newContent))                                                                                                                                                                                                       │
│     when(reportDAO.findByUserId(userIdString)).thenReturn(Right(Some(existingReport)))                                                                                                                                                                                      │
│     when(reportDAO.save(any(classOf[ReportEntity]))).thenReturn(Right(updatedReport))                                                                                                                                                                                       │
│                                                                                                                                                                                                                                                                             │
│     val eitherResult = reportService.createReport(userIdString)                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                             │
│     eitherResult match {                                                                                                                                                                                                                                                    │
│       case Left(_) => fail("Should have success return")                                                                                                                                                                                                                    │
│       case Right(report) =>                                                                                                                                                                                                                                                 │
│         assertNotNull(report)                                                                                                                                                                                                                                               │
│         assertEquals(newContent, report.content)                                                                                                                                                                                                                            │
│         assertEquals(existingReport.id, report.id)                                                                                                                                                                                                                          │
│     }                                                                                                                                                                                                                                                                       │
│   }                                                                                                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│   @Test                                                                                                                                                                                                                                                                     │
│   def shouldReturnErrorWhenUserNotFound(): Unit = {                                                                                                                                                                                                                         │
│     val userIdString = UUID.randomUUID().toString                                                                                                                                                                                                                           │
│     val reportService = createReportService()                                                                                                                                                                                                                               │
│                                                                                                                                                                                                                                                                             │
│     when(userService.findUserByUserId(userIdString)).thenReturn(Right(None))                                                                                                                                                                                                │
│                                                                                                                                                                                                                                                                             │
│     val eitherResult = reportService.createReport(userIdString)                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                             │
│     eitherResult match {                                                                                                                                                                                                                                                    │
│       case Left(error) =>                                                                                                                                                                                                                                                   │
│         assertTrue(error.isInstanceOf[NotFoundErrorDTO])                                                                                                                                                                                                                    │
│         assertEquals("Not found user", error.getMessage)                                                                                                                                                                                                                    │
│       case Right(_) => fail("Should have returned error")                                                                                                                                                                                                                   │
│     }                                                                                                                                                                                                                                                                       │
│   }                                                                                                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│   @Test                                                                                                                                                                                                                                                                     │
│   def shouldReturnErrorWhenUserIsNotActive(): Unit = {                                                                                                                                                                                                                      │
│     val userId = UUID.randomUUID()                                                                                                                                                                                                                                          │
│     val userIdString = userId.toString                                                                                                                                                                                                                                      │
│     val inactiveUser = UserEntity(                                                                                                                                                                                                                                          │
│       id = userId,                                                                                                                                                                                                                                                          │
│       ssn = "encrypted-ssn",                                                                                                                                                                                                                                                │
│       status = UserEntity.inactiveStatus                                                                                                                                                                                                                                    │
│     )                                                                                                                                                                                                                                                                       │
│     val reportService = createReportService()                                                                                                                                                                                                                               │
│                                                                                                                                                                                                                                                                             │
│     when(userService.findUserByUserId(userIdString)).thenReturn(Right(Some(inactiveUser)))                                                                                                                                                                                  │
│                                                                                                                                                                                                                                                                             │
│     val eitherResult = reportService.createReport(userIdString)                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                             │
│     eitherResult match {                                                                                                                                                                                                                                                    │
│       case Left(error) =>                                                                                                                                                                                                                                                   │
│         assertTrue(error.isInstanceOf[BadRequestErrorDTO])                                                                                                                                                                                                                  │
│         assertEquals("User not active to create report", error.getMessage)                                                                                                                                                                                                  │
│       case Right(_) => fail("Should have returned error")                                                                                                                                                                                                                   │
│     }                                                                                                                                                                                                                                                                       │
│   }                                                                                                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│   @Test                                                                                                                                                                                                                                                                     │
│   def shouldReturnErrorWhenFindUserByUserIdFails(): Unit = {                                                                                                                                                                                                                │
│     val userIdString = UUID.randomUUID().toString                                                                                                                                                                                                                           │
│     val reportService = createReportService()                                                                                                                                                                                                                               │
│                                                                                                                                                                                                                                                                             │
│     when(userService.findUserByUserId(userIdString)).thenReturn(Left(InternalErrorDTO("Database error")))                                                                                                                                                                   │
│                                                                                                                                                                                                                                                                             │
│     val eitherResult = reportService.createReport(userIdString)                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                             │
│     eitherResult match {                                                                                                                                                                                                                                                    │
│       case Left(error) =>                                                                                                                                                                                                                                                   │
│         assertTrue(error.isInstanceOf[InternalErrorDTO])                                                                                                                                                                                                                    │
│         assertEquals("Database error", error.getMessage)                                                                                                                                                                                                                    │
│       case Right(_) => fail("Should have returned error")                                                                                                                                                                                                                   │
│     }                                                                                                                                                                                                                                                                       │
│   }                                                                                                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│   @Test                                                                                                                                                                                                                                                                     │
│   def shouldReturnErrorWhenGenerateReportFails(): Unit = {                                                                                                                                                                                                                  │
│     val userId = UUID.randomUUID()                                                                                                                                                                                                                                          │
│     val userIdString = userId.toString                                                                                                                                                                                                                                      │
│     val user = UserEntity(                                                                                                                                                                                                                                                  │
│       id = userId,                                                                                                                                                                                                                                                          │
│       ssn = "encrypted-ssn",                                                                                                                                                                                                                                                │
│       status = UserEntity.activeStatus                                                                                                                                                                                                                                      │
│     )                                                                                                                                                                                                                                                                       │
│     val reportService = createReportService()                                                                                                                                                                                                                               │
│                                                                                                                                                                                                                                                                             │
│     when(userService.findUserByUserId(userIdString)).thenReturn(Right(Some(user)))                                                                                                                                                                                          │
│     when(reportClient.generateReport()).thenReturn(Left(InternalErrorDTO("Report generation failed")))                                                                                                                                                                      │
│                                                                                                                                                                                                                                                                             │
│     val eitherResult = reportService.createReport(userIdString)                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                             │
│     eitherResult match {                                                                                                                                                                                                                                                    │
│       case Left(error) =>                                                                                                                                                                                                                                                   │
│         assertTrue(error.isInstanceOf[InternalErrorDTO])                                                                                                                                                                                                                    │
│         assertEquals("Report generation failed", error.getMessage)                                                                                                                                                                                                          │
│       case Right(_) => fail("Should have returned error")                                                                                                                                                                                                                   │
│     }                                                                                                                                                                                                                                                                       │
│   }                                                                                                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│   @Test                                                                                                                                                                                                                                                                     │
│   def shouldReturnErrorWhenFindByUserIdFails(): Unit = {                                                                                                                                                                                                                    │
│     val userId = UUID.randomUUID()                                                                                                                                                                                                                                          │
│     val userIdString = userId.toString                                                                                                                                                                                                                                      │
│     val user = UserEntity(                                                                                                                                                                                                                                                  │
│       id = userId,                                                                                                                                                                                                                                                          │
│       ssn = "encrypted-ssn",                                                                                                                                                                                                                                                │
│       status = UserEntity.activeStatus                                                                                                                                                                                                                                      │
│     )                                                                                                                                                                                                                                                                       │
│     val reportContent = "Generated report content"                                                                                                                                                                                                                          │
│     val reportService = createReportService()                                                                                                                                                                                                                               │
│                                                                                                                                                                                                                                                                             │
│     when(userService.findUserByUserId(userIdString)).thenReturn(Right(Some(user)))                                                                                                                                                                                          │
│     when(reportClient.generateReport()).thenReturn(Right(reportContent))                                                                                                                                                                                                    │
│     when(reportDAO.findByUserId(userIdString)).thenReturn(Left(InternalErrorDTO("Database error")))                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│     val eitherResult = reportService.createReport(userIdString)                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                             │
│     eitherResult match {                                                                                                                                                                                                                                                    │
│       case Left(error) =>                                                                                                                                                                                                                                                   │
│         assertTrue(error.isInstanceOf[InternalErrorDTO])                                                                                                                                                                                                                    │
│         assertEquals("Database error", error.getMessage)                                                                                                                                                                                                                    │
│       case Right(_) => fail("Should have returned error")                                                                                                                                                                                                                   │
│     }                                                                                                                                                                                                                                                                       │
│   }                                                                                                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│   @Test                                                                                                                                                                                                                                                                     │
│   def shouldReturnErrorWhenSaveReportFails(): Unit = {                                                                                                                                                                                                                      │
│     val userId = UUID.randomUUID()                                                                                                                                                                                                                                          │
│     val userIdString = userId.toString                                                                                                                                                                                                                                      │
│     val user = UserEntity(                                                                                                                                                                                                                                                  │
│       id = userId,                                                                                                                                                                                                                                                          │
│       ssn = "encrypted-ssn",                                                                                                                                                                                                                                                │
│       status = UserEntity.activeStatus                                                                                                                                                                                                                                      │
│     )                                                                                                                                                                                                                                                                       │
│     val reportContent = "Generated report content"                                                                                                                                                                                                                          │
│     val reportService = createReportService()                                                                                                                                                                                                                               │
│                                                                                                                                                                                                                                                                             │
│     when(userService.findUserByUserId(userIdString)).thenReturn(Right(Some(user)))                                                                                                                                                                                          │
│     when(reportClient.generateReport()).thenReturn(Right(reportContent))                                                                                                                                                                                                    │
│     when(reportDAO.findByUserId(userIdString)).thenReturn(Right(None))                                                                                                                                                                                                      │
│     when(reportDAO.save(any(classOf[ReportEntity]))).thenReturn(Left(InternalErrorDTO("Save failed")))                                                                                                                                                                      │
│                                                                                                                                                                                                                                                                             │
│     val eitherResult = reportService.createReport(userIdString)                                                                                                                                                                                                             │
│                                                                                                                                                                                                                                                                             │
│     eitherResult match {                                                                                                                                                                                                                                                    │
│       case Left(error) =>                                                                                                                                                                                                                                                   │
│         assertTrue(error.isInstanceOf[InternalErrorDTO])                                                                                                                                                                                                                    │
│         assertEquals("Save failed", error.getMessage)                                                                                                                                                                                                                       │
│       case Right(_) => fail("Should have returned error")                                                                                                                                                                                                                   │
│     }                                                                                                                                                                                                                                                                       │
│   }                                                                                                                                                                                                                                                                         │
│                                                                                                                                                                                                                                                                             │
│   private def createReportService(): ReportService = {                                                                                                                                                                                                                      │
│     val logger = Logger()                                                                                                                                                                                                                                                   │
│     ReportService(logger = logger, userService = userService, reportClient = reportClient, reportDAO = reportDAO)                                                                                                                                                           │
│   }                                                                                                                                                                                                                                                                         │
│ }  
```

## Tests

```bash
[info] Passed: Total 28, Failed 0, Errors 0, Passed 28
[success] Total time: 6 s, completed Oct 30, 2025, 3:57:54 PM
```
