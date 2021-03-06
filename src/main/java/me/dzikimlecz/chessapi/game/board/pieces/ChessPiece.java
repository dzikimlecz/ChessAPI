package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Color;

/**
 * Abstract class being an interface for working with pieces from outside of the chess API
 * @see Piece
 * @see Movable
 */
public abstract class ChessPiece {


	/**
	 * Field representing color of the piece.
	 */
	protected final Color color;

	/**
	 * Default constructor setting the color
	 * @param color color of the ChessPiece instance
	 */
	public ChessPiece(Color color) {
		this.color = color;
	}

	/**
	 * Gets the color of the ChessPiece instance
	 * @return color (black or white)
	 * @see Color
	 */
	public final Color color() {
		return color;
	}

	/**
	 * Gets location of the ChessPiece being a two-elements array of first element being an
	 * alphabetic character and second being a integer (not character representation of it!)
	 * @return array of location {line, row}
	 */
	public abstract char[] location();


	/**
	 * Checks if passed reference is pointing to same instance as this <b>(same as ==)</b>
	 * .<br>
	 * Overridden only to prohibit different implementation in subclasses.
	 * @param o object to be checked.
	 * @return {@code true} if o points to this object, {@code false} otherwise.
	 */
	@Override public final boolean equals(Object o) {
		return super.equals(o);
	}

	/**
	 * Returns default hashcode for this object generated just by calling super.<br>
	 * Overridden only to prohibit different implementation in subclasses.
	 * @return default hashcode generated by {@link Object#hashCode}
	 */
	@Override public final int hashCode() {
		return super.hashCode();
	}
}
