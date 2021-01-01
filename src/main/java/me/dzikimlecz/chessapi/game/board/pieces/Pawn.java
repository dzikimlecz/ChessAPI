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
		final int startingRow = currentLocation.getRow();
		final char startingLine = currentLocation.getLine();
		int lineDelta = (color == Color.WHITE) ? 1 : -1;
		char colorStartLine = (color == Color.WHITE) ? 'b' : 'g';
		//normal move one square upfront
		deltas.add(new int[]{lineDelta, 0});
		//taking moves
		deltas.add(new int[]{lineDelta, 1});
		deltas.add(new int[]{lineDelta, -1});
		//possible first move (2 squares upfront
		if (colorStartLine == startingLine)
			deltas.add(new int[]{2 * lineDelta, 0});
	}

	/**
	 * Changes location of piece to square. Puts itself in it and clears move deltas.
	 * <br> All subclasses are supposed to override it and call super as the first action!
	 *
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
