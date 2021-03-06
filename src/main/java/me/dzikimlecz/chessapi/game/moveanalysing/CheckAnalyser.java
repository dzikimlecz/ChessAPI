package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.board.BoardState;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.square.Square;
import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.pieces.King;
import me.dzikimlecz.chessapi.game.board.pieces.Knight;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.GameState;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;

import java.util.Map;
import java.util.stream.Collectors;

public class CheckAnalyser implements IMoveAnalyser {
	private GameState gameState;
	private BoardState boardState;
	private Board board;
	private IMoveValidator validator;

	@Override
	public void setValidator(IMoveValidator validator) {
		this.validator = validator;
	}

	@Override
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	@Override
	public MoveData analyse(MoveData data) {
		this.board = gameState.board();
		this.boardState = board.getState();
		var variations = data.getVariations();
		for (ChessPiece piece : variations.keySet()) {
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
		return boardState.isKingAttacked(data.color().opposite());
	}

	private boolean lookForMate(MoveData data) {
		var color = data.color().opposite();
		King king = board.getKing(color);

		boolean areCloseSquaresBlocked = king.moveDeltas().stream().allMatch(set -> {
			var square = board.getSquareByDelta(king.square(), set);
			return boardState.isSquareOccupied(square, color) ||
					boardState.isSquareAttacked(square, color);
		});

		if(!areCloseSquaresBlocked) return false;

		var variations = data.getVariations();
		var pieces = variations.keySet();
		if (pieces.stream().noneMatch(e -> e instanceof Knight)) {
			for (ChessPiece piece : pieces) {
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

}
