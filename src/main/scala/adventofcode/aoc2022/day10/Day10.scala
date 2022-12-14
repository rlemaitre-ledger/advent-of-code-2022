package adventofcode.aoc2022.day10

import adventofcode.*
import adventofcode.aoc2022.day10.Day10.*
import adventofcode.aoc2022.day10.Day10.Cycle
import adventofcode.aoc2022.day10.Day10.Instruction
import scala.annotation.targetName
import scala.util.matching.Regex

case class Day10(input: List[Instruction])
    extends Problem[List[Instruction], Int, String](2022, 10, "Cathode-Ray Tube") {
  lazy val executions: List[Execution] =
    input
      .foldLeft((List.empty[Execution], Cycle(0))) { case ((instructions, previousCycle), instruction) =>
        val tick      = previousCycle.next
        val execution = Execution(tick, instruction)
        (instructions :+ execution, execution.endsAt)
      }
      ._1

  override def part1: Int =
    List(20, 60, 100, 140, 180, 220)
      .map(Cycle.apply)
      .map(signal(_))
      .map(_.strength)
      .sum

  override def part2: String = crt('#', '.')

  def crt(text: Char, fill: Char): String = Printer(fill, text).print(display)

  def display: Display =
    (1 to 240)
      .map(Cycle.apply)
      .map(signal)
      .foldLeft(Display.default)((display, signal) => display.draw(signal))

  def registerAt(cycle: Cycle): Register =
    executions
      .filter(_.endsAt.number < cycle.number)
      .foldLeft(Register.init)((register, execution) => execution.instruction.execute(register))

  def signal(cycle: Cycle): Signal =
    Signal(cycle, registerAt(cycle))

}
object Day10 {

  val instance: Day10                                      = Day10(instructions(inputLines("2022/day10.txt")))
  def instructions(lines: List[String]): List[Instruction] = lines.map(Instruction.parse)

  case class Signal(cycle: Cycle, register: Register) {
    val strength: Int = cycle.number * register.value
  }

  enum Instruction(val nbCycles: Int) {
    case Noop           extends Instruction(0)
    case Addx(inc: Int) extends Instruction(1)

    def execute(register: Register): Register = this match
      case Noop      => register
      case Addx(inc) => register + inc
  }

  object Instruction {
    private val pattern: Regex = "addx (.*)".r

    def parse(str: String): Instruction = str match
      case "noop"         => Noop
      case pattern(value) => Addx(value.toInt)
  }

  case class Execution(startsAt: Cycle, instruction: Instruction) {
    val endsAt: Cycle = startsAt + instruction.nbCycles
  }

  case class Cycle(number: Int) {
    def next: Cycle = copy(number = number + 1)

    @targetName("plus")
    def +(inc: Int): Cycle = copy(number = number + inc)
  }

  case class Register(value: Int) {
    @targetName("plus")
    def +(inc: Int): Register = copy(value = value + inc)
  }

  object Register {
    val init: Register = Register(1)
  }

  case class Display(pixels: List[Pixel]) {
    def update(pixel: Pixel, position: Int): Display = copy(pixels = pixels.updated(position, pixel))

    override def toString: String = Printer.default.print(this)

    def pixelPosition(cycle: Cycle): Int = cycle.number - 1

    def spritePositions(register: Register): List[Int] =
      List(register.value - 1, register.value, register.value + 1)

    def draw(signal: Signal): Display =
      val position: Int = pixelPosition(signal.cycle)
      val pixel: Pixel = if (isPixelLit(signal, position)) {
        Pixel.lit
      } else {
        Pixel.dark
      }
      update(pixel, position)

    private def isPixelLit(signal: Signal, position: Int) =
      spritePositions(signal.register).contains(position % 40)
  }
  object Display {
    val default: Display = Display(List.fill(240)(Pixel.dark))
  }

  enum Pixel {
    case lit, dark
  }

  case class Printer(fillCharacter: Char, textCharacter: Char) {
    def print(display: Display): String = display.pixels
      .grouped(40)
      .map(_.map(show).mkString)
      .mkString("\n")

    private def show(pixel: Pixel) = pixel match
      case Pixel.lit  => textCharacter
      case Pixel.dark => fillCharacter
  }

  object Printer {
    val default: Printer = Printer('.', '#')
  }
}
