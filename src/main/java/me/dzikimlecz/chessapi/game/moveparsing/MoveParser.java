package me.dzikimlecz.chessapi.game.moveparsing;

import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.movestoring.GameState;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoveParser implements IMoveParser {

	private GameState gameState;

	private Board board;
	private IllegalArgumentException illegalMove;

	@Override
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	@Override
	public MoveData parse(String notation) {
		board = gameState.board();
		Color color = gameState.color();
		Map<Piece, Square> variations = parseToMap(notation, color);
		return new MoveData(notation, variations, color);
	}


	private Map<Piece, Square> parseToMap(String notation, Color color) {
		illegalMove = new IllegalArgumentException(MessageFormat.format(
				"{0} is not valid move notation for {1}",
				notation, color.toString().toLowerCase())
		);
		if (simplePawnMove.matcher(notation).matches())
			return parseSimplePawnMove(notation, color);
		if (simplePieceMove.matcher(notation).matches())
			return parseSimplePieceMove(notation, color);
		if (pawnMove.matcher(notation).matches())
			return parsePawnMove(notation, color);
		if (pieceMove.matcher(notation).matches())
			return parsePieceMove(notation, color);
		if (specifiedPawnMove.matcher(notation).matches())
			return parseSpecifiedPawnMove(notation, color);
		if (specifiedPieceMove.matcher(notation).matches())
			return parseSpecifiedPieceMove(notation, color);
		if (castling.matcher(notation).matches())
			return parseCastling(notation, color);
		throw illegalMove;
	}

	private Map<Piece, Square> parseCastling(String notation, Color color) {
		boolean isCastlingShort = notation.length() == 3;
		final int row = (color == Color.WHITE) ? 1 : 8;
		Piece piece = board.square('e', row).piece();
		if (!(piece instanceof King)) throw illegalMove;
		var king = (King) piece;

		char rookLine = (isCastlingShort) ? 'h' : 'a';
		piece = board.square(rookLine, row).piece();
		if (!(piece instanceof Rook)) throw illegalMove;
		var rook = (Rook) piece;

		char newKingLine = (isCastlingShort) ? 'g' : 'c';
		char newRookLine = (isCastlingShort) ? 'f' : 'd';
		return Map.of(
				king, board.square(newKingLine, row),
				rook, board.square(newRookLine, row)
		);
	}

	private Map<Piece, Square> parseSimplePawnMove(String notation, Color color) {
		return parseSimplePieceMove('P' + notation, color);
	}

	private Map<Piece, Square> parseSimplePieceMove(String notation, Color color) {
		Map<Piece, Square> moves = new HashMap<>();
		char line = notation.charAt(1);
		//row is an integer, needs to be converted from char
		int row = notation.charAt(2) - '0';
		var square = board.square(line, row);
		var pieceType = getPieceType(notation);
		board.getPiecesMovingTo(line, row, pieceType, color).forEach(e -> moves.put(e, square));
		return moves;
	}

	private Map<Piece, Square> parsePawnMove(String notation, Color color) {
		return parsePieceMove("P" + notation, color);
	}

	private Map<Piece, Square> parsePieceMove(String notation, Color color) {
		char endLine = notation.charAt(1);
		int endRow = notation.charAt(2) - '0';
		char startSpecifier = notation.charAt(0);
		boolean lookingForRow = Character.isDigit(startSpecifier);
		char startLine = (lookingForRow) ? endLine : startSpecifier;
		int startRow = (lookingForRow) ? startSpecifier - '0' : endRow;
		var pieceType = getPieceType(notation);
		List<? extends Piece> pieces = board.getPiecesMovingTo(endLine, endRow, pieceType, color);
		Square destination = board.square(endLine, endRow);
		return pieces.stream().filter(e -> {
			char[] location = e.location();
			return location[0] == startLine && location[1] == startRow;
		}).collect(Collectors.toUnmodifiableMap(e -> e, e -> destination, (a, b) -> b));
	}

	private Map<Piece, Square> parseSpecifiedPawnMove(String notation, Color color) {
		return parseSpecifiedPieceMove('P' + notation, color);
	}

	private Map<Piece, Square> parseSpecifiedPieceMove(String notation, Color color) {
		final var pieceType = getPieceType(notation);
		final char startLine = notation.charAt(1);
		final int startRow = notation.charAt(2) - '0';
		final char endLine = notation.charAt(3);
		final int endRow = notation.charAt(4) - '0';
		final var startSquare = board.square(startLine, startRow);
		final var endSquare = board.square(endLine, endRow);
		var piece = startSquare.piece();
		if (piece == null || piece.getClass() != pieceType || piece.color() != color)
			return Map.of();
		return Map.of(piece, endSquare);
	}

	@NotNull
	private Class<? extends Piece> getPieceType(String notation) {
		return switch (notation.charAt(0)) {
			case 'P' -> Pawn.class;
			case 'N', 'S' -> Knight.class;
			case 'B', 'G' -> Bishop.class;
			case 'R', 'W' -> Rook.class;
			case 'Q', 'H' -> Queen.class;
			case 'K' -> King.class;
			default -> throw new IllegalStateException("Unexpected value: " + notation.charAt(0));
		};
	}
}
