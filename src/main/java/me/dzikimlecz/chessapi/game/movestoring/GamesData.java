package me.dzikimlecz.chessapi.game.movestoring;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Board;

public class GamesData {
	private Board board;
	private Color color;

	public void setBoard(Board board) {
		this.board = board;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Board getBoard() {
		return board;
	}

	public Color getColor() {
		return color;
	}
}
