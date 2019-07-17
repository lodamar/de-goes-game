package com.lodamar

import com.lodamar.Commands.parse
import com.lodamar.model.{Command, State}
import org.scalatest.{FlatSpecLike, Matchers}
import scalaz.zio.ZIO
import scalaz.zio.random.Random

class CommandsTest extends FlatSpecLike with Matchers {
  "Parser" should "return unknown commands" in {
    val uc: ZIO[Random, model.Error, Command] = parse(State(Set.empty, List.empty))("unknown command")


  }
}

