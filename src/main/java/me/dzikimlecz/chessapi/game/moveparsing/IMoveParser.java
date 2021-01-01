package me.dzikimlecz.chessapi.game.moveparsing;

import me.dzikimlecz.chessapi.game.movestoring.MoveData;

import java.util.regex.Pattern;

public interface IMoveParser {

	//patterns
	Pattern simplePawnMove = Pattern.compile(
			"[a-h][1-8]"
	);
	Pattern simplePieceMove = Pattern.compile(
			"[PNSBGRWQHK][a-h][1-8]"
	);
	Pattern pawnMove = Pattern.compile(
			"[a-h1-8][a-h][1-8]"
	);
	Pattern pieceMove = Pattern.compile(
			"[PNSBGRWQHK][a-h1-8][a-h][1-8]"
	);
	Pattern specifiedPawnMove = Pattern.compile(
			"[a-h][1-8][a-h][1-8]"
	);
	Pattern specifiedPieceMove = Pattern.compile(
			"[PNSBGRWQHK][a-h][1-8][a-h][1-8]"
	);
	Pattern castling = Pattern.compile(
			"[Oo0](-[Oo0]){1,2}"
	);

	MoveData parse(String notation);
}
