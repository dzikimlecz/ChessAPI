package me.dzikimlecz.chessapi.game.board;

import me.dzikimlecz.chessapi.game.board.pieces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static me.dzikimlecz.chessapi.game.board.Color.BLACK;
import static me.dzikimlecz.chessapi.game.board.Color.WHITE;

/**
 * Chess board of dimensions 8x8 containing squares.
 * @see Square
 * @see ChessPiece
 * @see Piece
 */
public class Board {
	/**
	 * Array of squares being raw form of the board
	 */
	private final Square[][] theBoard;
	/**
	 * White King, constant for a whole game
	 */
	private final King whiteKing;
	/**
	 * Black King, constant for a whole game
	 */
	private final King blackKing;
	private final BoardState boardState;

	/**
	 * Initialises object, and puts pieces into the right squares
	 */
	public Board() {
		this.theBoard = new Square[8][8];
		//initializes all squares of the board
		for (byte row = 0; row < 8; row++)
			for (byte line = 0; line < 8; line++)
				theBoard[row][line] = new Square(line, row, Color.values()[(line + row) % 2]);

		//Puts every piece on its place
		this.putPieces();
		this.whiteKing = (King) square('e', 1).getPiece();
		this.blackKing = (King) square('e', 8).getPiece();
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
		return theBoard[coords[0]][coords[1]];
	}

	/**
	 * Gets square lying on the specified change of coordinates from start.
	 * @param startingSquare square from which change of coords is starting
	 * @param delta change of coordinates
	 * @return square lying on the position of starting square location + delta
	 */
	public Square getSquareByDelta(Square startingSquare, int[] delta) {
		if (delta.length != 2) throw new IllegalArgumentException("Illegal format of delta.");
		char line = (char) (startingSquare.getLine() + delta[0]);
		int row = startingSquare.getRow() + delta[1];
		return square(line, row);
	}

	/**
	 * Gets list of all squares lying in space between other 2 (exclusive of them).<br>
	 * MAY PRODUCE BUGS WHEN SQUARES ARE NOT ON THE SAME LINE OR DIAGONAL - IT'S MISUSE
	 * @param square start of the space to be returned.
	 * @param square1 end of the space to be returned.
	 * @return all squares between {@code square} and {@code square1}
	 */
	public List<Square> squaresBetween(Square square, Square square1) {
		return squaresBetween(square, square1, false);
	}

	/**
	 * Gets list of all squares lying in space between other 2.<br>
	 * MAY PRODUCE BUGS WHEN SQUARES ARE NOT ON THE SAME LINE OR DIAGONAL - IT'S MISUSE
	 * @param square start of the space to be returned.
	 * @param square1 end of the space to be returned.
	 * @param inclusive are the squares on the edges supposed to be included.
	 * @return all squares between {@code square} and {@code square1}
	 */
	public List<Square> squaresBetween(Square square, Square square1, boolean inclusive) {
		int lesserChange = inclusive ? 0 : 1;
		int biggerChange = inclusive ? 1 : 0;
		final int lesserY = Math.min(square.getRow(), square1.getRow()) + lesserChange;
		final int biggerY = Math.max(square.getRow(), square1.getRow()) + biggerChange;
		final char lesserX = (char) (Math.min(square.getLine(), square1.getLine()) + lesserChange);
		final char biggerX = (char) (Math.max(square.getLine(), square1.getLine()) + biggerChange);

		List<Square> squares = new ArrayList<>();
		for (int y = lesserY; y < biggerY; y++)
			for (char x = lesserX; x < biggerX; x++)
				squares.add(this.square(x, y));

		return List.copyOf(squares);
	}

	/**
	 * @param square destination of hypothetical moves
	 * @param type type of pieces to be moved
	 * @param color color of pieces to be moved
	 * @return list off all pieces that can move to the specified square and are of the specified
	 * type and
	 * color
	 */
	public List<? extends Piece> getPiecesMovingTo(Square square,
	                                               @Nullable Class<? extends Piece> type,
	                                               @NotNull Color color) {
		return getPiecesMovingTo(square.getLine(), square.getRow(), type, color);
	}

	/**
	 * @param line line of destination of hypothetical moves
	 * @param row row of destination of hypothetical moves
	 * @param type type of pieces to be moved
	 * @param color color of pieces to be moved
	 * @return list off all pieces that can move to the specified location and are of the specified
	 * type and color
	 */
	public List<? extends Piece> getPiecesMovingTo(char line,
	                                               int row,
	                                               @Nullable Class<? extends Piece> type,
	                                               @NotNull Color color) {
		List<Piece> pieces = new ArrayList<>();
		if (type == Piece.class) {
			List.of(
					Pawn.class,
					Knight.class,
					Bishop.class,
					Rook.class,
					Queen.class,
					King.class
			).forEach(clazz -> pieces.addAll(getPiecesMovingTo(line, row, clazz, color)));
		} else if (type == null) {
			List.of(
					Knight.class,
					Bishop.class,
					Rook.class,
					Queen.class
			).forEach(clazz -> pieces.addAll(getPiecesMovingTo(line, row, clazz, color)));
		} else {
			Arrays.stream(theBoard).forEach(boardRow -> Arrays.stream(boardRow)
					.map(Square::getPiece)
					.filter(Objects::nonNull)
					.filter(piece -> piece.getColor() == color && piece.getClass() == type)
					.filter(piece -> piece.getMoveDeltas().stream().anyMatch(set -> {
						        boolean lineMatch = piece.getLocation()[0] + set[0] == line;
						        boolean rowMatch = piece.getLocation()[1] + set[1] == row;
						        return lineMatch && rowMatch;
					        })
					).forEach(pieces::add));
		}

		return List.copyOf(pieces);
	}

	/**
	 * Gets King of the specified color
	 * @param color color of the ordered King
	 * @return King of player with specified color
	 */
	public King getKing(Color color) {
		return (color == WHITE) ? whiteKing : blackKing;
	}


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
				var piece = square.getPiece();
				String character = (piece == null) ? "   " :
						piece.getColor().name().charAt(0) + piece.toString() + ' ';
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
