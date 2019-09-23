package com.lodamar.model

import java.io.IOException

import pureconfig.error.ConfigReaderFailures

sealed trait Output

final case class AddedPlayer(players: Set[Player])                                       extends Output
final case class MovedPlayer(player: Player, roll: DiceRoll, start: String, end: String) extends Output
final case class PlayerJumpedFromBridge(player: Player, to: Int)                         extends Output
final case class PlayerBounced(player: Player, to: Int)                                  extends Output
final case class PlayerMovedAgain(player: Player, to: String)                            extends Output
final case class PlayerPranked(pranked: Player, collision: String, to: String)           extends Output
final case class PlayerVictory(player: Player)                                           extends Output

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
      s"${player.name} rolls ${roll.first}, ${roll.second}. ${player.name} moves from $start to $end."
    case PlayerJumpedFromBridge(player, to)    => s" ${player.name} jumps to $to."
    case PlayerBounced(player, to)             => s" ${player.name} bounces! ${player.name} returns to $to."
    case PlayerMovedAgain(player, to)          => s" ${player.name} moves again and goes to $to."
    case PlayerPranked(pranked, collision, to) => s" On $collision there is ${pranked.name}, who returns to $to."
    case PlayerVictory(player)                 => s" ${player.name} Wins!!"
    case InvalidDiceRoll(roll)                 => s"Invalid dice roll ${roll.first}, ${roll.second}"
    case PlayerNotFound(name)                  => s"player with name $name not found"
    case NotParsableDiceRoll(roll)             => s"Not parsable dice roll: $roll"
    case UnknownCommandReceived(command)       => s"Unknown command: $command"
    case AlreadyExistingPlayer(existing)       => s"$existing: already existing player"
    case InvalidPlayerName(name)               => s"Name $name is invalid"
    case EmptyPlayerName                       => "Name cannot be blank"
    case InvalidConfig(failures)               => s"Invalid config, failures: ${failures.toList.mkString(", ")}"
    case IOError(e)                            => s"IOException received, cause: ${e.getCause}"
  }
}
