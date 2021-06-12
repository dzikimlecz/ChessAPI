package me.dzikimlecz.chessapi.game.moveparsing;

import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.BoardState;
import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.square.Square;
import me.dzikimlecz.chessapi.game.movestoring.GameState;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;

import java.util.ArrayList;

import static me.dzikimlecz.chessapi.game.moveparsing.MoveValidator.ValidationResult.*;

public class MoveValidator implements IMoveValidator {
	private GameState gameState;

	private Board board;
	private Color color;
	private BoardState boardState;

	@Override
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	@Override
	public MoveData validate(MoveData moveData) {
		board = gameState.board();
		color = gameState.color();
		boardState = board.getState();
		var moveVariations = moveData.getVariations();

		if (moveData.doingCastling()) return validateCastling(moveData);

		for (var iterator = moveVariations.keySet().iterator(); iterator.hasNext();) {
			var piece = iterator.next();
			var status = validStatus(piece, moveVariations.get(piece));
			if (status == INVALID) iterator.remove();
			else if (status == CHECK) moveData.setToFurtherCheck(true);
		}
		var values = new ArrayList<>(moveVariations.values());
		// checks if there are multiple pieces supposed to move to the same square after the validation
		boolean ambiguous = values.stream()
				.anyMatch(square -> values.indexOf(square) != values.lastIndexOf(square));
		if (ambiguous) moveVariations.clear();
		return moveData;
	}

	private ValidationResult validStatus(ChessPiece piece, Square square) {

		if (boardState.isSquareOccupied(square, color)) return INVALID;

		if (!(piece instanceof Knight)
				&& boardState.anyPiecesBetween(
						board.square(piece.location()[0], piece.location()[1]), square))
			return INVALID;

		if (!(piece instanceof King) && (boardState.isPieceDefendingKing(piece) ||
				boardState.isKingAttacked(color))) return INVALID;

		if (piece instanceof Pawn) return validatePawnMove((Pawn) piece, square);

		if (piece instanceof King
				&& boardState.isSquareAttacked(square, color))  return INVALID;

		return VALID;
	}

	private ValidationResult validatePawnMove(Pawn pawn, Square square) {
		final int rowDelta = (color == Color.WHITE) ? 1 : -1;
		final int opponentsFirstFreeRow = (color == Color.BLACK) ? 3 : 6;
		var pawnSquare = pawn.square();
		var squareBefore = board.square(square.line(), square.row() - rowDelta);
		//taking move:
		if (pawnSquare.line() != square.line()) {
			var piece = square.piece();
			if(piece instanceof King) return VALID;
			if (piece == null) {
				var pieceBefore = squareBefore.piece();
				boolean valid = pieceBefore instanceof Pawn
						&& pieceBefore.color() == pawn.color().opposite()
						&& square.row() == opponentsFirstFreeRow;
				return valid ? CHECK : INVALID;
			}
			return VALID;
		}
		//
		else if (Math.abs(pawnSquare.row() - square.row()) == 2 && squareBefore.piece() != null)
			return INVALID;
		return VALID;
	}

	private MoveData validateCastling(MoveData moveData) {
		var map = moveData.getVariations();
		var pieces = map.keySet().toArray();
		King king = (King) ((pieces[0] instanceof King) ? pieces[0] : pieces[1]);
		Rook rook = (Rook) ((king == pieces[0]) ? pieces[1] : pieces[0]);
		var kingSquare = king.square();
		var rookSquare = rook.square();
		boolean invalid = board.squaresBetween(kingSquare, map.get(king), true)
				.stream().anyMatch(square -> boardState.isSquareAttacked(square, color))
				|| boardState.anyPiecesBetween(kingSquare, rookSquare);
		if (invalid) map.clear();
		else moveData.setToFurtherCheck(true);
		return moveData;
	}

	protected enum ValidationResult {
		VALID,
		INVALID,
		CHECK,
	}
}
