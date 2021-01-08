package me.dzikimlecz.chessapi.game.events;

import java.util.regex.Pattern;

import static me.dzikimlecz.chessapi.game.events.ChessEvent.Type.*;

public class ChessEvent {
	private static final Pattern movePattern = Pattern.compile(
			"([PBGSNRWQHK]?([a-h][1-8]){1,2})|([Oo0](-[Oo0]){1,2})"
	);
	private static final Pattern closePattern = Pattern.compile(
			"\\s*close\\s\\S*"
	);
	private static final Pattern drawRequestPattern = Pattern.compile(
			"\\s*draw\\s*(white)|(black)\\s*"
	);

	private final Type type;
	private final String notation;

	public ChessEvent(String notation) {
		this.notation = notation;
		if (movePattern.matcher(notation).matches()) type = MOVE;
		else if(closePattern.matcher(notation).matches()) type = CLOSE;
		else if(drawRequestPattern.matcher(notation).matches()) type = DRAW_REQUEST;
		else throw new IllegalArgumentException("Illegal notation");
	}

	public enum Type {
		DRAW_REQUEST, CLOSE, MOVE
	}


	public Type getType() {
		return type;
	}

	public String getNotation() {
		return notation;
	}
}
