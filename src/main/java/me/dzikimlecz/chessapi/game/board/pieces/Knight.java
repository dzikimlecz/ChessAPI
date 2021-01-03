package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;


/**
 * Class representing bishop chess piece.
 * @see Piece
 * @see Takeable
 */

public final class Knight extends TakeablePiece {
	
	public Knight(Color color, Square startLocation) {
		super(color, startLocation);
	}
	
	@Override
	public String toString() {
		return "N";
	}


	/**
	 * Updates move deltas, called by super after each move.
	 */
	@Override
	protected void updateDeltas() {
		final int startingRow = currentLocation.row();
		final int startingLine = currentLocation.line();
		//all possible changes of coordinates for a knight being at the middle of the board.
		byte[][] deltasSets =
				{{-2, 1}, {-1, 2}, {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}};
		for (byte[] deltas: deltasSets) {
			int lineCursor = startingLine + deltas[0];
			int rowCursor = startingRow + deltas[1];
			if (lineCursor < 'a' || lineCursor > 'h' || rowCursor < 1 || rowCursor > 8) continue;
			this.deltas.add(new int[] {lineCursor - startingLine, rowCursor - startingRow});
		}
	}
}
