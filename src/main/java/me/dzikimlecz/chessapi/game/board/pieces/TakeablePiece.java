package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;

/**
 * Class representing Takeable instance of Piece
 */
public abstract class TakeablePiece extends Piece implements Takeable {

	/**
	 * Creates new Pice of specified color and location
	 *
	 * @param color         immutable color of the piece.
	 * @param startLocation square, where the pieces starts the game.
	 */
	public TakeablePiece(Color color, Square startLocation) {
		super(color, startLocation);
	}

	/**
	 * Method removing piece from the board.
	 * @see Takeable
	 */
	@Override
	public void beTaken() {
		currentLocation.putPiece(null);
		currentLocation = null;
		deltas = null;
	}
}
