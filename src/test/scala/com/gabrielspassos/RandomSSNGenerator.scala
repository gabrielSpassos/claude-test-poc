package com.gabrielspassos

import scala.annotation.tailrec
import scala.util.Random

object RandomSSNGenerator {

  private val random = new Random()

  def generate(): String = {
    val area = randomArea()
    val group = randomGroup()
    val serial = randomSerial()

    f"$area%03d-$group%02d-$serial%04d"
  }

  @tailrec
  private def randomArea(): Int = {
    val area = random.between(1, 900)
    if (area == 666) randomArea() else area
  }

  private def randomGroup(): Int =
    random.between(1, 100)

  private def randomSerial(): Int =
    random.between(1, 10000)
}
