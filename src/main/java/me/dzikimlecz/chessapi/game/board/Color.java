package me.dzikimlecz.chessapi.game.board;

import org.jetbrains.annotations.Contract;

/**
 * Color of pieces and squares
 */
public enum Color {
	WHITE, BLACK;

	/**
	 * Gets color opposite to the present.
	 * @return {@code BLACK} if the object is {@code WHITE} and {@code WHITE} otherwise
	 */
	@Contract(pure = true)
	public Color opposite() {
		return (this == WHITE) ? BLACK : WHITE;
	}

	public int getPawnStartingRow() {
		return (this == WHITE) ? 2 : 7;
	}

}
