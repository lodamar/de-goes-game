package com.lodamar

import scalaz.zio.{Chunk, ZIO}
import scalaz.zio.random.Random

object services {

  class RandomTest extends Random.Service[Any] {
    override val nextBoolean: ZIO[Any, Nothing, Boolean] = _

    override def nextBytes(length: Int): ZIO[Any, Nothing, Chunk[Byte]] = ???

    override val nextDouble: ZIO[Any, Nothing, Double] = _
    override val nextFloat: ZIO[Any, Nothing, Float] = _
    override val nextGaussian: ZIO[Any, Nothing, Double] = _

    override def nextInt(n: Int): ZIO[Any, Nothing, Int] = ???

    override val nextInt: ZIO[Any, Nothing, Int] = _
    override val nextLong: ZIO[Any, Nothing, Long] = _
    override val nextPrintableChar: ZIO[Any, Nothing, Char] = _

    override def nextString(length: Int): ZIO[Any, Nothing, String] = ???
  }

}
