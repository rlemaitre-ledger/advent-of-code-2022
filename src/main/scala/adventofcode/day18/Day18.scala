package adventofcode.day18

import adventofcode.AdventOfCodeBase
import adventofcode.day18.Day18.*
import adventofcode.inputLines

final case class Day18(input: Set[Coordinates3D]) extends AdventOfCodeBase[Set[Coordinates3D], Int, Int] {
  override def part1: Int = input.toSeq.map(cube => cube.visibleSides(input)).sum

  override def part2: Int = {
    val airCubes = Box.enclosing(input).outsideCubes
    input.toSeq.map(_.neighbours.count(airCubes.contains)).sum
  }
}
object Day18 {
  val instance: Day18 = Day18(Coordinates3D.fromLines(inputLines("day18.txt")))
}
