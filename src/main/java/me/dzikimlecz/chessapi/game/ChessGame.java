package me.dzikimlecz.chessapi.game;

import me.dzikimlecz.chessapi.ChessEventListener;
import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.board.Board;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.pieces.Movable;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.square.Square;
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
import java.util.concurrent.atomic.AtomicLong;

import static me.dzikimlecz.chessapi.game.board.Color.BLACK;
import static me.dzikimlecz.chessapi.game.board.Color.WHITE;

public final class ChessGame implements Runnable {
	private static final AtomicLong games = new AtomicLong();
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


	private final String name;
	public String getName() {
		return name;
	}

	public static final class Builder {
		private IMoveAnalyser pawnExchangeAnalyser;
		private MoveDatabase moveDatabase;
		private final ChessEventListener listener;
		private IMoveParser parser;
		private IMoveValidator validator;
		private IDrawAnalyser drawAnalyser;
		private IMoveValidator enPassantCastlingValidator;
		private IMoveAnalyser checkAnalyser;
		private String name;

		public Builder(@NotNull ChessEventListener chessGameEventListener) {
			this.listener = chessGameEventListener;
		}

		public Builder pawnExchangeAnalyser(@NotNull IMoveAnalyser pawnExchangeAnalyser) {
			this.pawnExchangeAnalyser = pawnExchangeAnalyser;
			return this;
		}

		public Builder moveDatabase(@NotNull MoveDatabase moveDatabase) {
			this.moveDatabase = moveDatabase;
			return this;
		}

		public Builder parser(@NotNull IMoveParser parser) {
			this.parser = parser;
			return this;
		}

		public Builder validator(@NotNull IMoveValidator validator) {
			this.validator = validator;
			return this;
		}

		public Builder drawAnalyser(@NotNull IDrawAnalyser drawAnalyser) {
			this.drawAnalyser = drawAnalyser;
			return this;
		}

		public Builder enPassantCastlingValidator(@NotNull IMoveValidator enPassantCastlingValidator) {
			this.enPassantCastlingValidator = enPassantCastlingValidator;
			return this;
		}

		public Builder checkAnalyser(@NotNull IMoveAnalyser checkAnalyser) {
			this.checkAnalyser = checkAnalyser;
			return this;
		}

		public void name(String name) {
			this.name = name;
		}

		public ChessGame build() {
			validator = Objects.requireNonNullElseGet(validator, MoveValidator::new);
			return new ChessGame(
					listener,
					Objects.requireNonNullElseGet(moveDatabase, ListMoveDatabase::new),
					Objects.requireNonNullElseGet(parser, MoveParser::new),
					validator,
					Objects.requireNonNullElseGet(enPassantCastlingValidator,
					                              EnPassantCastlingValidator::new),
					Objects.requireNonNullElseGet(checkAnalyser, () -> {
						var checkAnalyser1 = new CheckAnalyser();
						checkAnalyser1.setValidator(validator);
						return checkAnalyser1;
					}),
					Objects.requireNonNullElseGet(drawAnalyser, () -> {
						var drawAnalyser1 = new DrawAnalyser();
						drawAnalyser1.setValidator(validator);
						return drawAnalyser1;
					}),
					Objects.requireNonNullElseGet(pawnExchangeAnalyser, PawnExchangeAnalyser::new),
					Objects.requireNonNullElse(name, "Game: %x".formatted(Long.hashCode(games.getAndIncrement())))
			);
		}

	}

	public ChessGame(ChessEventListener listener) {
		this(listener, new ListMoveDatabase(), new MoveParser(), new MoveValidator(),
		     new EnPassantCastlingValidator(), new CheckAnalyser(), new DrawAnalyser(),
		     new PawnExchangeAnalyser(), "Game: %x".formatted(games.getAndIncrement()));
		checkAnalyser.setValidator(validator);
		drawAnalyser.setValidator(validator);
	}

	private ChessGame(@NotNull ChessEventListener listener,
					  @NotNull MoveDatabase moveDatabase,
					  @NotNull IMoveParser parser,
					  @NotNull IMoveValidator validator,
					  @NotNull IMoveValidator enPassantCastlingValidator,
					  @NotNull IMoveAnalyser checkAnalyser,
					  @NotNull IDrawAnalyser drawAnalyser,
					  @NotNull IMoveAnalyser pawnExchangeAnalyser, String name) {
		super();
		this.name = name;
		this.events = new ArrayBlockingQueue<>(100);
		this.board = Board.create();
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
					case MOVE -> handleMove(event.getNotation());
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

	private void handleMove(String notation) {
		move(validator.validate(parser.parse(notation)));
	}

	private void move(MoveData data) {
		if (data.toFurtherCheck()) data.validate(enPassantCastlingValidator);
		var pieceMoves = data.getVariations();
		if (pieceMoves.isEmpty()) {
			listener.onIllegalMove();
			return;
		}

		pieceMoves.forEach((piece, square) -> {
			var targetSquarePiece = square.piece();
			if (targetSquarePiece != null) {
				take(targetSquarePiece);
				var newNotation = new StringBuilder(data.notation()).insert(1, 'x');
				data.setNotation(newNotation.toString());
			}
			try {
				((Movable) piece).moveTo(square);
			} catch (ClassCastException e) {
				throw new IllegalStateException("Can't move non movable piece");
			}
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

	private void take(@NotNull ChessPiece targetSquarePiece) {
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
	
	public void stopGame() {
		hasStopped.set(true);
	}

	@Override public String toString() {
		return "{Game: %s, Ongoing: %s}".formatted(name, !hasStopped.get());
	}
}
