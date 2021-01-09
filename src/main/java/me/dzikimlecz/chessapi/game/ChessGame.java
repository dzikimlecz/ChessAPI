package me.dzikimlecz.chessapi.game;

import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.Takeable;
import me.dzikimlecz.chessapi.game.events.ChessEvent;
import me.dzikimlecz.chessapi.game.moveanalysing.*;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveParser;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.moveparsing.MoveParser;
import me.dzikimlecz.chessapi.game.moveparsing.MoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.*;
import me.dzikimlecz.chessapi.ChessEventListener;
import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;


import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.dzikimlecz.chessapi.game.board.Color.*;

public class ChessGame extends Thread {
	private MoveDatabase moveDatabase;
	private ChessEventListener listener;
	private IMoveParser parser;
	private IMoveValidator validator;
	private GameState gameState;
	private Board board;
	private IDrawAnalyser drawAnalyser;
	private IMoveValidator enPassantCastlingValidator;
	private IMoveAnalyser checkAnalyser;
	private BlockingQueue<ChessEvent> events;
	private final AtomicBoolean hasStopped;

	public ChessGame(ChessEventListener listener) {
		this(listener, new ListMoveDatabase(), new MoveParser(), new MoveValidator(),
		     new EnPassantCastlingValidator(), new CheckAnalyser(), new DrawAnalyser());
		checkAnalyser.setValidator(validator);
		drawAnalyser.setValidator(validator);
	}

	public ChessGame(ChessEventListener listener,
	                 MoveDatabase moveDatabase,
	                 IMoveParser parser,
	                 IMoveValidator validator,
	                 IMoveValidator enPassantCastlingValidator,
	                 IMoveAnalyser checkAnalyser,
	                 IDrawAnalyser drawAnalyser) {
		super();
		this.events = new LinkedBlockingQueue<>(100);
		this.board = new Board();
		this.gameState = new GameState();
		gameState.setBoard(board);
		gameState.setColor(WHITE);
		this.hasStopped = new AtomicBoolean(false);
		this.moveDatabase = moveDatabase;
		this.listener = listener;
		this.drawAnalyser = drawAnalyser;
		this.parser = parser;
		this.validator = validator;
		this.enPassantCastlingValidator = enPassantCastlingValidator;
		this.checkAnalyser = checkAnalyser;
		parser.setGameState(gameState);
		validator.setGameState(gameState);
		enPassantCastlingValidator.setGameState(gameState);
		drawAnalyser.setGameState(gameState);
		checkAnalyser.setGameState(gameState);
		enPassantCastlingValidator.setMoveDatabase(moveDatabase);
		drawAnalyser.setMoveDatabase(moveDatabase);
		start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(100);
			while (isOngoing()) {
				var event = events.take();
				switch (event.getType()) {
					case DRAW_REQUEST -> requestDraw(
							event.getNotation().contains("white") ? WHITE : BLACK
					);
					case CLOSE -> stopGame();
					case MOVE -> move(event.getNotation());
				}
			}
		} catch(InterruptedException e) {
			stopGame();
		}
	}

	public void handleEvent(ChessEvent event) throws InterruptedException {
		if (hasStopped.get()) throw new IllegalStateException("Game is not ongoing");
		events.put(event);
	}


	public Board board() {
		return board;
	}

	private void move(String notation) {
		var moveData = validator.validate(parser.parse(notation));
		handleMove(moveData);
	}

	private void handleMove(MoveData data) {

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
		gameState.setColor(moveDatabase.turnColor());

		var notation = data.notation();
		if (notation.contains("+")) listener.onCheck(gameState.color());
		else if (notation.contains("#")) {
			listener.onMoveHandled();
			listener.onMate(gameState.color());
			stopGame();
			return;
		} else {
			var drawReason = drawAnalyser.lookForDraw();
			if (drawReason != null) {
				listener.onMoveHandled();
				listener.onDraw(drawReason);
				stopGame();
				return;
			}
		}
		gameState.setColor(moveDatabase.turnColor());
		listener.onMoveHandled();
	}

	public Color color() {
		return moveDatabase.turnColor();
	}

	public boolean isOngoing() {
		return !hasStopped.get();
	}

	private void requestDraw(Color color) {
		if (listener.onDrawRequest(color)) {
			listener.onDraw(DrawReason.PLAYERS_DECISION);
			stopGame();
		}
	}

	public ChessEventListener listener() {
		return listener;
	}
	
	private void stopGame() {
		if (!isInterrupted()) super.interrupt();
		hasStopped.set(true);
		moveDatabase = null;
		listener = null;
		parser = null;
		validator = null;
		gameState = null;
		board = null;
		drawAnalyser = null;
		enPassantCastlingValidator = null;
		checkAnalyser = null;
		events = null;
	}

	@Override
	public void interrupt() {
		super.interrupt();
		stopGame();
	}
}
