package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.square.Color;

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
	public Color color() {
		return color;
	}

	/**
	 * Gets location of the ChessPiece being a two-elements array of first element being an
	 * alphabetic character and second being a integer (not character representation of it!)
	 * @return array of location {line, row}
	 */
	public abstract char[] location();

}
