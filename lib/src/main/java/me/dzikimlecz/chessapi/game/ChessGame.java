package me.dzikimlecz.chessapi.game;

import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.Takeable;
import me.dzikimlecz.chessapi.game.moveanalysing.DrawAnalyser;
import me.dzikimlecz.chessapi.game.moveanalysing.EnPassantCastlingValidator;
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
	private boolean hasStopped;

	public ChessGame(MoveDatabase moveDatabase, ChessEventListener listener, GamesData gamesData) {
		this.moveDatabase = moveDatabase;
		this.listener = listener;
		this.gamesData = gamesData;
		board = new Board();
		drawAnalyser = new DrawAnalyser(moveDatabase, gamesData, new MoveValidator(gamesData));
		enPassantCastlingValidator = new EnPassantCastlingValidator(moveDatabase);
	}

	public Board getBoard() {
		return board;
	}

	public void handleMove(MoveData data) {
		if (hasStopped) throw new IllegalStateException("Game is not ongoing");
		enPassantCastlingValidator.validate(data);
		Map<Piece, Square> variations = data.getVariations();
		if (variations.isEmpty()) {
			listener.onIllegalMove();
			return;
		}
		for (Piece piece : variations.keySet()) {
			var targetSquare = variations.get(piece);
			var targetSquarePiece = targetSquare.getPiece();
			if (!(targetSquarePiece instanceof Takeable))
				throw new IllegalStateException("Cannot take non-takeable piece.");
			((Takeable) targetSquarePiece).beTaken();
			piece.moveTo(targetSquare);
		}
		moveDatabase.put(data);
		gamesData.setColor(moveDatabase.turnColor());
		var notation = data.getNotation();
		if (notation.contains("+")) listener.onCheck(gamesData.getColor());
		else if (notation.contains("#")) {
			this.hasStopped = false;
			listener.onMate(gamesData.getColor());
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

	public Color getColor() {
		return moveDatabase.turnColor();
	}

	public boolean isOngoing() {
		return !hasStopped;
	}

	public void closeAndDraw() {
		listener.onDraw(DrawReason.PLAYERS_DECISION);
	}
}
