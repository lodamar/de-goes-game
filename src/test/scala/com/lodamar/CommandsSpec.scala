package com.lodamar

import com.lodamar.Commands.parse
import com.lodamar.model._
import com.lodamar.services._
import com.lodamar.testutil.TestUtil._
import org.scalatest.{ FlatSpecLike, Matchers }
import zio.Ref

class CommandsSpec extends FlatSpecLike with Matchers {

  "Parser" should "return unknown commands" in {
    val command = "unknown command"
    val res     = unsafeRun(UnusedRandom)(parse(State.empty)(command))

    assertError(res)(_ shouldBe UnknownCommandReceived(command))
  }

  "Add command" should "refuse empty player names" in {
    val command = "add player "
    val res     = unsafeRun(UnusedRandom)(parse(State.empty)(command))

    assertError(res)(_ shouldBe EmptyPlayerName)

    val commandWithSpaces = "add player    "
    val resWithSpaces     = unsafeRun(UnusedRandom)(parse(State.empty)(commandWithSpaces))

    assertError(resWithSpaces)(_ shouldBe EmptyPlayerName)
  }

  it should "refuse player name with spaces" in {
    val command = "add player Name Surname"
    val res     = unsafeRun(UnusedRandom)(parse(State.empty)(command))

    assertError(res)(_ shouldBe InvalidPlayerName("Name Surname"))
  }

  it should "parse correct add player" in {
    val command = "add player Name"
    val res     = unsafeRun(UnusedRandom)(parse(State.empty)(command))

    assertValue(res)(_ shouldBe AddPlayer("Name"))
  }

  "Move command" should "refuse unparsable dice rolls" in {
    val notParsableRolls = List("error", "error,error", "6, error", "error, 6", "a6,6a", " ,", ",")

    notParsableRolls.foreach(c => {
      val res = unsafeRun(UnusedRandom)(parse(State.empty.addPlayer("A"))(s"move A $c"))
      assertError(res)(_ shouldBe NotParsableDiceRoll(c))
    })
  }

  it should "refuse invalid dice rolls" in {
    val invalidRolls = List("6,7", "10,0", "-1, -1", "100, -100", "0,0", "1,0", "0,1")

    invalidRolls.foreach(c => {
      val res   = unsafeRun(UnusedRandom)(parse(State.empty.addPlayer("A"))(s"move A $c"))
      val rolls = c.split(",")
      assertError(res)(_ shouldBe InvalidDiceRoll(DiceRoll(rolls(0).trim.toInt, rolls(1).trim.toInt)))
    })
  }

  it should "refuse move for not existing player name" in {
    val command = "move NotExisting"
    val res     = unsafeRun(UnusedRandom)(parse(State.empty)(command))

    assertError(res)(_ shouldBe PlayerNotFound("NotExisting"))
  }

  it should "parse correct move command with dice rolls" in {
    val command = "move A 4,5"
    val res     = unsafeRun(UnusedRandom)(parse(State.empty.addPlayer("A"))(command))

    assertValue(res)(_ shouldBe MovePlayer(Player("A", 0), DiceRoll(4, 5)))
  }

  it should "parse correct move command without dice rolls" in {
    val command = "move A"

    val test = for {
      ref      <- Ref.make(RollDiceState(List(1, 4), List.empty))
      randTest = new RandomTest(ref)
      r        <- parse(State.empty.addPlayer("A"))(command).provide(randTest)
      s        <- ref.get
    } yield (r, s)

    val res = unsafeRun(test)

    assertValue(res) {
      case (cmd, diceState) =>
        cmd shouldBe MovePlayer(Player("A", 0), DiceRoll(1, 4))
        diceState.toRoll shouldBe empty
        diceState.rolled shouldBe List(4, 1)
    }
  }
}
