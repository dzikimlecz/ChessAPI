package me.dzikimlecz.chessapi.game.board;

import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.board.square.Color;
import me.dzikimlecz.chessapi.game.board.square.Square;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

/**
 * Chequerboard of dimensions 8x8 containing squares.
 * @see Square
 * @see ChessPiece
 * @see Piece
 */
public abstract class Board {
	/**
	 * Array of squares being raw form of the board
	 */
	private final Square[][] theBoard;
	private final AtomicReferenceArray<Square[]> board;

	private final BoardState boardState;

	public static Board create() {
		return new DefaultBoard();
	}
	public static Board createEmpty() {
		return null;
	}

	Board() {
		this.theBoard = new Square[8][8];
		board = new AtomicReferenceArray<>(theBoard);
		//initializes all squares of the board
		for (byte row = 0; row < 8; row++)
			for (byte line = 0; line < 8; line++)
				theBoard[row][line] = new Square(line, row, Color.values()[(line + row) % 2]);
		this.boardState = new BoardState(this);
	}

	private void putPieces() {
		//Puts Pawns
		for (char line = 'a'; line <= 'h'; line++) {
			new Pawn(WHITE, square(line, 2));
			new Pawn(BLACK, square(line, 7));
		}

		List.of(square('a', 1), square('h', 1))
				.forEach(square -> new Rook(WHITE, square));
		List.of(square('a', 8), square('h', 8))
				.forEach(square -> new Rook(BLACK, square));
		List.of(square('b', 1), square('g', 1))
				.forEach(square -> new Knight(WHITE, square));
		List.of(square('b', 8), square('g', 8))
				.forEach(square -> new Knight(BLACK, square));
		List.of(square('c', 1), square('f', 1))
				.forEach(square -> new Bishop(WHITE, square));
		List.of(square('c', 8), square('f', 8))
				.forEach(square -> new Bishop(BLACK, square));
		new Queen(WHITE, square('d', 1));
		new Queen(BLACK, square('d', 8));
		new King(WHITE, square('e', 1));
		new King(BLACK, square('e', 8));
	}

	/**
	 * parses from chess notation to coordinates in the array of squares e.g. (a, 1 -> 0, 0)
	 * @param row row of the board (1-8)
	 * @param line line of the board (a-h)
	 * @return array mapping both row and line to ints in closed range from 0 to 7
	 */
	private int[] parseCoords(int row, char line) {
		var parsedLine = line - 'a';
		var parsedRow = 8 - row;
		if (parsedLine < 0 || parsedRow < 0 || parsedLine >= 8 || parsedRow >= 8)
			throw new IllegalArgumentException(
					MessageFormat.format("Illegal coordinates: {0}:{1}", line, row)
			);
		return new int[]{parsedRow, parsedLine};
	}

	/**
	 * gets square on specified chess notation
	 * @param line line of the board (a-h)
	 * @param row row of the board (1-8)
	 * @return square on the specified location
	 */
	public Square square(char line, int row) {
		int[] coords = parseCoords(row, line);
		return board.get(coords[0])[coords[1]];
	}

	/**
	 * Gets square lying on the specified change of coordinates from start.
	 * @param startingSquare square from which change of coords is starting
	 * @param delta change of coordinates
	 * @return square lying on the position of starting square location + delta
	 */
	public Square getSquareByDelta(Square startingSquare, int[] delta) {
		if (delta.length != 2) throw new IllegalArgumentException("Illegal format of delta.");
		char line = (char) (startingSquare.line() + delta[0]);
		int row = startingSquare.row() + delta[1];
		return square(line, row);
	}

	/**
	 * Gets list of all squares lying in space between other 2 (exclusive of them).<br>
	 * <strong>May misbehave when used on squares not laying on same row, line, nor diagonal -
	 * It's a misuse.</strong>
	 * @param square start of the space to be returned.
	 * @param square1 end of the space to be returned.
	 * @return all squares between {@code square} and {@code square1}
	 */
	public List<Square> squaresBetween(Square square, Square square1) {
		return squaresBetween(square, square1, false);
	}

	/**
	 * Gets list of all squares lying in space between other 2.<br>
	 * <strong>May misbehave when used on squares not laying on same row, line, nor diagonal -
	 * It's a misuse.</strong>
	 * @param square start of the space to be returned.
	 * @param square1 end of the space to be returned.
	 * @param inclusive are the squares on the edges supposed to be included.
	 * @return all squares between {@code square} and {@code square1}
	 */
	public List<Square> squaresBetween(@NotNull Square square,
	                                   @NotNull Square square1,
	                                   boolean inclusive) {
		if (square.equals(square1)) return List.of(square);
		List<Square> squares = new ArrayList<>();
		final float rowDelta  = Math.signum(square1.row() - square.row());
		final float lineDelta = Math.signum(square1.line() - square.line());
		int row = square.row();
		char line = square.line();
		while (row != square1.row() || line != square1.line()) {
			squares.add(square(line, row));
			row += rowDelta;
			line += lineDelta;
		}

		if (inclusive) squares.add(square1);
		else squares.remove(square);

		return squares;
	}

	/**
	 * @param line line of destination of hypothetical moves
	 * @param row row of destination of hypothetical moves
	 * @param type type of pieces to be moved
	 * @param color color of pieces to be moved
	 * @return list off all pieces that can move to the specified square and are of the specified
	 * type and
	 * color
	 */
	public List<ChessPiece> getPiecesMovingTo(char line,
	                                               int row,
	                                               @Nullable Class<? extends Piece> type,
	                                               @NotNull Color color) {
		return getPiecesMovingTo(square(line, row), type, color);
	}

	/**
	 * @param square destination of hypothetical moves
	 * @param type type of pieces to be moved
	 * @param color color of pieces to be moved
	 * @return list off all pieces that can move to the specified location and are of the specified
	 * type and color
	 */
	public List<ChessPiece> getPiecesMovingTo(Square square,
	                                               @Nullable Class<? extends Piece> type,
	                                               @NotNull Color color) {
		if (square == null) return List.of();
		List<ChessPiece> pieces = new ArrayList<>();
		if (type == Piece.class) {
			List.of(
					Pawn.class,
					Knight.class,
					Bishop.class,
					Rook.class,
					Queen.class,
					King.class
			).forEach(clazz -> pieces.addAll(getPiecesMovingTo(square, clazz, color)));
		} else if (type == null) {
			List.of(
					Knight.class,
					Bishop.class,
					Rook.class,
					Queen.class
			).forEach(clazz -> pieces.addAll(getPiecesMovingTo(square, clazz, color)));
		} else {
			for (int rowCursor = 1; rowCursor <= 8; rowCursor++) {
				for (char lineCursor = 'a'; lineCursor <= 'h'; lineCursor++) {
					var squareCursor = square(lineCursor, rowCursor);
					var piece = squareCursor.piece();
					if (!(piece instanceof Movable)) continue;
					if (piece.color() == color && piece.getClass() == type) {
						for (int[] deltas : ((Movable) piece).moveDeltas()) {
							try {
								if (getSquareByDelta(squareCursor, deltas) == square) {
									pieces.add(piece);
									break;
								}
							} catch(Exception ignored) {}
						}
					}
				}
			}
		}

		return List.copyOf(pieces);
	}

	/**
	 * Gets King of the specified color
	 * @param color color of the ordered King
	 * @return King of player with specified color
	 */
	public abstract King getKing(@NotNull Color color);


	/**
	 * Gets a string representation of the pieces on the board
	 * For an empty square returns "   " (triple space), for a square returns a 1st two letters
	 * of the pieces colour and a chess notation of the piece
	 * @return string representation of pieces on board
	 */
	@Override
	public String toString() {
		var stringBuilder = new StringBuilder();
		for (Square[] squares : theBoard) {
			for (Square square : squares) {
				var piece = square.piece();
				String character = (piece == null) ? "   " :
						piece.color().name().charAt(0) + piece.toString() + ' ';
				stringBuilder.append(character);
			}
			stringBuilder.append('\n');
		}
		return stringBuilder.toString();
	}

	public BoardState getState() {
		return boardState;
	}
}
