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
		final int row = currentLocation.row();
		final char line = currentLocation.line();
		int rowDelta = (color == Color.WHITE) ? 1 : -1;
		int colorStartRow = (color == Color.WHITE) ? 2 : 7;
		int destinationRow = row + rowDelta;
		if (destinationRow >= 1 && destinationRow <= 8) {
			//normal move one square upfront
			deltas.add(new int[] {0, rowDelta});
			//taking moves
			if (line < 'h')
				deltas.add(new int[] { 1, rowDelta});
			if (line > 'a')
				deltas.add(new int[] {-1, rowDelta});
			//possible first move (2 squares upfront
			if (colorStartRow == row)
				deltas.add(new int[]{0, 2 * rowDelta});
		}
	}

	/**
	 * Changes location of piece to square. Puts itself in it and clears move deltas.
	 * <br> All subclasses are supposed to override it and call super as the first action!
	 *
	 * @param square destination of the move.
	 */
	@Override
	public void moveTo(Square square) {
		if (currentLocation != null) movesCount++;
		super.moveTo(square);
	}

	public int movesCount() {
		return movesCount;
	}
}
