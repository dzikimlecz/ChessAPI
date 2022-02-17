package me.dzikimlecz.chessapi.game.board;

import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.board.square.Square;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.dzikimlecz.chessapi.game.board.Color.BLACK;
import static me.dzikimlecz.chessapi.game.board.Color.WHITE;

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

		for (var entry : Map.of(1, WHITE, 8, BLACK).entrySet()) {
			var line = entry.getKey();
			var color = entry.getValue();
			for (char c : new char[]{'c', 'f'})
				new Bishop(color, square(c, line));
			for (char c : new char[]{'a', 'h'})
				new Rook(color, square(c, line));
			for (char c : new char[]{'b', 'g'})
				new Knight(color, square(c, line));
		}
		new Queen(WHITE, square('d', 1));
		new King(WHITE, square('e', 1));
		new Queen(BLACK, square('d', 8));
		new King(BLACK, square('e', 8));
	}
}
