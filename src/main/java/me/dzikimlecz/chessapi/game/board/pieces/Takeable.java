package me.dzikimlecz.chessapi.game.board.pieces;

/**
 * Interface for pieces, that can be taken by another pieces.
 */
public interface Takeable {

	/**
	 * Method removing piece from the board.
	 */
	void beTaken();
}
