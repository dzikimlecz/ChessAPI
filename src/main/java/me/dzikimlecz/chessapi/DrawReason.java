package me.dzikimlecz.chessapi;

public enum DrawReason {
	FIFTY_MOVES_WITHOUT_PAWN,
	LACK_OF_PIECES,
	PLAYERS_DECISION,
	STALE_MATE,
	TRIPLE_POSITION_REPEAT,
}
