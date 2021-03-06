package chess

import ornicar.scalalib.test.OrnicarValidationMatchers
import org.specs2.mutable.Specification
import org.specs2.matcher.Matcher

import format.Visual

trait ChessTest
    extends Specification
    with OrnicarValidationMatchers {

  implicit def stringToBoard(str: String): Board = Visual << str

  implicit def stringToBoardBuilder(str: String) = new {

    def chess960: Board = makeBoard(str, Variant.Chess960)
  }

  implicit def stringToSituationBuilder(str: String) = new {

    def as(color: Color): Situation = Situation(Visual << str, color)
  }

  implicit def richGame(game: Game) = new {

    def as(color: Color): Game = game.copy(player = color)

    def playMoves(moves: (Pos, Pos)*): Valid[Game] = playMoveList(moves)

    def playMoveList(moves: Iterable[(Pos, Pos)]): Valid[Game] =
      moves.foldLeft(success(game): Valid[Game]) { (vg, move) ⇒
        vg flatMap { g ⇒ g(move._1, move._2) map (_._1) }
      }

    def playMove(
      orig: Pos,
      dest: Pos,
      promotion: Option[PromotableRole] = None): Valid[Game] =
      game.apply(orig, dest, promotion) map (_._1)

    def withClock(c: Clock) = game.copy(clock = Some(c))
  }

  def makeBoard(pieces: (Pos, Piece)*): Board = 
    Board(pieces toMap, History(), Variant.Standard)

  def makeBoard(str: String, variant: Variant) = 
    Visual << str withVariant variant

  def makeBoard: Board = Board init Variant.Standard

  def makeEmptyBoard: Board = Board empty Variant.Standard

  def bePoss(poss: Pos*): Matcher[Option[Iterable[Pos]]] = beSome.like {
    case p ⇒ sortPoss(p.toList) must_== sortPoss(poss.toList)
  }

  def makeGame: Game = Game(makeBoard)

  def bePoss(board: Board, visual: String): Matcher[Option[Iterable[Pos]]] = beSome.like {
    case p ⇒ Visual.addNewLines(Visual.>>|(board, Map(p -> 'x'))) must_== visual
  }

  def beBoard(visual: String): Matcher[Valid[Board]] = beSuccess.like {
    case b ⇒ b.visual must_== (Visual << visual).visual
  }

  def beSituation(visual: String): Matcher[Valid[Situation]] = beSuccess.like {
    case s ⇒ s.board.visual must_== (Visual << visual).visual
  }

  def beGame(visual: String): Matcher[Valid[Game]] = beSuccess.like {
    case g ⇒ g.board.visual must_== (Visual << visual).visual
  }

  def sortPoss(poss: Seq[Pos]): Seq[Pos] = poss sortBy (_.toString)

  def pieceMoves(piece: Piece, pos: Pos): Option[List[Pos]] =
    (makeEmptyBoard place piece at pos).toOption flatMap { b ⇒
      b actorAt pos map (_.destinations)
    }
}
