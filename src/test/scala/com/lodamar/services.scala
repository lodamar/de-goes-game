package com.lodamar

import com.lodamar.service.Random
import com.lodamar.service.Random.Service
import scalaz.zio.{ Ref, UIO }

object services {

  case class RollDiceState(toRoll: List[Int], rolled: List[Int]) {
    def roll: RollDiceState = copy(toRoll.tail, toRoll.head :: rolled)
  }

  class RandomTest(ref: Ref[RollDiceState]) extends Random {
    override val random: Service[Any] = _ => ref.update(_.roll).map(_.rolled.head)
  }

  object UnusedRandom extends Random {
    override val random: Service[Any] = _ => UIO(0)
  }

}
