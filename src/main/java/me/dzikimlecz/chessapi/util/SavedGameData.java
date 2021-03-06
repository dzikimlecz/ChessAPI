package me.dzikimlecz.chessapi.util;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;

import java.text.MessageFormat;

public class SavedGameData {
	private final ChessPiece[][] board;
	private final Color turn;

	public SavedGameData(ChessPiece[][] board, Color turn) {
		this.board = board;
		this.turn = turn;
	}

	public String toJSON() {
		var builder = new StringBuilder("{\n");
		builder.append("\"color\": \"").append(turn.name()).append("\",\n")
				.append("\"board\": {\n");
		for (int i = 0, boardLength = board.length; i < boardLength; i++) {
			ChessPiece[] chessPieces = board[i];
			builder.append(MessageFormat.format("{0}{1}{0}: [\n", '"', i + 1));
			for (int j = 0, chessPiecesLength = chessPieces.length; j < chessPiecesLength; j++) {
				ChessPiece chessPiece = chessPieces[j];
				if (chessPiece == null)
					builder.append("null");
				else builder.append('"').append(chessPiece.toString())
						.append(chessPiece.color().name().charAt(0)).append('"');
				if (j != chessPiecesLength - 1)
					builder.append(", ");
			}
			builder.append(']');
			if (i != boardLength - 1)
				builder.append(", ");
			builder.append('\n');
		}
		builder.append('}');
		return builder.toString();
	}
}
