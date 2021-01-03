package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.Color;


/**
 * Class representing bishop chess piece.
 * @see Piece
 * @see Takeable
 */
public final class Rook extends TakeablePiece {
	
	public Rook(Color color, Square startLocation) {
		super(color, startLocation);
	}
	
	@Override
	public String toString() {
		return "R";
	}

	/**
	 * Updates move deltas, called by super after each move.
	 */
	@Override
	protected void updateDeltas() {
		final int startingRow = currentLocation.row();
		final char startingLine = currentLocation.line();
		//squares on the same row
		for (int rowCursor = 1; rowCursor <= 8; rowCursor++)
			if (rowCursor != startingRow)
				deltas.add(new int[] {0, rowCursor - startingRow});
		//squares on the same line
		for (char lineCursor = 'a'; lineCursor <= 'h'; lineCursor++)
			if (lineCursor != startingLine)
				deltas.add(new int[] {lineCursor - startingLine, 0});
	}
}
