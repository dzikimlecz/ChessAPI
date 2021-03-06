package me.dzikimlecz.chessapi.game.movestoring;

import me.dzikimlecz.chessapi.game.board.square.Color;
import me.dzikimlecz.chessapi.game.board.Board;

public class GameState {
	private Board board;
	private Color color;

	public void setBoard(Board board) {
		this.board = board;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Board board() {
		return board;
	}

	public Color color() {
		return color;
	}
}
