package me.dzikimlecz.chessapi;

import me.dzikimlecz.chessapi.game.ChessGame;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import me.dzikimlecz.chessapi.game.moveanalysing.CheckAnalyser;
import me.dzikimlecz.chessapi.game.moveanalysing.MoveAnalyser;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveParser;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.moveparsing.MoveParser;
import me.dzikimlecz.chessapi.game.moveparsing.MoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.GamesData;
import me.dzikimlecz.chessapi.game.movestoring.ListMoveDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class GamesManager<K> {
	private final GamesData gamesData;
	private final IMoveParser parser;
	private final IMoveValidator validator;
	private final MoveAnalyser analyser;
	private final Map<K, ChessGame> games;
	private final Map<ChessGame, GameInfo<?, ?>> gameInfoMap;

	public GamesManager() {
		gamesData = new GamesData();
		parser = new MoveParser(gamesData);
		validator = new MoveValidator(gamesData);
		analyser = new CheckAnalyser(gamesData, validator);
		games = new LinkedHashMap<>();
		gameInfoMap = new LinkedHashMap<>();
	}

	public void newGame(K gameKey, ChessEventListener listener) {
		var game = new ChessGame(new ListMoveDatabase(), listener, gamesData);
		if(games.containsKey(gameKey))
			throw new IllegalStateException("Game on this gameKey is already ongoing");
		games.put(gameKey, game);
	}

	public void forceClose(K gameKey) {
		games.remove(gameKey);
		System.gc();
	}

	public boolean close(K gameKey) {
		ChessGame game = getGame(gameKey);
		if (game.isOngoing()) return false;
		forceClose(gameKey);
		return true;
	}

	public void move(K gameKey, String notation) {
		ChessGame game = getGame(gameKey);
		gamesData.setBoard(game.board());
		gamesData.setColor(game.color());
		notation = notation.replaceAll("[^a-hOo0-8PNSBGRWQHK\\-]*", "");
		try {
			game.handleMove(parser.parse(notation).validate(validator).analyse(analyser));
		} catch(Exception e) {
			game.listener().onIllegalMove();
		}
	}

	@NotNull
	private ChessGame getGame(K gameKey) {
		return games.computeIfAbsent(gameKey, gameKey1 -> {
			throw new IllegalArgumentException(
					"There is no game corresponding to gameKey: " + gameKey1.toString()
			);
		});
	}

	public void attachInfo(K gameKey, @NotNull GameInfo<?, ?> info) {
		this.gameInfoMap.put(getGame(gameKey), info);
	}

	public GameInfo<?, ?> getInfo(K gameKey) {
		return gameInfoMap.get(getGame(gameKey));
	}

	public ChessPiece[][] read(K gameKey) {
		Piece[][] pieces = new Piece[8][8];
		ChessGame game = games.get(gameKey);
		if (game == null) return null;
		var board = game.board();
		for (int row = 1; row <= 8; row++)
			for (char line = 'a'; line <= 'h'; line++)
				pieces[row - 1][line - 'a'] = board.square(line, row).piece();
		return pieces;
	}

	public void requestDraw(K gameKey) {
		getGame(gameKey).requestDraw();
	}
}
