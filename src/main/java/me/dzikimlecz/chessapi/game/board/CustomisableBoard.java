package me.dzikimlecz.chessapi.game.board;

import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.board.square.Square;
import org.jetbrains.annotations.NotNull;

import static me.dzikimlecz.chessapi.game.board.Color.*;

class CustomisableBoard extends Board {


	CustomisableBoard() {
		this("8 ".repeat(8).trim());
	}

	CustomisableBoard(String fen) {
		super();
		parseFEN(fen);
	}

	@Override public King getKing(@NotNull Color color) {
		for (int row = 1; row <= 8; row++) {
			for (char line = 'a'; line <= 'h'; line++ ) {
				var piece = square(line, row).piece();
				if (piece instanceof King && piece.color() == color)
					return (King) piece;
			}
		}
		throw new IllegalStateException(
				"%s king not found on the board".formatted(color).toLowerCase());
	}

	void parseFEN(String fen) {
		char line = 'a';
		int row = 8;
		var ranks = fen.split("/", 8);
		for (int j = ranks.length - 1; j >= 0; j--) {
			String rank = ranks[j];
			var chars = rank.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (row <= 0 || line >= 'h') throw new IllegalArgumentException("Corrupted FEN");
				var aChar = chars[i];
				if (aChar >= '1' && aChar <= '8') {
					var skipped = aChar - '0';
					i += skipped;
					line += skipped;
				} else putPiece(aChar, square(line, row));
			}
			row--;
			line = 'a';
		}
	}

	private void putPiece(char aChar, @NotNull Square square) {
		var type = switch (aChar) {
			case 'P', 'p' -> Pawn.class;
			case 'N', 'n' -> Knight.class;
			case 'B', 'b' -> Bishop.class;
			case 'R', 'r' -> Rook.class;
			case 'Q', 'q' -> Queen.class;
			case 'K', 'k' -> King.class;
			default -> throw new IllegalArgumentException("Unexpected character: " + aChar);
		};
		var color = Character.isUpperCase(aChar) ? WHITE : BLACK;
		try {
			type.getConstructor(Color.class, Square.class).newInstance(color, square);
		} catch(Exception e) {
			throw new AssertionError();
		}
	}


}
