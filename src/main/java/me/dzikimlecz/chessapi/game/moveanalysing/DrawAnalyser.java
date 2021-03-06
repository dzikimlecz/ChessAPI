package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.square.Color;
import me.dzikimlecz.chessapi.game.board.square.Square;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.GameState;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;
import me.dzikimlecz.chessapi.game.movestoring.MoveDatabase;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.dzikimlecz.chessapi.game.board.square.Color.BLACK;
import static me.dzikimlecz.chessapi.game.board.square.Color.WHITE;

public class DrawAnalyser implements IDrawAnalyser {
	private MoveDatabase moveDatabase;
	private GameState gameState;
	private IMoveValidator validator;
	private Board board;
	private List<MoveData> whiteMoves;
	private List<MoveData> blackMoves;

	@Override
	public void setMoveDatabase(MoveDatabase moveDatabase) {
		this.moveDatabase = moveDatabase;
	}

	@Override
	public void setValidator(IMoveValidator validator) {
		this.validator = validator;
	}

	@Override
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	@Nullable
	public DrawReason lookForDraw() {
		this.board = gameState.board();
		whiteMoves = moveDatabase.getAllMoves(WHITE);
		blackMoves = moveDatabase.getAllMoves(BLACK);
		if (noMovesWithPawnDuring50Moves()) return DrawReason.FIFTY_MOVES_WITHOUT_PAWN;
		if (triplePositionRepeat()) return DrawReason.TRIPLE_POSITION_REPEAT;
		if (staleMate()) return DrawReason.STALE_MATE;
		if (deadPosition()) return DrawReason.LACK_OF_PIECES;
		return null;
	}

	//fixme 23.12.2020: It's a workaround, checks if three LAST positions were the same based on
	// move notations, it doesn't fit chess rules and need to be fixed. I didn't have any other
	// ideas than just store state of the board in MoveDatabase but i guess it would be
	// memory-consuming. Will be fixed after memory tests and optimization.
	private boolean triplePositionRepeat() {
		if (whiteMoves.size() < 4) return false;
		var whites = whiteMoves.subList(whiteMoves.size() - 3, whiteMoves.size());
		var blacks = blackMoves.subList(blackMoves.size() - 3, blackMoves.size());
		return whites.stream().distinct().count() == 1 && blacks.stream().distinct().count() == 1;
	}

	private boolean staleMate() {
		if (whiteMoves.size() < 10) return false;
		Color color = gameState.color();
		List<ChessPiece> pieces = new ArrayList<>();
		for (int row = 1; row <= 8; row++) {
			for (char line = 'a'; line <= 'h'; line++) {
				var piece = board.square(line, row).piece();
				if (piece != null && piece.color() == color)
					pieces.add(piece);
			}
		}
		Map<ChessPiece, Square> possibleResponses = new HashMap<>();
		for (ChessPiece piece : pieces) {
			var pieceSquare = board.square(piece.location()[0], piece.location()[1]);
			if (!(piece instanceof Movable)) continue;
			var movablePiece = (Movable) piece;
			var moveDeltas = movablePiece.moveDeltas();
			for (int[] moveDelta : moveDeltas)
				possibleResponses.put(piece, board.getSquareByDelta(pieceSquare, moveDelta));
		}
		var responseData = new MoveData("#stalemate", possibleResponses, color);
		return validator.validate(responseData).getVariations().isEmpty();
	}

	private boolean noMovesWithPawnDuring50Moves() {
		return moveDatabase.movesWithoutPawnCount() >= 50;
	}

	private boolean deadPosition() {
		List<ChessPiece> whitePieces = new ArrayList<>();
		List<ChessPiece> blackPieces = new ArrayList<>();
		for (int row = 1; row <= 8; row++) {
			for (char line = 'a'; line <= 'h'; line++) {
				var piece = board.square(line, row).piece();
				if (piece != null) {
					var list = (piece.color() == WHITE) ? whitePieces : blackPieces;
					list.add(piece);
				}
			}
		}

		if (anyRooksOrQueens(whitePieces) || anyRooksOrQueens(blackPieces))
			return false;
		var pawnsBlocked = pawnsBlocked(whitePieces) && pawnsBlocked(blackPieces);
		if (!pawnsBlocked) return false;
		if (blackPieces.stream().dropWhile(piece -> piece instanceof Pawn).count() == 1)
			return true;
		if (whitePieces.stream().dropWhile(piece -> piece instanceof Pawn).count() == 1)
			return true;
		return deadPieceSet(whitePieces, blackPieces);
	}

	private boolean deadPieceSet(List<ChessPiece> whites, List<ChessPiece> blacks) {
		if (whites.size() == blacks.size()) {
			if (whites.size() == 1) return true;
			if (whites.size() == 2) {
				Optional<ChessPiece> whiteBishopOpt =
						whites.stream().filter(piece -> piece instanceof Bishop).findAny();
				if (whiteBishopOpt.isEmpty()) return false;
				Optional<ChessPiece> blackBishopOpt =
						blacks.stream().filter(piece -> piece instanceof Bishop).findAny();
				if (blackBishopOpt.isEmpty()) return false;
				return blackBishopOpt.get().color() == whiteBishopOpt.get().color();
			}
			return false;
		}
		//list used when 1 of the players has only a king, contains pieces belonging to the
		// opponent of that player.
		List<ChessPiece> notSingletonList;
		if (whites.size() == 1) notSingletonList = blacks;
		else if (blacks.size() == 1) notSingletonList = whites;
		else return false;

		return notSingletonList.stream()
				.allMatch(piece -> piece instanceof Knight ||
						piece instanceof Bishop || piece instanceof King);
	}

	private boolean pawnsBlocked(List<ChessPiece> pieces) {
		return pieces.stream()
				.filter(piece -> piece instanceof Pawn)
				.map(piece -> (Pawn) piece)
				.allMatch(pawn -> {
					Map<Pawn, Square> variations = new HashMap<>();
					pawn.moveDeltas()
							.forEach(delta -> variations.put(
									pawn, board.getSquareByDelta(pawn.square(), delta))
							);
					return validator.validate(
							new MoveData("#deadposition", variations, pawn.color()))
							.getVariations().isEmpty();
				});
	}

	private boolean anyRooksOrQueens(List<ChessPiece> pieces) {
		return pieces.stream().anyMatch(piece -> piece instanceof Rook || piece instanceof Queen);
	}
}
