package me.dzikimlecz.chessapi.game.board.square;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A single square on the checkerboard.
 */
public class Square {
	/**
	 * Line on which square is located
	 */
	private final char line;
	/**
	 * Row on which square is located
	 */
	private final int row;
	/**
	 * Color of the square
	 */
	public final Color color;
	/**
	 * Piece staying in the square, if there isn't one present its value is {@code null}
	 */
	private @Nullable ChessPiece containedPiece;

	/**
	 * Initializes square and sets location converting raw coordinates
	 * @param line raw line (n -> n + 'a')
	 * @param row raw row (n -> n + 1)
	 * @param color color of the square
	 */
	public Square(int line, int row, @NotNull Color color) {
		this.line = (char) (line + 'a');
		this.row = 8 - row;
		this.color = color;
	}

	/**
	 * Puts piece (or removes if {@code null} was passed) onto the square.
	 * @param piece object to be put onto the object. If null removes piece.
	 * @return false if {@code piece} and {@code containedPiece} are both equal or both not equal
	 * to {@code null}. Otherwise returns false and not changes contained piece.
	 */
	public boolean putPiece(@Nullable ChessPiece piece) {
		if ((containedPiece != null) == (piece != null)) return false;
		containedPiece = piece;
		return true;
	}

	/**
	 * Gets piece lying on the square
	 * @return piece lying on the square if present, null otherwise.
	 */
	@Nullable
	public ChessPiece piece() {
		return containedPiece;
	}

	/**
	 * Gets line of the square (a-h)
	 * @return line on which square is located
	 */
	public char line() {
		return line;
	}

	/**
	 * Gets row of the square (1-8)
	 * @return row on which square is located
	 */
	public int row() {
		return row;
	}

	@Override
	public String toString() {
		return String.format("[%c%d]", line, row);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Square)) return false;
		Square square = (Square) o;
		return line == square.line && row == square.row && color == square.color && Objects.equals(
				containedPiece, square.containedPiece);
	}

	@Override
	public int hashCode() {
		return Objects.hash(line, row, color, containedPiece);
	}
}
