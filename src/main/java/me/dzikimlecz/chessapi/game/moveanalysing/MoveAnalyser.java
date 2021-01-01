package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.movestoring.MoveData;

public interface MoveAnalyser {

	MoveData analyse(MoveData data);
}
