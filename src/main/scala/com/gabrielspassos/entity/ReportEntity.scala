package com.gabrielspassos.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.{Column, Table}

import java.lang.Long
import java.time.OffsetDateTime
import java.util.UUID
import scala.annotation.meta.field

@Table(name = "reports")
case class ReportEntity(
  @(Id @field)
  id: UUID = null,

  @(Column @field)(value = "user_id")
  userId: UUID = null,

  @(Column @field)
  content: String,

  @(Column @field)(value = "created_at")
  createdAt: OffsetDateTime = OffsetDateTime.now(),

  @(Column @field)(value = "updated_at")
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
