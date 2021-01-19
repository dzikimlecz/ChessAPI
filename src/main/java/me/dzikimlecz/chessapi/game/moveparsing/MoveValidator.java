package me.dzikimlecz.chessapi.game.moveparsing;

import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.BoardState;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.movestoring.GameState;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

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
		Map<Piece, Square> moveVariations = moveData.getVariations();

		if (moveData.doingCastling()) return validateCastling(moveData);

		for (Iterator<Piece> iterator = moveVariations.keySet().iterator(); iterator.hasNext();) {
			Piece piece = iterator.next();
			int status = validStatus(piece, moveVariations.get(piece));
			if (status == 0) iterator.remove();
			else if (status < 0) moveData.setToFurtherCheck(true);
		}
		var values = new ArrayList<>(moveVariations.values());
		boolean unambigious = values.stream()
				.anyMatch(square -> values.indexOf(square) != values.lastIndexOf(square));
		if (unambigious) moveVariations.clear();
		return moveData;
	}

	private int validStatus(Piece piece, Square square) {

		if (boardState.isSquareOccupied(square, color)) return 0;

		if (!(piece instanceof Knight)
				&& boardState.anyPiecesBetween(piece.square(), square))  return 0;

		if (!(piece instanceof King) && (boardState.isPieceDefendingKing(piece) ||
				boardState.isKingAttacked(color))) return 0;

		if (piece instanceof Pawn) return validatePawnMove((Pawn) piece, square);

		if (piece instanceof King
				&& boardState.isSquareAttacked(square, color))  return 0;

		return 1;
	}

	private int validatePawnMove(Pawn pawn, Square square) {
		final int rowDelta = (color == Color.WHITE) ? 1 : -1;
		final int opponentsFirstFreeRow = (color == Color.BLACK) ? 3 : 6;
		var pawnSquare = pawn.square();
		var squareBefore = board.square(square.line(), square.row() - rowDelta);
		//taking move:
		if (pawnSquare.line() != square.line()) {
			Piece piece = square.piece();
			if(piece instanceof King) return 0;
			if (piece == null) {
				var pieceBefore = squareBefore.piece();
				boolean valid = pieceBefore instanceof Pawn
						&& pieceBefore.color() == pawn.color().opposite()
						&& square.row() == opponentsFirstFreeRow;
				return valid ? -1 : 0;
			}
			return 1;
		}
		//
		else if (Math.abs(pawnSquare.row() - square.row()) == 2 && squareBefore.piece() != null)
			return 0;
		return 1;
	}

	private MoveData validateCastling(MoveData moveData) {
		Map<Piece, Square> map = moveData.getVariations();
		var pieces = map.keySet().toArray();
		King king = (King) ((pieces[0] instanceof King) ? pieces[0] : pieces[1]);
		Rook rook = (Rook) ((king == pieces[0]) ? pieces[1] : pieces[0]);
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
