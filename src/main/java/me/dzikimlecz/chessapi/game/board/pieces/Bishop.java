package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;


/**
 * Class representing bishop chess piece.
 * @see Piece
 * @see Takeable
 */
public final class Bishop extends TakeablePiece {

	public Bishop(Color color, Square startLocation) {
		super(color, startLocation);
	}




	@Override
	public String toString() {
		return "B";
	}

	/**
	 * Updates move deltas, called by super after each move.
	 */
	@Override
	protected void updateDeltas() {
		final int startingRow = currentLocation.row();
		final char startingLine = currentLocation.line();

		//changes of the coords between squares on diagonals.
		byte[][] diagonalDeltasSets = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
		for (byte[] diagonalDeltas : diagonalDeltasSets) {
			final byte rowDelta = diagonalDeltas[0];
			final byte lineDelta = diagonalDeltas[1];
			int rowCursor = startingRow;
			char lineCursor = startingLine;

			//iterates through all of squares on the diagonal
			while (rowCursor >= 1 && rowCursor <= 8 &&
					lineCursor >= 'a' && lineCursor <= 'h') {
				if (lineCursor != startingLine || rowCursor != startingRow)
					deltas.add(new int[] {lineCursor - startingLine, rowCursor - startingRow});
				rowCursor += rowDelta;
				lineCursor += lineDelta;
			}
		}
	}
}
