package me.dzikimlecz.chessapi;

import me.dzikimlecz.chessapi.game.ChessGame;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import me.dzikimlecz.chessapi.game.moveanalysing.CheckAnalyser;
import me.dzikimlecz.chessapi.game.moveanalysing.MoveAnalyser;
import me.dzikimlecz.chessapi.game.moveparsing.*;
import me.dzikimlecz.chessapi.game.movestoring.GamesData;
import me.dzikimlecz.chessapi.game.movestoring.ListMoveDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class GamesManager<Key> {
	private final GamesData gamesData;
	private final IMoveParser parser;
	private final IMoveValidator validator;
	private final MoveAnalyser analyser;
	private final Map<Key, ChessGame> games;


	public GamesManager() {
		gamesData = new GamesData();
		parser = new MoveParser(gamesData);
		validator = new MoveValidator(gamesData);
		analyser = new CheckAnalyser(gamesData, new MoveValidator(gamesData));
		games = new LinkedHashMap<>();
	}

	public void newGame(Key key, ChessEventListener listener) {
		var game = new ChessGame(new ListMoveDatabase(), listener, gamesData);
		if(games.containsKey(key))
			throw new IllegalStateException("Game on this key is already ongoing");
		games.put(key, game);
	}

	public void forceClose(Key key) {
		games.remove(key);
		System.gc();
	}

	public boolean close(Key key) {
		ChessGame game = getGame(key);
		if (game.isOngoing()) return false;
		forceClose(key);
		return true;
	}

	public void move(Key key, String notation) {
		ChessGame game = getGame(key);
		gamesData.setBoard(game.getBoard());
		gamesData.setColor(game.getColor());
		notation = notation.replaceAll("\\s*[^a-h0-8PNSBGRWQHKOo\\-]*", "");
		game.handleMove(parser.parse(notation).validate(validator).analyse(analyser));
	}

	@NotNull
	private ChessGame getGame(Key key) {
		return games.computeIfAbsent(key, key1 -> {
			throw new IllegalArgumentException(
					"There is no game corresponding to key: " + key1.toString()
			);
		});
	}

	public ChessPiece[][] read(Key key) {
		Piece[][] pieces = new Piece[8][8];
		ChessGame game = getGame(key);
		var board = game.getBoard();
		for (int row = 1; row <= 8; row++)
			for (char line = 'a'; line <= 'h'; line++)
				pieces[row - 1][line - 'a'] = board.getSquare(line, row).getPiece();
		return pieces;
	}

}
