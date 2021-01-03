package me.dzikimlecz.chessapi.game.board;

import me.dzikimlecz.chessapi.game.board.pieces.King;
import me.dzikimlecz.chessapi.game.board.pieces.Knight;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class BoardState {
	private final Board board;

	protected BoardState(Board board) {
		this.board = board;
	}

	public boolean isSquareOccupied(Square square, Color color) {
		return (square.piece() != null) && (square.piece().color() == color);
	}

	public boolean isSquareAttacked(Square square, Color attackedColor) {
		Color oppositeColor = attackedColor.opposite();
		return board.getPiecesMovingTo(
				square.line(),
				square.row(),
				null,
				oppositeColor
		).stream().anyMatch(
				opponentPiece -> opponentPiece.getClass() == Knight.class
						|| !anyPiecesBetween(square, opponentPiece.square())
		);
	}

	public boolean isPieceDefendingKing(Piece piece) {
		Color color = piece.color();
		King king = board.getKing(color);
		Color oppositeColor = color.opposite();

		List<Piece> opponentPiecesPinningToKing =
				board.getPiecesMovingTo(
						king.square(),
						null,
						oppositeColor
				).stream()
						.filter(opponentPiece -> opponentPiece.getClass() != Knight.class)
						.filter(opponentPiece ->
								        countOfPiecesBetween(
										        opponentPiece.square(),
										        king.square()
								        ) == 1)
						.collect(Collectors.toList());
		List<Piece> attackingOpponentPieces =
				board.getPiecesMovingTo(
						piece.square(),
						null,
						oppositeColor
				).stream()
						.filter(opponentPiece -> opponentPiece.getClass() != Knight.class)
						.filter(opponentPiece -> !anyPiecesBetween(
								piece.square(),
								opponentPiece.square()))
						.collect(Collectors.toList());

		opponentPiecesPinningToKing.retainAll(attackingOpponentPieces);

		return !opponentPiecesPinningToKing.isEmpty();
	}

	public boolean anyPiecesBetween(@NotNull Square square, @NotNull Square square1) {
		return board.squaresBetween(square, square1).stream()
				.anyMatch(square2 -> square2.piece() != null);
	}

	public int countOfPiecesBetween(@NotNull Square square, @NotNull Square square1) {
		return (int) board.squaresBetween(square, square1).stream()
				.filter(square2 -> square2.piece() != null).count();
	}
}
