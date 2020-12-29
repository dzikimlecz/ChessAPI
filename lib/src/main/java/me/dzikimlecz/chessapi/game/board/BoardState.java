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
		return (square.getPiece() == null) || (square.getPiece().getColor() != color);
	}

	public boolean isSquareAttacked(Square square, Color attackedColor) {
		Color oppositeColor = attackedColor.opposite();
		return board.getPiecesMovingTo(
				square.getLine(),
				square.getRow(),
				null,
				oppositeColor
		).stream().anyMatch(
				opponentPiece -> opponentPiece.getClass() == Knight.class
						|| !anyPiecesBetween(square, opponentPiece.getSquare())
		);
	}

	public boolean isPieceDefendingKing(Piece piece) {
		Color color = piece.getColor();
		King king = board.getKing(color);
		Color oppositeColor = color.opposite();

		List<Piece> opponentPiecesPinningToKing =
				board.getPiecesMovingTo(
						king.getLocation()[0],
						king.getLocation()[1],
						null,
						oppositeColor
				).stream()
						.filter(opponentPiece -> opponentPiece.getClass() != Knight.class)
						.filter(opponentPiece ->
								        countOfPiecesBetween(
										        opponentPiece.getSquare(),
										        king.getSquare()
								        ) == 1)
						.collect(Collectors.toList());
		List<Piece> attackingOpponentPieces =
				board.getPiecesMovingTo(
						piece.getLocation()[0],
						piece.getLocation()[1],
						null,
						oppositeColor
				).stream()
						.filter(opponentPiece -> opponentPiece.getClass() != Knight.class)
						.filter(opponentPiece -> !anyPiecesBetween(
								piece.getSquare(),
								opponentPiece.getSquare()))
						.collect(Collectors.toList());

		opponentPiecesPinningToKing.retainAll(attackingOpponentPieces);

		return opponentPiecesPinningToKing.isEmpty();
	}

	public boolean anyPiecesBetween(@NotNull Square square, @NotNull Square square1) {
		return board.squaresBetween(square, square1).stream()
				.anyMatch(square2 -> square2.getPiece() != null);
	}

	public int countOfPiecesBetween(@NotNull Square square, @NotNull Square square1) {
		return (int) board.squaresBetween(square, square1).stream()
				.filter(square2 -> square2.getPiece() != null).count();
	}
}
