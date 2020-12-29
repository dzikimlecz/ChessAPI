package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Square;

import java.util.List;

/**
 * Interface representing movable element
 */
public interface Movable {
	/**
	 * returns changes of coordinates for all possible moves in bounds of the board for this
	 * instance
	 * @return List of 2-elements arrays of first element being change of the line, and second
	 * change of a row.
	 */
	List<int[]> getMoveDeltas();

	/**
	 * Changes location of piece to square and puts i.
	 * @param square destination of the move
	 */
	void moveTo(Square square);


}
