package me.dzikimlecz.chessapi;

import me.dzikimlecz.chessapi.game.ChessGame;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import me.dzikimlecz.chessapi.game.events.ChessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GamesManager<K> {
	protected Map<K, ChessGame> games;
	protected Map<ChessGame, GameInfo<?, ?>> gameInfoMap;

	public GamesManager() {
		games = new HashMap<>();
		gameInfoMap = new HashMap<>();
	}

	public void newGame(K gameKey, ChessEventListener listener) {
		var game = new ChessGame(listener);
		if(games.containsKey(gameKey) && games.get(gameKey).isOngoing())
			throw new IllegalStateException("Game on this gameKey is already ongoing");
		games.put(gameKey, game);
	}

	public void forceClose(K gameKey) {
		var game = getGame(gameKey);
		game.interrupt();
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

	}

	@NotNull
	protected ChessGame getGame(K gameKey) {
		var noGameException = new IllegalArgumentException(
				"There is no game corresponding to gameKey: " + gameKey.toString()
		);
		var game = games.computeIfAbsent(gameKey, gameKey1 -> {
			throw noGameException;
		});
		if (game.isOngoing()) return game;
		games.remove(gameKey);
		throw noGameException;
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

	public void requestDraw(K gameKey, Color requester) {
		var game = getGame(gameKey);
		try {
			game.handleEvent(new ChessEvent("draw" + requester.name().toLowerCase()));
		} catch(InterruptedException e) {
			game.listener().onIllegalMove();
		}
	}
}
