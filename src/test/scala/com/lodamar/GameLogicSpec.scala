package com.lodamar

import com.lodamar.GameLogic.handleCommand
import com.lodamar.config.{ Bridge, GameBoard }
import com.lodamar.model.{ Box, _ }
import org.scalatest.{ FlatSpecLike, Matchers }

class GameLogicSpec extends FlatSpecLike with Matchers {
  private lazy val playerName   = "A"
  private lazy val player       = Player(playerName, 0)
  private lazy val initialState = State(Set(player), List.empty)

  "Add player command" should "not add already existing player" in {
    val res = handleCommand(initialState, AddPlayer(playerName), gameBoard)
    res shouldBe Left(AlreadyExistingPlayer(playerName))
  }

  it should "add new player" in {
    val newPlayer = "new_player"
    val res       = handleCommand(initialState, AddPlayer(newPlayer), gameBoard)
    val players   = initialState.players + Player(newPlayer, 0)

    res shouldBe Right(initialState.copy(players).copy(outputs = List(AddedPlayer(players))))
  }

  "Move command" should "move player" in {
    val diceRoll      = DiceRoll(1, 4)
    val res           = handleCommand(initialState, MovePlayer(player, diceRoll), gameBoard)
    val updatedPlayer = player.copy(position = 5)

    res shouldBe Right(
      initialState
        .copy(players = Set(updatedPlayer))
        .copy(
          outputs = List(
            MovedPlayer(updatedPlayer, diceRoll, PositionBox(0, Start), PositionBox(updatedPlayer.position, Normal))
          )
        )
    )
  }

  it should "move to victory" in {
    val diceRoll      = DiceRoll(5, 5)
    val res           = handleCommand(initialState, MovePlayer(player, diceRoll), gameBoard)
    val updatedPlayer = player.copy(position = 10)

    res shouldBe Right(
      initialState
        .copy(players = Set.empty)
        .copy(
          outputs = List(
            MovedPlayer(updatedPlayer, diceRoll, PositionBox(0, Start), PositionBox(updatedPlayer.position, Victory)),
            PlayerVictory(updatedPlayer)
          )
        )
    )
  }

  it should "bounce" in {
    val diceRoll      = DiceRoll(6, 6)
    val res           = handleCommand(initialState, MovePlayer(player, diceRoll), gameBoard)
    val updatedPlayer = player.copy(position = 8)

    res shouldBe Right(
      initialState
        .copy(players = Set(updatedPlayer))
        .copy(
          outputs = List(
            MovedPlayer(updatedPlayer.copy(position = 12), diceRoll, PositionBox(0, Start), PositionBox(12, Bounce)),
            PlayerBounced(updatedPlayer, 8)
          )
        )
    )
  }

  it should "use bridge" in {
    val diceRoll      = DiceRoll(1, 5)
    val res           = handleCommand(initialState, MovePlayer(player, diceRoll), gameBoard)
    val updatedPlayer = player.copy(position = 8)

    res shouldBe Right(
      initialState
        .copy(players = Set(updatedPlayer))
        .copy(
          outputs = List(
            MovedPlayer(updatedPlayer.copy(position = 6),
                        diceRoll,
                        PositionBox(0, Start),
                        PositionBox(6, model.Bridge(8))),
            PlayerJumpedFromBridge(updatedPlayer, 8)
          )
        )
    )
  }

  it should "use goose" in {
    val diceRoll      = DiceRoll(1, 1)
    val res           = handleCommand(initialState, MovePlayer(player, diceRoll), gameBoard)
    val updatedPlayer = player.copy(position = 8)

    res shouldBe Right(
      initialState
        .copy(players = Set(updatedPlayer))
        .copy(
          outputs = List(
            MovedPlayer(updatedPlayer.copy(position = 2), diceRoll, PositionBox(0, Start), PositionBox(2, model.Goose)),
            PlayerMovedAgain(updatedPlayer.copy(position = 4), PositionBox(4, model.Goose)),
            PlayerMovedAgain(updatedPlayer.copy(position = 6), PositionBox(6, model.Bridge(8))),
            PlayerJumpedFromBridge(updatedPlayer, 8)
          )
        )
    )
  }

  it should "prank player" in {
    val diceRoll     = DiceRoll(1, 4)
    val secondPlayer = Player("B", 0)
    val res = handleCommand(initialState.copy(initialState.players + secondPlayer),
                            MovePlayer(player, diceRoll),
                            gameBoard).flatMap(res => handleCommand(res, MovePlayer(secondPlayer, diceRoll), gameBoard))

    val updatedPlayer       = player.copy(position = 5)
    val secondUpdatedPlayer = secondPlayer.copy(position = 5)

    res shouldBe Right(
      initialState
        .copy(players = Set(secondUpdatedPlayer, player))
        .copy(
          outputs = List(
            MovedPlayer(updatedPlayer, diceRoll, PositionBox(0, Start), PositionBox(5, Normal)),
            MovedPlayer(secondUpdatedPlayer, diceRoll, PositionBox(0, Start), PositionBox(5, Normal)),
            PlayerPranked(updatedPlayer, PositionBox(5, Normal), PositionBox(0, Start))
          )
        )
    )
  }

  private lazy val gameBoard = GameBoard(10, Bridge(6, 8), List(2, 4))
}
