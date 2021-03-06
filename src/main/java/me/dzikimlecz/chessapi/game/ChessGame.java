package me.dzikimlecz.chessapi.game;

import me.dzikimlecz.chessapi.ChessEventListener;
import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import me.dzikimlecz.chessapi.game.board.pieces.Takeable;
import me.dzikimlecz.chessapi.game.events.ChessEvent;
import me.dzikimlecz.chessapi.game.moveanalysing.*;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveParser;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.moveparsing.MoveParser;
import me.dzikimlecz.chessapi.game.moveparsing.MoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.GameState;
import me.dzikimlecz.chessapi.game.movestoring.ListMoveDatabase;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;
import me.dzikimlecz.chessapi.game.movestoring.MoveDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.dzikimlecz.chessapi.game.board.Color.BLACK;
import static me.dzikimlecz.chessapi.game.board.Color.WHITE;

public final class ChessGame extends Thread {
	private final IMoveAnalyser pawnExchangeAnalyser;
	private final MoveDatabase moveDatabase;
	private final ChessEventListener listener;
	private final IMoveParser parser;
	private final IMoveValidator validator;
	private final IDrawAnalyser drawAnalyser;
	private final IMoveValidator enPassantCastlingValidator;
	private final IMoveAnalyser checkAnalyser;

	private final PawnExchangeProcessor pawnExchangeProcessor;
	private final GameState gameState;
	private final Board board;
	private final BlockingQueue<ChessEvent> events;
	private final AtomicBoolean hasStopped;

	public ChessGame(ChessEventListener listener) {
		this(listener, new ListMoveDatabase(), new MoveParser(), new MoveValidator(),
		     new EnPassantCastlingValidator(), new CheckAnalyser(), new DrawAnalyser(),
		     new PawnExchangeAnalyser());
		setName("Game: %x".formatted(getId()));
		checkAnalyser.setValidator(validator);
		drawAnalyser.setValidator(validator);
	}

	public ChessGame(ChessEventListener listener,
	                 MoveDatabase moveDatabase,
	                 IMoveParser parser,
	                 IMoveValidator validator,
	                 IMoveValidator enPassantCastlingValidator,
	                 IMoveAnalyser checkAnalyser,
	                 IDrawAnalyser drawAnalyser,
	                 IMoveAnalyser pawnExchangeAnalyser) {
		super();
		this.events = new ArrayBlockingQueue<>(100);
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
		this.pawnExchangeAnalyser = pawnExchangeAnalyser;
		this.pawnExchangeProcessor = new PawnExchangeProcessor();
		initProcessors();
		start();
	}

	private void initProcessors() {
		parser.setGameState(gameState);
		validator.setGameState(gameState);
		enPassantCastlingValidator.setGameState(gameState);
		drawAnalyser.setGameState(gameState);
		checkAnalyser.setGameState(gameState);
		pawnExchangeAnalyser.setGameState(gameState);
		enPassantCastlingValidator.setMoveDatabase(moveDatabase);
		drawAnalyser.setMoveDatabase(moveDatabase);
	}

	@Override public void run() {
		try {
			Thread.sleep(100);
			while (isOngoing()) {
				var event = events.take();
				switch (event.getType()) {
					case DRAW_REQUEST -> requestDraw(
							event.getNotation().contains(WHITE.name().toLowerCase()) ? WHITE : BLACK
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
				take(targetSquarePiece);
				var newNotation = new StringBuilder(data.notation())
						.insert(1, 'x');
				data.setNotation(newNotation.toString());
			}
			piece.moveTo(square);
		});

		pawnExchangeAnalyser.analyse(data);
		var notation = data.notation();
		if (notation.endsWith(":exchange")) {
			@SuppressWarnings("all")
			var piece = pieceMoves.keySet().stream().findFirst().get();
			pawnExchangeProcessor.exchange(listener.onPawnExchange(), color(), pieceMoves.get(piece));
		}

		checkAnalyser.analyse(data);
		moveDatabase.put(data);
		gameState.setColor(moveDatabase.turnColor());
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

	private void take(@NotNull Piece targetSquarePiece) {
		if (!(targetSquarePiece instanceof Takeable))
			throw new IllegalStateException("Cannot take non-takeable piece.");
		((Takeable) targetSquarePiece).beTaken();
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
	}

	@Override public void interrupt() {
		super.interrupt();
		stopGame();
	}

	@Override public String toString() {
		return "{Game: %s, Ongoing: %s}".formatted(getName(), hasStopped);
	}
}
