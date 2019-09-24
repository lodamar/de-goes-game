package com.lodamar.model

final case class Player(name: String, position: Int) {
  def move(pos: Int => Int): Player = copy(position = pos(position))
  def move(pos: Int): Player        = copy(position = pos)
}
final case class DiceRoll(first: Int, second: Int)
final case class State(players: Set[Player], outputs: List[Output]) {
  def addPlayer(name: String): State      = copy(players = players + Player(name, 0))
  def updatePlayer(player: Player): State = copy(players = players.filterNot(_.name == player.name) + player)
  def addOutput(output: Output): State    = copy(outputs = outputs :+ output)
  def clearOutputs: State                 = copy(outputs = List.empty)
}

object State {
  lazy val empty = State(Set.empty, List.empty)
}

sealed trait Command
final case class AddPlayer(name: String)                        extends Command
final case class MovePlayer(player: Player, diceRoll: DiceRoll) extends Command

sealed trait Box
case object Victory              extends Box
case object Bounce               extends Box
case object Normal               extends Box
case object Start                extends Box
case object Goose                extends Box
final case class Bridge(to: Int) extends Box
