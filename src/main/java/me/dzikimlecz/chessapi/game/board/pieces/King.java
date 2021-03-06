package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.square.Color;
import me.dzikimlecz.chessapi.game.board.square.Square;


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
		var row = currentLocation.row();
		var line = currentLocation.line();
		for (int rowDelta = -1; rowDelta <= 1; rowDelta++) {
			var destinationRow = row + rowDelta;
			if (destinationRow < 1 || destinationRow > 8) continue;
			for (int lineDelta = -1; lineDelta <= 1; lineDelta++) {
				char destinationLine = (char) (line + lineDelta);
				if (destinationLine < 'a' || destinationLine > 'h') continue;
				deltas.add(new int[]{lineDelta, rowDelta});
			}
		}
	}
}
