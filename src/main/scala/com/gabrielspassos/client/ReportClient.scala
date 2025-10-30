package com.gabrielspassos.client

import com.gabrielspassos.dto.ErrorDTO
import org.springframework.stereotype.Component

import scala.util.Random

@Component
class ReportClient {
  
  def generateReport(): Either[ErrorDTO, String] = {
    // Simulate report generation
    val reportId = Random().nextInt()
    Right(s"Fake report content with id: $reportId")
  }

}
