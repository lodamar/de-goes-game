package com.lodamar

import com.lodamar.Commands._
import com.lodamar.GameLogic._
import com.lodamar.config.{ AppConfig, GameBoard }
import com.lodamar.model.{ Error, IOError, InvalidConfig, State }
import scalaz.zio.console.{ getStrLn, putStrLn, Console }
import scalaz.zio.random.Random
import scalaz.zio.{ App, UIO, ZIO }
//Intellij warns this import as unused but it's needed for implicits
import pureconfig.generic.auto._

object GooseGame extends App {
  override def run(args: List[String]): ZIO[GooseGame.Environment, Nothing, Int] =
    gooseGame.foldM(e => putStrLn(e.stringOutput) *> UIO(1), _ => UIO(2))

  def gooseGame: ZIO[Console with Random, Error, Unit] =
    for {
      config        <- ZIO.fromEither(pureconfig.loadConfig[AppConfig]).mapError(InvalidConfig)
      _             <- putStrLn("Welcome to Goose Game!")
      startingState = State(Set.empty, List.empty)
      _             <- gameLoop(startingState, config.gameBoard)
    } yield ()

  def gameLoop(state: State, gameBoard: GameBoard): ZIO[Console with Random, Nothing, Unit] =
    for {
      commandOrError <- (getStrLn.mapError(IOError) >>= parse(state)).either
      newState       = updateState(state, commandOrError, gameBoard)
      outputs        = newState.outputs.reverse.map(_.stringOutput)
      _              <- putStrLn(outputs.mkString)
      _              <- gameLoop(newState.clearOutputs, gameBoard)
    } yield ()
}
