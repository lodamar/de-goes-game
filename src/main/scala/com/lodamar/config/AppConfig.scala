package com.lodamar.config

final case class AppConfig(
  gameBoard: GameBoard
)

final case class GameBoard(victory: Int, bridge: Bridge, goose: List[Int])
final case class Bridge(from: Int, to: Int)
