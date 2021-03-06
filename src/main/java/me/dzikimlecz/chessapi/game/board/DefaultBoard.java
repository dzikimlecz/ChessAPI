package me.dzikimlecz.chessapi.game.board;

import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.board.square.Color;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.dzikimlecz.chessapi.game.board.square.Color.BLACK;
import static me.dzikimlecz.chessapi.game.board.square.Color.WHITE;

class DefaultBoard extends Board {

	private final Map<Color, King> kings;

	DefaultBoard() {
		super();
		putPieces();
		var blackKing = Objects.requireNonNull((King) square('e', 8).piece());
		var whiteKing = Objects.requireNonNull((King) square('e', 1).piece());
		kings = Map.of(WHITE, whiteKing, BLACK, blackKing);
	}

	@Override public King getKing(@NotNull Color color) {
		return Objects.requireNonNull(kings.get(color));
	}

	private void putPieces() {
		//Puts Pawns
		for (char line = 'a'; line <= 'h'; line++) {
			new Pawn(WHITE, square(line, 2));
			new Pawn(BLACK, square(line, 7));
		}

		List.of(square('a', 1), square('h', 1))
				.forEach(square -> new Rook(WHITE, square));
		List.of(square('a', 8), square('h', 8))
				.forEach(square -> new Rook(BLACK, square));
		List.of(square('b', 1), square('g', 1))
				.forEach(square -> new Knight(WHITE, square));
		List.of(square('b', 8), square('g', 8))
				.forEach(square -> new Knight(BLACK, square));
		List.of(square('c', 1), square('f', 1))
				.forEach(square -> new Bishop(WHITE, square));
		List.of(square('c', 8), square('f', 8))
				.forEach(square -> new Bishop(BLACK, square));
		new Queen(WHITE, square('d', 1));
		new Queen(BLACK, square('d', 8));
		new King(WHITE, square('e', 1));
		new King(BLACK, square('e', 8));
	}
}
