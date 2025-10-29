package com.gabrielspassos.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.{Column, Table}

import java.lang.Long
import java.time.OffsetDateTime
import java.util.UUID
import scala.annotation.meta.field

@Table(name = "users")
case class UserEntity(
  @(Id @field)
  id: UUID = null,
                     
  @(Column @field)
  ssn: String = null,

  @(Column @field)
  status: String,

  @(Column @field)(value = "created_at")
  createdAt: OffsetDateTime = OffsetDateTime.now(),

  @(Column @field)(value = "updated_at")
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
  def isActive: Boolean = UserEntity.activeStatus == status
}

object UserEntity {
  def activeStatus = "ACTIVE"
  def inactiveStatus = "INACTIVE"
}
