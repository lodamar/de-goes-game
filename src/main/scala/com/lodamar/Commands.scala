package com.lodamar

import com.lodamar.model._
import scalaz.zio.random.{ nextInt, Random }
import scalaz.zio.{ UIO, ZIO }

import scala.util.{ Success, Try }

object Commands {
  case class SplitMove(name: String, diceRoll: Option[String])

  def parse(state: State)(command: String): ZIO[Random, Error, Command] = command match {
    case c if c.startsWith("add player ") => ZIO.fromEither(addCommand(c.stripPrefix("add player ")))
    case c if c.startsWith("move ")       => moveCommand(c.stripPrefix("move "), state)
    case _                                => ZIO.fail(UnknownCommandReceived(command))
  }

  def moveCommand(command: String, state: State): ZIO[Random, Error, MovePlayer] = {
    val split = UIO(command.trim.split(" ", 2))
      .flatMap(s => if (s.length == 1) UIO(SplitMove(s(0), None)) else UIO(SplitMove(s(0), Some(s(1)))))

    for {
      s         <- split
      player    <- ZIO.fromEither(player(s.name, state))
      roll      <- dice(s.diceRoll)
      validRoll <- ZIO.fromEither(validateRoll(roll))
    } yield MovePlayer(player, validRoll)
  }

  def addCommand(name: String): Either[Error, AddPlayer] =
    if (name.isEmpty) Left(EmptyPlayerName(name))
    else if (name.trim.contains(" ")) Left(InvalidPlayerName(name))
    else Right(AddPlayer(name))

  def player(name: String, state: State): Either[Error, Player] =
    state.players.find(_.name == name).fold[Either[Error, Player]](Left(PlayerNotFound(name)))(Right(_))

  def dice(roll: Option[String]): ZIO[Random, Error, DiceRoll] =
    roll.fold[ZIO[Random, Error, DiceRoll]](rollDice) { roll =>
      val rolls = roll.split(",").map(_.trim).map(s => Try(s.toInt)).toList
      rolls match {
        case Success(first) :: Success(second) :: Nil => UIO(DiceRoll(first, second))
        case _                                        => ZIO.fail(NotParsableDiceRoll(roll))
      }
    }

  private def validateRoll(roll: DiceRoll): Either[Error, DiceRoll] =
    if (isValid(roll.first) && isValid(roll.second)) Right(roll) else Left(InvalidDiceRoll(roll))

  private def isValid(die: Int) = die <= 6 && die > 0

  private def rollDice: ZIO[Random, Nothing, DiceRoll] =
    for {
      first  <- nextInt(6)
      second <- nextInt(6)
    } yield DiceRoll(first + 1, second + 1)

}
