package me.dzikimlecz.chessapi.game;

import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.Takeable;
import me.dzikimlecz.chessapi.game.moveanalysing.CheckAnalyser;
import me.dzikimlecz.chessapi.game.moveanalysing.DrawAnalyser;
import me.dzikimlecz.chessapi.game.moveanalysing.EnPassantCastlingValidator;
import me.dzikimlecz.chessapi.game.moveanalysing.MoveAnalyser;
import me.dzikimlecz.chessapi.game.movestoring.*;
import me.dzikimlecz.chessapi.ChessEventListener;
import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import me.dzikimlecz.chessapi.game.moveparsing.MoveValidator;


import java.util.Map;

public class ChessGame {

	private final MoveDatabase moveDatabase;
	private final ChessEventListener listener;
	private final GamesData gamesData;
	private final Board board;
	private final DrawAnalyser drawAnalyser;
	private final EnPassantCastlingValidator enPassantCastlingValidator;
	private final MoveAnalyser checkAnalyser;
	private boolean hasStopped;

	public ChessGame(MoveDatabase moveDatabase, ChessEventListener listener, GamesData gamesData) {
		this.moveDatabase = moveDatabase;
		this.listener = listener;
		this.gamesData = gamesData;
		board = new Board();
		drawAnalyser = new DrawAnalyser(moveDatabase, gamesData, new MoveValidator(gamesData));
		enPassantCastlingValidator = new EnPassantCastlingValidator(moveDatabase);
		checkAnalyser = new CheckAnalyser(gamesData, new MoveValidator(gamesData));
	}

	public Board board() {
		return board;
	}

	public void handleMove(MoveData data) {
		if (hasStopped) throw new IllegalStateException("Game is not ongoing");

		if (data.toFurtherCheck()) data.validate(enPassantCastlingValidator);
		Map<Piece, Square> pieceMoves = data.getVariations();
		if (pieceMoves.isEmpty()) {
			listener.onIllegalMove();
			return;
		}

		pieceMoves.forEach((piece, square) -> {
			var targetSquarePiece = square.piece();
			if (targetSquarePiece != null) {
				if (!(targetSquarePiece instanceof Takeable))
					throw new IllegalStateException("Cannot take non-takeable piece.");
				((Takeable) targetSquarePiece).beTaken();
				var newNotation = new StringBuilder(data.notation())
						.insert(1, 'x');
				data.setNotation(newNotation.toString());
			}
			piece.moveTo(square);
		});

		checkAnalyser.analyse(data);
		moveDatabase.put(data);
		gamesData.setColor(moveDatabase.turnColor());

		var notation = data.notation();
		if (notation.contains("+")) listener.onCheck(gamesData.color());
		else if (notation.contains("#")) {
			this.hasStopped = false;
			listener.onMate(gamesData.color());
		} else {
			var drawReason = drawAnalyser.lookForDraw();
			if (drawReason != null) {
				this.hasStopped = true;
				listener.onDraw(drawReason);
				return;
			}
		}
		listener.onMoveHandled();
	}

	public Color color() {
		return moveDatabase.turnColor();
	}

	public boolean isOngoing() {
		return !hasStopped;
	}

	public void requestDraw() {
		if (listener.onDrawRequest()) {
			listener.onDraw(DrawReason.PLAYERS_DECISION);
			hasStopped = true;
		}
	}

	public ChessEventListener listener() {
		return listener;
	}
}
