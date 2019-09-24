package com.lodamar.model

import java.io.IOException

import pureconfig.error.ConfigReaderFailures

sealed trait Output

final case class AddedPlayer(players: Set[Player])                                                 extends Output
final case class MovedPlayer(player: Player, roll: DiceRoll, start: PositionBox, end: PositionBox) extends Output
final case class PlayerJumpedFromBridge(player: Player, to: Int)                                   extends Output
final case class PlayerBounced(player: Player, to: Int)                                            extends Output
final case class PlayerMovedAgain(player: Player, to: PositionBox)                                 extends Output
final case class PlayerPranked(pranked: Player, collision: PositionBox, to: PositionBox)           extends Output
final case class PlayerVictory(player: Player)                                                     extends Output

final case class PositionBox(pos: Int, box: Box)

sealed trait Error     extends Output
sealed trait MoveError extends Error
sealed trait AddError  extends Error

final case class InvalidConfig(failures: ConfigReaderFailures) extends Error
final case class IOError(e: IOException)                       extends Error

final case class AlreadyExistingPlayer(existing: String) extends AddError
final case class InvalidPlayerName(name: String)         extends AddError
case object EmptyPlayerName                              extends AddError

final case class InvalidDiceRoll(roll: DiceRoll)         extends MoveError
final case class NotParsableDiceRoll(roll: String)       extends MoveError
final case class PlayerNotFound(name: String)            extends MoveError
final case class UnknownCommandReceived(command: String) extends MoveError

object Output {

  def output(output: Output): String = output match {
    case AddedPlayer(players) => s"players: ${players.map(_.name).mkString(", ")}"
    case MovedPlayer(player, roll, start, end) =>
      s"${player.name} rolls ${roll.first}, ${roll.second}. ${player.name} moves from ${nameOf(start.box, start.pos)} to ${nameOf(end.box, end.pos)}."
    case PlayerJumpedFromBridge(player, to) => s" ${player.name} jumps to $to."
    case PlayerBounced(player, to)          => s" ${player.name} bounces! ${player.name} returns to $to."
    case PlayerMovedAgain(player, to)       => s" ${player.name} moves again and goes to ${nameOf(to.box, to.pos)}."
    case PlayerPranked(pranked, collision, to) =>
      s" On ${nameOf(collision.box, collision.pos)} there is ${pranked.name}, who returns to ${nameOf(to.box, to.pos)}."
    case PlayerVictory(player)           => s" ${player.name} Wins!!"
    case InvalidDiceRoll(roll)           => s"Invalid dice roll ${roll.first}, ${roll.second}"
    case PlayerNotFound(name)            => s"player with name $name not found"
    case NotParsableDiceRoll(roll)       => s"Not parsable dice roll: $roll"
    case UnknownCommandReceived(command) => s"Unknown command: $command"
    case AlreadyExistingPlayer(existing) => s"$existing: already existing player"
    case InvalidPlayerName(name)         => s"Name $name is invalid"
    case EmptyPlayerName                 => "Name cannot be blank"
    case InvalidConfig(failures)         => s"Invalid config, failures: ${failures.toList.mkString(", ")}"
    case IOError(e)                      => s"IOException received, cause: ${e.getCause}"
  }

  private def nameOf(box: Box, pos: Int) = box match {
    case Start     => "Start"
    case Goose     => s"$pos, The Goose"
    case Bridge(_) => "The Bridge"
    case _         => pos.toString
  }

}
