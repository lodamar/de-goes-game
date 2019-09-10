package com.lodamar.testutil

import org.scalatest.{ Assertion, TestSuite }
import scalaz.zio.internal.PlatformLive
import scalaz.zio.{ DefaultRuntime, Runtime, ZIO }

object TestUtil extends TestSuite {

  def unsafeRun[R, E, A](r: R)(zio: ZIO[R, E, A]): Either[E, A] = Runtime(r, PlatformLive.Default).unsafeRun(zio.either)

  def unsafeRun[E, A](zio: ZIO[Any, E, A]): Either[E, A] = {
    val runtime = new DefaultRuntime {}
    runtime.unsafeRun(zio.either)
  }

  def assertError[E, A](either: Either[E, A])(validate: E => Assertion): Assertion = either match {
    case Left(e) => validate(e)
    case a       => fail(s"Unexpected successful value: $a")
  }

  def assertValue[E, A](either: Either[E, A])(validate: A => Assertion): Assertion = either match {
    case Right(a) => validate(a)
    case e        => fail(s"Unexpected error value: $e")
  }

}
