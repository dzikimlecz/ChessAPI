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
		final int startingRow = currentLocation.getRow();
		final int startingLine = currentLocation.getLine();
		//all possible changes of coordinates for a knight being at the middle of the board.
		byte[][] deltasSets =
				{{-2, 1}, {-1, 2}, {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}};
		for (byte[] deltas: deltasSets) {
			byte rowDelta = deltas[0];
			byte lineDelta = deltas[1];
			int newRow = startingRow + rowDelta;
			int newLine = startingLine + lineDelta;
			if (newLine >= 'a' && newLine <= 'h' && newRow >= 1 && newRow <= 8)
				this.deltas.add(new int[] {newLine - startingLine, newRow - startingRow});
		}
	}
}
