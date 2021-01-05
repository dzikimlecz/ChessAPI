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
		var piece = square.piece();
		return (piece != null) && (piece.color() == color);
	}

	public boolean isSquareAttacked(Square square, Color attackedColor) {
		return board.getPiecesMovingTo(
				square,
				Piece.class,
				attackedColor.opposite()
		).stream().anyMatch(
				opponentPiece -> opponentPiece.getClass() == Knight.class
						|| noPiecesBetween(square, opponentPiece.square())
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
						.filter(opponentPiece -> noPiecesBetween(
								piece.square(),
								opponentPiece.square()))
						.collect(Collectors.toList());

		opponentPiecesPinningToKing.retainAll(attackingOpponentPieces);

		return !opponentPiecesPinningToKing.isEmpty();
	}

	public boolean anyPiecesBetween(@NotNull Square square, @NotNull Square square1) {
		return !noPiecesBetween(square, square1);
	}

	public boolean noPiecesBetween(@NotNull Square square, @NotNull Square square1) {
		var squares = board.squaresBetween(square, square1);
		return squares.isEmpty() || squares.stream().allMatch(square2 -> square2.piece() == null);
	}

	public int countOfPiecesBetween(@NotNull Square square, @NotNull Square square1) {
		return (int) board.squaresBetween(square, square1).stream()
				.filter(square2 -> square2.piece() != null).count();
	}

	public boolean isKingAttacked(Color attacked) {
		var kingsSquare = board.getKing(attacked).square();
		return board.getPiecesMovingTo(kingsSquare, Piece.class, attacked.opposite()).stream()
				.anyMatch(piece -> piece instanceof Knight || noPiecesBetween(piece.square(),
				                                                               kingsSquare));
	}
}
