package com.gabrielspassos.dao.repository

import com.gabrielspassos.entity.ReportEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import java.lang.Long
import java.util.{Optional, UUID}

@Repository
trait ReportRepository extends CrudRepository[ReportEntity, UUID] {
  
  def findByUserId(userId: String): Optional[ReportEntity]

}
