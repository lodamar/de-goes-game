package com.lodamar.service

import zio.ZIO

import scala.util.{Random => SRandom}

trait Random {
  val random: Random.Service[Any]
}

object Random {

  trait Service[R] {
    def nextIntInclusive(n: Int): ZIO[R, Nothing, Int]
  }

  trait Live extends Random {
    override val random: Service[Any] = (n: Int) => ZIO.effectTotal(SRandom.nextInt(n) + 1)
  }

  def nextIntInclusive(n: Int): ZIO[Random, Nothing, Int] = ZIO.accessM(_.random.nextIntInclusive(n))

}
