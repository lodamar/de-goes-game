package com.lodamar

import com.lodamar.config.GameBoard
import com.lodamar.model._

object GameLogic {

  def updateState(currentState: State, commandOrError: Either[Error, Command], gameBoard: GameBoard): State =
    commandOrError.fold(currentState.addOutput, handleCommand(currentState, _, gameBoard))

  def handleCommand(currentState: State, command: Command, gameBoard: GameBoard): State = command match {
    case AddPlayer(newPlayer) if currentState.players.exists(_.name == newPlayer) =>
      currentState.addOutput(AlreadyExistingPlayer(newPlayer))
    case AddPlayer(newPlayer)     => addPlayer(newPlayer, currentState)
    case MovePlayer(player, roll) => movePlayer(player, roll, currentState, gameBoard)
  }

  def addPlayer(newPlayer: String, state: State): State = {
    val updatedState = state.addPlayer(newPlayer)
    updatedState.addOutput(AddedPlayer(updatedState.players))
  }

  def movePlayer(player: Player, roll: DiceRoll, currentState: State, gameBoard: GameBoard): State = {
    val startPos  = player.position
    val sumOfDice = roll.first + roll.second
    val moved     = player.move(_ + sumOfDice)

    val movedState = currentState
      .updatePlayer(moved)
      .addOutput(
        MovedPlayer(moved, roll, nameOf(startPos, gameBoard), boxType(moved.position, gameBoard).name(moved.position))
      )

    additionalRules(moved, gameBoard, movedState, sumOfDice)
  }

  private def additionalRules(player: Player, gameBoard: GameBoard, stateAfterMove: State, sumOfDice: Int): State =
    boxType(player.position, gameBoard) match {
      case Victory => stateAfterMove.copy(players = Set.empty).addOutput(PlayerVictory(player))
      case Bounce =>
        val p = player.move(2 * gameBoard.victory - _)
        additionalRules(p, gameBoard, stateAfterMove.updatePlayer(p).addOutput(PlayerBounced(p, p.position)), sumOfDice)
      case Bridge(to) =>
        val p = player.move(to)
        additionalRules(p,
                        gameBoard,
                        stateAfterMove.updatePlayer(p).addOutput(PlayerJumpedFromBridge(p, to)),
                        sumOfDice)
      case Goose =>
        val p = player.move(_ + sumOfDice)
        additionalRules(p,
                        gameBoard,
                        stateAfterMove.updatePlayer(p).addOutput(PlayerMovedAgain(p, nameOf(p.position, gameBoard))),
                        sumOfDice)
      case Normal =>
        (stateAfterMove.players - player)
          .find(_.position == player.position)
          .fold(stateAfterMove)(
            pr => {
              val starting = player.position - sumOfDice
              stateAfterMove
                .updatePlayer(pr.move(starting))
                .addOutput(
                  PlayerPranked(pr, nameOf(player.position, gameBoard), nameOf(starting, gameBoard))
                )
            }
          )
      case _ => stateAfterMove
    }

  private def boxType(pos: Int, gameBoard: GameBoard): Box =
    if (pos == gameBoard.victory) Victory
    else if (pos == 0) Start
    else if (pos > gameBoard.victory) Bounce
    else if (pos == gameBoard.bridge.from) Bridge(gameBoard.bridge.to)
    else if (gameBoard.goose.contains(pos)) Goose
    else Normal

  private def nameOf(pos: Int, gameBoard: GameBoard): String = boxType(pos, gameBoard).name(pos)

}
