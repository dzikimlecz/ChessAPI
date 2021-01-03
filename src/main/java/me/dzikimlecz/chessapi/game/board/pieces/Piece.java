package me.dzikimlecz.chessapi.game.board.pieces;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Abstract representation of chess piece. Containing required data and methods for all of them.</p>
 * <p><strong>NOT INTENDED AS A DEFAULT INTERFACE FOR WORKING OUTSIDE OF THE API.</strong>
 * <br>For that purpose see: {@link ChessPiece}</p>
 * @see ChessPiece
 * @see Movable
 * @see Takeable
 * @see Pawn
 * @see Knight
 * @see Bishop
 * @see Rook
 * @see Queen
 * @see King
 */
public abstract class Piece extends ChessPiece implements Movable {
	/**
	 * Square in which piece is located.
	 */
	protected Square currentLocation;
	/**
	 * List of changes of coordinates for each hypothetically possible move from current location.
	 */
	protected List<int[]> deltas;

	/**
	 * Creates new Pice of specified color and puts it it into specified square
	 * @param color immutable color of the piece.
	 * @param startLocation square, where the piece starts the game.
	 */
	public Piece(Color color, Square startLocation) {
		super(color);
		deltas = new ArrayList<>();
		moveTo(startLocation);
	}

	/**
	 * Changes location of piece to square. Puts itself in it and clears move deltas.
	 * @param square destination of the move.
	 */
	@Override
	public void moveTo(Square square) {
		boolean pieceMoved = square.putPiece(this);
		if (!pieceMoved)
			throw new IllegalArgumentException("Could not move to square: " + square.toString());
		if (currentLocation != null) currentLocation.putPiece(null);
		currentLocation = square;
		deltas.clear();
		updateDeltas();
	}

	/**
	 * Updates move deltas, called after each move.
	 */
	protected abstract void updateDeltas();

	/**
	 * Gets location of the ChessPiece being a two-elements array of first element being an
	 * alphabetic character and second being a integer (not character representation of it!)
	 * @return 2-elements array of location: {line, row}
	 */
	@Override
	public char[] location() {
		return new char[] {currentLocation.line(), (char) currentLocation.row()};
	}

	/**
	 * Gets square being current location of the object.
	 * @return square in which the piece is currently located.
	 */
	public Square square() {
		return currentLocation;
	}

	/**
	 * returns changes of coordinates for all possible moves in bounds of the board for this
	 * instance
	 * @return List of 2-elements arrays of first element being change of the line, and second
	 * change of a row.
	 */
	@Override
	public List<int[]> moveDeltas() {
		return List.copyOf(deltas);
	}
}
