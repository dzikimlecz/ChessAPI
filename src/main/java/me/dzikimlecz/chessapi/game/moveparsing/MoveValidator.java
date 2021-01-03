package me.dzikimlecz.chessapi.game.moveparsing;

import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.BoardState;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.movestoring.GamesData;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;

import java.util.Iterator;
import java.util.Map;

public class MoveValidator implements IMoveValidator {
	private final GamesData gamesData;

	private Board board;
	private Color color;
	private BoardState boardState;

	public MoveValidator(GamesData gamesData) {
		this.gamesData = gamesData;
	}

	@Override
	public MoveData validate(MoveData moveData) {
		board = gamesData.board();
		color = gamesData.color();
		boardState = board.getState();
		Map<Piece, Square> moveVariations = moveData.getVariations();

		if (moveData.doingCastling()) return validateCastling(moveData);

		for (Iterator<Piece> iterator = moveVariations.keySet().iterator(); iterator.hasNext(); ) {
			Piece piece = iterator.next();
			int status = validStatus(piece, moveVariations.get(piece));
			if (status == 0) iterator.remove();
			else if (status < 0) moveData.setToFurtherCheck(true);
		}
		return moveData;
	}

	private int validStatus(Piece piece, Square square) {

		if (boardState.isSquareOccupied(square, color)) return 0;

		if (!(piece instanceof Knight)
				&& boardState.anyPiecesBetween(piece.square(), square))  return 0;

		if (!(piece instanceof King)
				&& boardState.isPieceDefendingKing(piece))  return 0;

		if (piece instanceof Pawn) return validatePawnMove((Pawn) piece, square);

		if (piece instanceof King
				&& boardState.isSquareAttacked(square, color))  return 0;

		return 1;
	}

	private int validatePawnMove(Pawn pawn, Square square) {
		final Square pawnSquare = pawn.square();
		final int rowDelta = (color == Color.WHITE) ? 1 : -1;
		final int opponentsFirstFreeRow = (color == Color.BLACK) ? 3 : 6;
		final Square squareBefore = board.square(square.line(), square.row() - rowDelta);
		//taking move:
		if (pawnSquare.line() != square.line()) {
			Piece piece = square.piece();
			if(piece instanceof King) return 0;
			if (piece == null) {
				boolean valid = squareBefore.piece() instanceof Pawn
						&& squareBefore.piece().color() == pawn.color().opposite()
						&& square.row() == opponentsFirstFreeRow;
				return valid ? -1 : 0;
			}
			return 1;
		}
		else if (Math.abs(pawnSquare.row() - square.row()) == 2) {
			boolean valid = squareBefore.piece() == null;
			if (!valid) return 0;
		}
		return 1;
	}

	private MoveData validateCastling(MoveData moveData) {
		Map<Piece, Square> map = moveData.getVariations();
		var pieces = map.keySet().toArray();
		King king = (King) pieces[0];
		Rook rook = (Rook) pieces[1];
		Square kingSquare = king.square();
		Square rookSquare = rook.square();
		boolean invalid = board.squaresBetween(kingSquare, map.get(king), true)
				.stream().anyMatch(square -> boardState.isSquareAttacked(square, color))
				|| boardState.anyPiecesBetween(kingSquare, rookSquare);
		if (invalid) map.clear();
		else moveData.setToFurtherCheck(true);
		return moveData;
	}
}
