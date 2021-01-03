package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.Color;


/**
 * Class representing bishop chess piece.
 * @see Piece
 * @see Takeable
 */
public final class Queen extends TakeablePiece {

	public Queen(Color color, Square startLocation) {
		super(color, startLocation);
	}
	
	@Override
	public String toString() {
		return "Q";
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
			if(rowCursor != startingRow)
				deltas.add(new int[]{0, rowCursor - startingRow});
		//squares on the same line
		for (char lineCursor = 'a'; lineCursor <= 'h'; lineCursor++)
			if(lineCursor != startingLine)
				deltas.add(new int[]{lineCursor - startingLine, 0});

		//changes of the coords between squares on diagonals.
		byte[][] diagonalDeltasSets = {{1,1}, {1,-1}, {-1, 1}, {-1, -1}};
		for (byte[] diagonalDeltas : diagonalDeltasSets) {
			byte rowDelta = diagonalDeltas[0];
			byte lineDelta = diagonalDeltas[1];
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
