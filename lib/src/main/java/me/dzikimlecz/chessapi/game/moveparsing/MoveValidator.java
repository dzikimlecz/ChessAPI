package me.dzikimlecz.chessapi.game.moveparsing;

import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.BoardState;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.movestoring.GamesData;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;

import java.util.LinkedHashMap;
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
		board = gamesData.getBoard();
		color = gamesData.getColor();
		boardState = board.getState();
		Map<Piece, Square> moveVariations = moveData.getVariations();
		if (moveData.doingCastling()) return validateCastling(moveData);
		for (Piece piece : moveVariations.keySet()) {
			int status = validStatus(piece, moveVariations.get(piece));
			if (status == 0) moveVariations.remove(piece);
			else if (status == -1) moveData.setToFurtherCheck(true);
		}
		return moveData;
	}

	private int validStatus(Piece piece, Square square) {

		if(boardState.isSquareOccupied(square, color)) return 0;

		if (!(piece instanceof Knight)
				&& boardState.anyPiecesBetween(piece.getSquare(), square))  return 0;

		if (!(piece instanceof King)
				&& boardState.isPieceDefendingKing(piece))  return 0;

		if (piece instanceof Pawn) return validatePawnMove((Pawn) piece, square);

		if (piece instanceof King
				&& boardState.isSquareAttacked(square, color))  return 0;

		return 1;
	}



	private int validatePawnMove(Pawn pawn, Square square) {
		final Square pawnSquare = pawn.getSquare();
		final int yDelta = (color == Color.WHITE) ? -1 : 1;
		final int startRow = (color == Color.WHITE) ? 2 : 7;
		final int opponentsFirstFreeRow = (color == Color.BLACK) ? 3 : 6;
		final Square squareBefore = board.getSquare(square.getLine(), square.getRow() + yDelta);
		if (pawnSquare.getLine() != square.getLine()) {
			Piece piece = square.getPiece();
			if(piece instanceof King) return 0;
			if (piece == null) {
				boolean valid = squareBefore.getPiece() instanceof Pawn
						&& square.getRow() == opponentsFirstFreeRow;
				return valid ? -1 : 0;
			}
		}
		else if (Math.abs(pawnSquare.getRow() - square.getRow()) == 2) {
			boolean valid = squareBefore.getPiece() == null && pawn.getLocation()[1] == startRow;
			if (!valid) return 0;
		}
		return 1;
	}

	private MoveData validateCastling(MoveData moveData) {
		LinkedHashMap<Piece, Square> map = (LinkedHashMap<Piece, Square>) moveData.getVariations();
		King king = (King) map.keySet().toArray()[0];
		Rook rook = (Rook) map.keySet().toArray()[1];
		Square kingSquare = king.getSquare();
		Square rookSquare = rook.getSquare();
		boolean invalid =
				board.squaresBetween(kingSquare, map.get(king), true)
						.stream().anyMatch(square -> boardState.isSquareAttacked(square, color))
				|| boardState.anyPiecesBetween(kingSquare, rookSquare);

		if (invalid) map.clear();
		else moveData.setToFurtherCheck(true);
		return moveData;
	}
}
