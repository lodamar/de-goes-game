package com.lodamar

import com.lodamar.Commands._
import com.lodamar.GameLogic._
import com.lodamar.config.{ AppConfig, GameBoard }
import com.lodamar.model.Output.output
import com.lodamar.model.{ Error, IOError, InvalidConfig, Output, State }
import com.lodamar.service.Random
import pureconfig.loadConfig
import scalaz.zio.console.{ getStrLn, putStrLn, Console }
import scalaz.zio.{ App, UIO, ZIO }
//Intellij warns this import as unused but it's needed for implicits
import pureconfig.generic.auto._

object GooseGame extends App {
  override def run(args: List[String]): ZIO[GooseGame.Environment, Nothing, Int] =
    gooseGame.foldM(e => putStrLn(output(e)) *> UIO(1), _ => UIO(2)).provide(new Console.Live with Random.Live)

  def gooseGame: ZIO[Console with Random, Error, Unit] =
    for {
      config        <- ZIO.fromEither(loadConfig[AppConfig]).mapError(InvalidConfig)
      _             <- putStrLn("Welcome to Goose Game!")
      startingState = State(Set.empty, List.empty)
      _             <- gameLoop(startingState, config.gameBoard)
    } yield ()

  def gameLoop(state: State, gameBoard: GameBoard): ZIO[Console with Random, Nothing, Unit] =
    for {
      newState <- processCommand(state, gameBoard)
      outputs  = newState.outputs.map(output)
      _        <- putStrLn(outputs.mkString)
      _        <- gameLoop(newState.clearOutputs, gameBoard)
    } yield ()

  def processCommand(state: State, gameBoard: GameBoard): ZIO[Random with Console, Nothing, State] =
    (for {
      command  <- getStrLn.mapError(IOError) >>= parse(state)
      newState <- ZIO.fromEither(handleCommand(state, command, gameBoard))
    } yield newState).catchAll(e => UIO(state.addOutput(e)))
}
