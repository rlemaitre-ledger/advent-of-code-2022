package adventofcode

import scala.annotation.nowarn
import scala.annotation.targetName
import scala.collection.mutable
import scala.collection.mutable.Map as MutableMap

object Day12 extends AdventOfCodeBase[Int, Int]("day12.txt") {
  opaque type LineNumber = Int
  object LineNumber {
    def apply(n: Int): LineNumber = n
  }
  opaque type ColumnNumber = Int
  object ColumnNumber {
    def apply(n: Int): ColumnNumber = n
  }
  enum CellType {
    case Start, Normal, End
  }
  final case class Altitude(height: Char, cellType: CellType) {
    @targetName("minus")
    def -(other: Altitude): Int = height - other.height
  }
  object Altitude {
    def apply(c: Char): Altitude = c match
      case 'S' => Altitude('a', CellType.Start)
      case 'E' => Altitude('z', CellType.End)
      case _   => Altitude(c, CellType.Normal)
  }

  override def part1(lines: List[String]): Int = parse(lines).minDistance

  override def part2(lines: List[String]): Int = ???

  def parse(lines: List[String]): HeightMap =
    HeightMap(
      lines.zipWithIndex.map { case (str, x) =>
        str.zipWithIndex.map { case (alt, y) => Cell(Coordinates(x, y), Altitude(alt)) }.toList
      }
    )

  final case class HeightMap(cells: List[List[Cell]]) {
    private val linesCount                            = cells.size
    private val colsCount                             = cells.map(_.size).max
    private val byCoordinates: Map[Coordinates, Cell] = cells.flatten.map(cell => cell.position -> cell).toMap
    val start: Cell                                   = cells.flatten.find(_.altitude.cellType == CellType.Start).get
    val end: Cell                                     = cells.flatten.find(_.altitude.cellType == CellType.End).get
    def isValid(coordinates: Coordinates): Boolean =
      (0 until linesCount).contains(coordinates.x) && (0 until colsCount).contains(coordinates.y)
    def toGraph: Graph = Graph(adjacency)
    def adjacency: Map[Cell, List[Edge]] = {
      val adjacencyMap = MutableMap.empty[Cell, List[Edge]]
      cells.flatten.foreach { cell =>
        cell.position.neighbours
          .filter(isValid)
          .map(byCoordinates.apply)
          .filter(_.isAccessibleFrom(cell))
          .map(to => Edge(cell, to))
          .foreach { edge =>
            adjacencyMap.updateWith(cell) {
              case None        => Some(List(edge))
              case Some(value) => Some(value :+ edge)
            }
          }
      }
      adjacencyMap.toMap
    }
    def minDistance: Int = toGraph.distance(start, end)
  }
  final case class Edge(from: Cell, to: Cell) {
    val weight: Int = 1
  }
  final case class Graph(adjacency: Map[Cell, List[Edge]]) {
    def shortestPathsFrom(start: Cell): ShortestPaths = {
      ShortestPaths.from(start, adjacency)
    }
    def distance(start: Cell, end: Cell): Int = shortestPathsFrom(start).distanceTo(end)
  }
  object Graph {
    val empty: Graph = Graph(Map.empty)
  }
  case class Path(weight: Int, predecessor: Option[Cell])
  case class ShortestPaths(from: Cell, directPaths: Map[Cell, Path]) {
    def distanceTo(cell: Cell): Int = directPaths(cell).weight
  }
  object ShortestPaths {
    @nowarn
    def from(from: Cell, adjacency: Map[Cell, List[Edge]]): ShortestPaths = {
      val distanceTo = MutableMap.empty[Cell, Path]
      adjacency.keys.foreach(cell => distanceTo.put(cell, Path(Int.MaxValue, None)))
      distanceTo.put(from, Path(0, None))
      val sortByWeight: Ordering[Path] = (p1, p2) => p1.weight.compareTo(p2.weight)
      val queue                        = mutable.PriorityQueue[Path](Path(0, Some(from)))(sortByWeight)
      while (queue.nonEmpty) {
        val p     = queue.dequeue()
        val edges = adjacency.getOrElse(p.predecessor.get, List.empty)
        edges.foreach { e =>
          val previousWeight = distanceTo(e.from)
          distanceTo.get(e.to) match
            case Some(path) if path.weight <= previousWeight.weight + e.weight => path
            case _ => {
              val path = Path(previousWeight.weight + e.weight, Some(e.from))
              distanceTo.put(e.to, path)
              if (!queue.exists(_.predecessor.contains(e.to))) {
                queue.enqueue(Path(path.weight, Some(e.to)))
              }
            }
        }
      }
      ShortestPaths(from, distanceTo.toMap)
    }
  }
  final case class Cell(position: Coordinates, altitude: Altitude) {
    def isAccessibleFrom(cell: Cell): Boolean =
      position.neighbours.contains(cell.position) && altitude - cell.altitude <= 1
  }
  final case class Coordinates(x: LineNumber, y: ColumnNumber) {
    def neighbours: List[Coordinates] = List(
      Coordinates(x - 1, y),
      Coordinates(x, y - 1),
      Coordinates(x, y + 1),
      Coordinates(x + 1, y)
    )
  }
}