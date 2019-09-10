package com.lodamar.model

import java.io.IOException

import pureconfig.error.ConfigReaderFailures

sealed trait Output {
  def stringOutput: String
}

final case class AddedPlayer(players: Set[Player]) extends Output {
  override def stringOutput: String = s"players: ${players.map(_.name).mkString(", ")}"
}

final case class MovedPlayer(player: Player, roll: DiceRoll, start: String, end: String) extends Output {
  override def stringOutput: String =
    s"${player.name} rolls ${roll.first}, ${roll.second}. ${player.name} moves from $start to $end."
}

final case class PlayerJumpedFromBridge(player: Player, to: Int) extends Output {
  override def stringOutput: String =
    s" ${player.name} jumps to $to."
}

final case class PlayerBounced(player: Player, to: Int) extends Output {
  override def stringOutput: String =
    s" ${player.name} bounces! ${player.name} returns to $to."
}

final case class PlayerMovedAgain(player: Player, to: String) extends Output {
  override def stringOutput: String =
    s" ${player.name} moves again and goes to $to."
}

final case class PlayerPranked(pranked: Player, collision: String, to: String) extends Output {
  override def stringOutput: String =
    s" On $collision there is ${pranked.name}, who returns to $to."
}

final case class PlayerVictory(player: Player) extends Output {
  override def stringOutput: String = s" ${player.name} Wins!!"
}

sealed trait Error     extends Output
sealed trait MoveError extends Error
sealed trait AddError  extends Error

final case class InvalidDiceRoll(roll: DiceRoll) extends MoveError {
  override def stringOutput: String = s"Invalid dice roll ${roll.first}, ${roll.second}"
}

final case class InvalidConfig(failures: ConfigReaderFailures) extends Error {
  override def stringOutput: String = s"Invalid config, failures: ${failures.toList.mkString(", ")}"
}

final case class IOError(e: IOException) extends Error {
  override def stringOutput: String = s"IOException received, cause: ${e.getCause}"
}

final case class PlayerNotFound(name: String) extends MoveError {
  override def stringOutput: String = s"player with name $name not found"
}

final case class AlreadyExistingPlayer(existing: String) extends AddError {
  override def stringOutput: String = s"$existing: already existing player"
}

final case class InvalidPlayerName(name: String) extends AddError {
  override def stringOutput: String = s"Name $name is invalid"
}

case object EmptyPlayerName extends AddError {
  override def stringOutput: String = s"Name cannot be blank"
}

final case class NotParsableDiceRoll(roll: String) extends MoveError {
  override def stringOutput: String = s"Not parsable dice roll: $roll"
}

final case class UnknownCommandReceived(command: String) extends MoveError {
  override def stringOutput: String = s"Unknown command: $command"
}
