package ru.kpfu.itis.kharakhashyan.server;

import ru.kpfu.itis.kharakhashyan.server.player.PlayerStatus;

import java.io.Serializable;

//objects of this class are sent and received by client and server to update game play
public class Message implements Serializable {

	private PlayerStatus messageType; //message type (NOT_CONNECTED, CONNECTED, READY, TURN, WAIT, GAMEOVER)

	private int[][][] minePosition; //position of mine

	private int[][] mineLocation; //mine layout

	private int row; //number of row

	private int column; //number of column

	private boolean hit; //hit or win

	private boolean mineBoombed; //mine boombed


	public PlayerStatus getMessageType() {
		return messageType;
	}

	public void setMessageType(PlayerStatus messageType) {
		this.messageType = messageType;
	}

	public int[][][] getMinePosition() {
		return minePosition;
	}

	public void setMinePosition(int[][][] minePosition) {
		this.minePosition = minePosition;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public boolean isHit() {
		return hit;
	}

	public void setHit(boolean hit) {
		this.hit = hit;
	}

	public int[][] getMineLocation() {
		return mineLocation;
	}

	public void setMineLocation(int[][] mineLocation) {
		this.mineLocation = mineLocation;
	}

	public boolean isMineBoombed() {
		return mineBoombed;
	}

	public void setMineBoombed(boolean mineBoombed) {
		this.mineBoombed = mineBoombed;
	}
	

	
}
