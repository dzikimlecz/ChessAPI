package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;


/**
 * Class representing King chess piece.
 * Not takeable
 * @see Piece
 */

public final class King extends Piece {
	
	public King(Color color, Square startLocation) {
		super(color, startLocation);
	}
	
	@Override
	public String toString() {
		return "K";
	}

	/**
	 * Updates move deltas, called by super after each move.
	 */
	@Override
	protected void updateDeltas() {
		final int startingRow = currentLocation.getRow();
		final char startingLine = currentLocation.getLine();
		for (int rowCursor = startingRow - 1;
		     rowCursor <= 8 && rowCursor <= startingRow + 1; rowCursor++) {
			for (int lineCursor = startingLine - 1;
			     lineCursor <= 'h' && lineCursor <= startingLine + 1; lineCursor++) {
				if (rowCursor < 1 || lineCursor < 'a') continue;
				if (rowCursor != startingRow || lineCursor != startingLine)
					deltas.add(new int[]{rowCursor - startingRow, lineCursor - startingLine});
			}
		}
	}
}
