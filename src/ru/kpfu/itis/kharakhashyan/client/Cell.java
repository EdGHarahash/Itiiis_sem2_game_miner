package ru.kpfu.itis.kharakhashyan.client;

import ru.kpfu.itis.kharakhashyan.client.field.GridField;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.LineBorder;

//class to create cell inside grid of game field
public class Cell extends JPanel {

	Color backColor = new Color (54, 44, 25);//creates your new color

	JLabel blueLabel;

	private int row;//number of cell's row

    private int column;//number of cell's column

	private int isMouseOn;//the is mouse on

    private int mineBoom; //0-no hit 1-no mine 2-BOOM

	private int sum;

    private GridField gridField;//grid field of this cell

    private ClickListener cellMouseListener;//cell mouse listener

    //creates a new cell in special gridboard on special row and column
    public Cell(int row, int column, GridField gui) {

		sum=0;
		blueLabel = new JLabel(""+sum);
		blueLabel.setLocation(10, 20);
		blueLabel.setSize(30, 30);

		blueLabel.setForeground(backColor);
		this.add(blueLabel);
      this.row = row;
      this.column = column;
      this.gridField = gui;
      isMouseOn = 0;
      mineBoom = 0;
      this.setPreferredSize(new Dimension(30,30));
      this.setMinimumSize(new Dimension(30, 30));
      this.setMaximumSize(new Dimension(30, 30));
      
      setBorder(new LineBorder(Color.GRAY.darker(), 1));   // Set cell's border

      cellMouseListener = new ClickListener();
    }

    //enable mouse events for this cell
	public void enableCellMouseEvents(){
			this.addMouseListener(cellMouseListener);
	}
	
	//disable mouse events for this cell
	public void disableCellMouseEvents(){
			this.removeMouseListener(cellMouseListener);
	}

    public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	//set cell's background color
	public void highLight(){
    	isMouseOn=1;
    	repaint();
    }

	//reset cell background color
    public void resetHighlight(){
    	isMouseOn=0;
    	mineBoom =0;
    	repaint();
    }

	//repaint cell
    protected void paintComponent(Graphics g) {
		gridField.getSum(row,column);
      super.paintComponent(g);

      //Change Color of cell if mouse over and exit
      if(mineBoom > 0){
		  blueLabel.setText(""+sum);
    	  if(mineBoom ==1){
			  if  (row>0 && row<14 && 0<column && column<14) {

				  int cGB = 54 * sum - 3 * sum * sum;
				  Color cellColor = new Color(255, 255 - cGB, 255 - cGB);
				  g.setColor(cellColor);
			  }else{g.setColor(Color.cyan);}
    	      g.fillRect(0, 0, getWidth(), getHeight());
          }else if(mineBoom ==2){
              g.setColor(Color.red.brighter());
              g.fillRect(0, 0, getWidth(), getHeight());
          }
      }else if(isMouseOn==1){
		  blueLabel.setForeground(Color.RED.darker());
	      g.setColor(Color.RED.darker());
	      g.fillRect(0, 0, getWidth(), getHeight());
      }else if(isMouseOn==0){
		  blueLabel.setForeground(backColor);
          g.setColor(backColor);
          g.fillRect(0, 0, getWidth(), getHeight());
      }

    }
    
	//set flag if mine BOOmbed
    public void setMineBoom(){
    	mineBoom = 2;
    	repaint();
    }
    
	//set flag if hit missed
    public void setMissedHit(){
    	mineBoom = 1;
    	repaint();    	
    }

    public void setSum(int sum){
		this.sum=sum;
	}
    
	//mouse handler class
	private class ClickListener extends MouseAdapter {

    	@Override
    	public void mouseClicked(MouseEvent e) {
	    	if(mineBoom > 0)return;
	    	mineBoom = 1;
	    	gridField.onMouseHitAtCell(row,column);
	    	repaint();
	    }

	    @Override
    	public void mouseEntered(MouseEvent e) {
	    	isMouseOn = 1;
	    	repaint();
	    }

	    @Override
    	public void mouseExited(MouseEvent e) {
	    	isMouseOn = 0;
	    	repaint();
	    }
	    
	 }
}
