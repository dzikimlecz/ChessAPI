package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.board.BoardState;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.King;
import me.dzikimlecz.chessapi.game.board.pieces.Knight;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.GamesData;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;

import java.util.Map;
import java.util.stream.Collectors;

public class CheckAnalyser implements MoveAnalyser {

	public final GamesData gamesData;
	private BoardState boardState;
	private Board board;
	private final IMoveValidator validator;

	public CheckAnalyser(GamesData gamesData, IMoveValidator validator) {
		this.gamesData = gamesData;
		this.validator = validator;
	}


	@Override
	public MoveData analyse(MoveData data) {
		this.board = gamesData.board();
		this.boardState = board.getState();
		Map<Piece, Square> variations = data.getVariations();
		for (Piece piece : variations.keySet()) {
			var targetSquare = variations.get(piece);
			lookForTaking(data, targetSquare);
			if (lookForCheck(data)) {
				var notation = new StringBuilder(data.notation());
				notation.setLength(notation.length() - 1);
				notation.append((lookForMate(data)) ? '#' : '+');
				data.setNotation(notation.toString());
			}
		}
		return data;
	}

	private boolean lookForCheck(MoveData data) {
		King king = board.getKing((data.color() == Color.WHITE) ? Color.BLACK : Color.WHITE);
		if (boardState.isSquareAttacked(king.square(), king.color())) {
			data.setNotation(data.notation() + '+');
			return true;
		}
		return false;
	}

	private boolean lookForMate(MoveData data) {
		var color = data.color();
		King king = board.getKing((color == Color.WHITE) ? Color.BLACK : Color.WHITE);

		boolean areCloseSquaresBlocked = king.moveDeltas().stream().allMatch(set -> {
			var square = board.square((char) set[0], set[1]);
			return boardState.isSquareOccupied(square, color) ||
					boardState.isSquareAttacked(square, color);
		});

		if(!areCloseSquaresBlocked) return false;

		var variations = data.getVariations();
		var pieces = variations.keySet();
		if (pieces.stream().noneMatch(e -> e instanceof Knight)) {
			for (Piece piece : pieces) {
				if (piece instanceof King) continue;
				var attackingSquare = variations.get(piece);
				var squaresBetween = board.squaresBetween(attackingSquare, king.square());
				for (Square square : squaresBetween) {
					var possibleResponses =
							board.getPiecesMovingTo(square, Piece.class, color)
									.stream()
									.filter(e -> !(e instanceof King))
									.collect(Collectors.toMap(e -> e, e -> square, (a, b) -> b));
					var responsesData = new MoveData("#response", possibleResponses, color);
					validator.validate(responsesData);
					if (!responsesData.getVariations().isEmpty()) return false;
				}
			}
		}
		return true;
	}

	private void lookForTaking(MoveData data, Square targetSquare) {
		if (targetSquare.piece() != null) {
			var newNotation = new StringBuilder(data.notation())
					.insert(1, 'x');
			data.setNotation(newNotation.toString());
		}
	}
}
