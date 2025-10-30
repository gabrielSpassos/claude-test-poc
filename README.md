# Database Migration POC

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
```
- Output: 
```
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
create integration tests for class UserDAO covering happy and unhappy paths and all code branches
```
- Output: 
```
46 +    @Test
47 +    def shouldFindUserById(): Unit = {
48 +      val userToSave = createUser()
49 +      val savedUser = userDAO.save(userToSave).getOrElse(fail("failed to save user"))
50 +      userList += savedUser
51 +  
52 +      val eitherResult = userDAO.findById(savedUser.id)
53 +  
54 +      eitherResult match {
55 +        case Left(_) => fail("should have success return")
56 +        case Right(None) => fail("should have found user")
57 +        case Right(Some(foundUser)) =>
58 +          assertEquals(savedUser.id, foundUser.id)
59 +          assertEquals(savedUser.ssn, foundUser.ssn)
60 +          assertEquals(savedUser.status, foundUser.status)
61 +      }
62 +    }
63 +  
64 +    @Test
65 +    def shouldReturnNoneWhenUserNotFoundById(): Unit = {
66 +      val randomUserId = UUID.randomUUID()
67 +  
68 +      val eitherResult = userDAO.findById(randomUserId)
69 +  
70 +      eitherResult match {
71 +        case Left(_) => fail("should have success return")
72 +        case Right(None) => assertTrue(true)
73 +        case Right(Some(_)) => fail("should not have found user")
74 +      }
75 +    }
76 +  
77 +    @Test
78 +    def shouldFindUserBySsn(): Unit = {
79 +      val userToSave = createUser()
80 +      val savedUser = userDAO.save(userToSave).getOrElse(fail("failed to save user"))
81 +      userList += savedUser
82 +  
83 +      val eitherResult = userDAO.findBySsn(savedUser.ssn)
84 +  
85 +      eitherResult match {
86 +        case Left(_) => fail("should have success return")
87 +        case Right(None) => fail("should have found user")
88 +        case Right(Some(foundUser)) =>
89 +          assertEquals(savedUser.id, foundUser.id)
90 +          assertEquals(savedUser.ssn, foundUser.ssn)
91 +          assertEquals(savedUser.status, foundUser.status)
92 +      }
93 +    }
94 +  
95 +    @Test
96 +    def shouldReturnNoneWhenUserNotFoundBySsn(): Unit = {
97 +      val randomSsn = encryptString(RandomSSNGenerator.generate())
98 +  
99 +      val eitherResult = userDAO.findBySsn(randomSsn)
100 +  
101 +      eitherResult match {
102 +        case Left(_) => fail("should have success return")
103 +        case Right(None) => assertTrue(true)
104 +        case Right(Some(_)) => fail("should not have found user")
105 +      }
106 +    }
107 +  
108 +    @Test
109 +    def shouldSaveMultipleUsers(): Unit = {
110 +      val user1 = createUser()
111 +      val user2 = createUser()
112 +  
113 +      val result1 = userDAO.save(user1)
114 +      val result2 = userDAO.save(user2)
115 +  
116 +      result1 match {
117 +        case Left(_) => fail("failed to save first user")
118 +        case Right(savedUser1) =>
119 +          assertNotNull(savedUser1.id)
120 +          userList += savedUser1
121 +      }
122 +  
123 +      result2 match {
124 +        case Left(_) => fail("failed to save second user")
125 +        case Right(savedUser2) =>
126 +          assertNotNull(savedUser2.id)
127 +          userList += savedUser2
128 +      }
129 +    }
130 +  
131 +    @Test
132 +    def shouldFindSavedUserAfterMultipleSaves(): Unit = {
133 +      val user1 = createUser()
134 +      val user2 = createUser()
135 +      val user3 = createUser()
136 +  
137 +      val savedUser1 = userDAO.save(user1).getOrElse(fail("failed to save user1"))
138 +      val savedUser2 = userDAO.save(user2).getOrElse(fail("failed to save user2"))
139 +      val savedUser3 = userDAO.save(user3).getOrElse(fail("failed to save user3"))
140 +  
141 +      userList += savedUser1
142 +      userList += savedUser2
143 +      userList += savedUser3
144 +  
145 +      val foundUser2 = userDAO.findById(savedUser2.id)
146 +  
147 +      foundUser2 match {
148 +        case Left(_) => fail("should have found user2")
149 +        case Right(None) => fail("should have found user2")
150 +        case Right(Some(user)) =>
151 +          assertEquals(savedUser2.id, user.id)
152 +          assertEquals(savedUser2.ssn, user.ssn)
153 +      }
154 +    }
```

3.
- Input:
```
 verify if there is any service class that needs unit test to implement and create then, covering happy, unhappy and all code branches
```
- Output: 
```
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
