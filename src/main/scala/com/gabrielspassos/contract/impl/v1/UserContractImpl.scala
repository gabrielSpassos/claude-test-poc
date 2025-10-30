package com.gabrielspassos.contract.impl.v1

import com.gabrielspassos.contracts.v1
import com.gabrielspassos.contracts.v1.request.UserRequest
import com.gabrielspassos.contracts.v1.response.UserResponse
import com.gabrielspassos.exception.HttpException
import com.gabrielspassos.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserContractImpl @Autowired(private val userService: UserService) extends v1.UserContract {

  override def createUser(userRequest: UserRequest): UserResponse = {
    userService.createUser(userRequest) match {
      case Left(error) =>
        throw HttpException(message = error.getMessage, httpStatus = error.getStatus)
      case Right(entity) =>
        val response = UserResponse()
        response.setUserId(entity.id.toString)
        response
    }
  }

}
