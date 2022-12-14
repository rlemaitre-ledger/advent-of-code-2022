package adventofcode.aoc2022.day02

import adventofcode.AoCTest
import adventofcode.Mode
import adventofcode.aoc2022.day02.Day02
import adventofcode.aoc2022.day02.Day02.*
import org.scalacheck.Gen
import org.scalacheck.Gen.*
import org.scalacheck.Prop.forAll

class Day02Test extends AoCTest {
  val lines: String = """A Y
                |B X
                |C Z""".stripMargin

  val instance: Day02 = Day02(input)
  test("parse part1") {
    assertEquals(
      instance.part1Rounds(input),
      List(
        Round(Move.Rock, Move.Paper, Result.Win),
        Round(Move.Paper, Move.Rock, Result.Loss),
        Round(Move.Scissors, Move.Scissors, Result.Draw)
      )
    )
  }

  property("Two equals move makes draw") {
    val movePairGen: Gen[(Move, Move)] =
      for {
        opponent <- oneOf(Move.Rock, Move.Paper, Move.Scissors)
        me       <- oneOf(Move.Rock, Move.Paper, Move.Scissors)
      } yield (opponent, me)
    forAll(movePairGen) { (opponent: Move, me: Move) =>
      Result.from(opponent, me) match
        case Result.Win  => Result.from(me, opponent) == Result.Loss
        case Result.Draw => me == opponent
        case Result.Loss => Result.from(me, opponent) == Result.Win
    }
  }

  property("move from opponent") {
    val moveResultGen: Gen[(Move, Result)] =
      for {
        opponent <- oneOf(Move.Rock, Move.Paper, Move.Scissors)
        result   <- oneOf(Result.Win, Result.Loss, Result.Draw)
      } yield (opponent, result)
    forAll(moveResultGen) { (opponent: Move, result: Result) =>
      Result.from(opponent, Move.from(opponent, result)) == result
    }
  }

  test("Illegal input") {
    ('A' to 'W').foreach { c =>
      intercept[IllegalArgumentException] {
        Move.fromMe(c.toString)
      }
    }
    ('A' to 'W').foreach { c =>
      intercept[IllegalArgumentException] {
        Result.fromInput(c.toString)
      }
    }
    ('D' to 'Z').foreach { c =>
      intercept[IllegalArgumentException] {
        Move.fromOpponent(c.toString)
      }
    }
  }

  test("compute score part 1") {
    assertEquals(instance.part1, 15)
  }
  test("parse part2") {
    assertEquals(
      instance.part2Rounds(input),
      List(
        Round(Move.Rock, Move.Rock, Result.Draw),
        Round(Move.Paper, Move.Rock, Result.Loss),
        Round(Move.Scissors, Move.Rock, Result.Win)
      )
    )
  }

  test("compute score part 2") {
    assertEquals(instance.part2, 12)
  }
  test("real part 1") {
    assertEquals(Day02.instance.run(Mode.Part1), 10310)
  }
  test("real part 1") {
    assertEquals(Day02.instance.run(Mode.Part2), 14859)
  }

}
