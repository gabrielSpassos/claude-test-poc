package com.gabrielspassos.dao.repository

import com.gabrielspassos.entity.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import java.lang.Long
import java.util
import java.util.{Optional, UUID}

@Repository
trait UserRepository extends CrudRepository[UserEntity, UUID] {
  
  def findBySsn(ssn: String): Optional[UserEntity]

}
