package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.Color;


/**
 * Class representing bishop chess piece.
 * @see Piece
 * @see Takeable
 */
public final class Pawn extends TakeablePiece {

	private int movesCount;

	public Pawn(Color color, Square startLocation) {
		super(color, startLocation);
	}
	
	@Override
	public String toString() {
		return "P";
	}

	/**
	 * Updates move deltas, called by super after each move.
	 */
	@Override
	protected void updateDeltas() {
		int rowDelta = (color == Color.WHITE) ? 1 : -1;
		char colorStartLine = (color == Color.WHITE) ? 'b' : 'g';
		//normal move one square upfront
		deltas.add(new int[]{0, rowDelta});
		//taking moves
		deltas.add(new int[]{1, rowDelta});
		deltas.add(new int[]{-1, rowDelta});
		//possible first move (2 squares upfront)
		if (currentLocation.getLine() == colorStartLine)
			deltas.add(new int[]{0, 2 * rowDelta});
	}

	/**
	 * Changes location of piece to square. Puts itself in it and clears move deltas.
	 * @param square destination of the move.
	 */
	@Override
	public void moveTo(Square square) {
		super.moveTo(square);
		movesCount++;
	}

	public int movesCount() {
		return movesCount;
	}
}
