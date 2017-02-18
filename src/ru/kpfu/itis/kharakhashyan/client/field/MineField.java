package ru.kpfu.itis.kharakhashyan.client.field;

import ru.kpfu.itis.kharakhashyan.client.Mine;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.JPanel;

//this class contains visible mines, it displays and hides mines during game play
public class MineField extends JPanel{

	private GameField gameField; //parent game field

	private Mine[] battleMines; //array of mines

	public MineField(GameField gameField){
		this.setLayout(null);
		this.gameField = gameField;
		
		battleMines = new Mine[7];
		
		for(int i = 0;i<7;i++){
			battleMines[i] = new Mine(this,i+1,1,i,0);
			battleMines[i].setOpaque(true);
			this.add(battleMines[i]);
		}

		battleMines[0].setBackground(Color.black.darker());
		battleMines[1].setBackground(Color.black.darker());
		battleMines[2].setBackground(Color.black.darker());
		battleMines[3].setBackground(Color.black.darker());
		battleMines[4].setBackground(Color.black.darker());
		battleMines[5].setBackground(Color.black.darker());
		battleMines[6].setBackground(Color.black.darker());

	}

	//gets mine's bounds
	public Rectangle getMineBounds(Mine s) {
		return gameField.getMineBounds(s);
	}

	//hides all mines on the current field
	public void hideAllMines(){
		for(int i = 0;i<7;i++){
			battleMines[i].setOpaque(false);
		}
		repaint();
	}

	//show mine of specified id
	public void showMine(int mineId){
		battleMines[mineId-1].setOpaque(true);
		repaint();
	}


	//sets boom flag of specified mine id
	public boolean setMineBoomById(int mineId){
		battleMines[mineId-1].setHit();
		if(battleMines[mineId-1].getHit()== battleMines[mineId-1].getLength()){
			return true;
		}
		else
			return false;
	}

	//sets the opponent's mine location
	public void setOpponentMineLocation(int[][] mineLocation){
		for(int i = 0;i<7;i++){
			battleMines[i].setVertical(mineLocation[i][0]==0);
			battleMines[i].setRow(mineLocation[i][1]);
			battleMines[i].setColumn(mineLocation[i][2]);
			battleMines[i].setBounds(getMineBounds(battleMines[i]));
		}
	}
	
	//return player's mine location
	public int[][] getPlayerMineLocation(){
		int[][] mineLocation = new int[7][3];
		for(int i = 0;i<7;i++){
			mineLocation[i][0] = battleMines[i].isVertical()?0:1;
			mineLocation[i][1] = battleMines[i].getRow();
			mineLocation[i][2] = battleMines[i].getColumn();
		}
		return mineLocation;
	}
	
	//reset current minefield
	public void resetMineBoard() {
		for(int i = 0;i<7;i++){
			battleMines[i].setVertical(true);
			battleMines[i].setRow(i);
			battleMines[i].setColumn(0);
			battleMines[i].resetHit();
			battleMines[i].setBounds(getMineBounds(battleMines[i]));
		}
		repaint();
	}

}
