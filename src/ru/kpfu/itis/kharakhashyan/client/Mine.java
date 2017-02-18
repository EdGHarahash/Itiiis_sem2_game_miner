package ru.kpfu.itis.kharakhashyan.client;

import ru.kpfu.itis.kharakhashyan.client.field.MineField;

import javax.swing.JLabel;


public class Mine extends JLabel {

    private MineField mineField;//the parent minefield

    private int id;//id of the mine

    private int length;//useless field)

    private boolean vertical; //0-horizontal, 1-vertical

    //position
    private int row;//mine's start row
    private int column;//mine's start column

    private int hit;//number of hit

    //mineField - parent field, id - mine's id, column, row - mine's place
    public Mine(MineField mineField, int id, int length, int row, int column) {
        this.mineField = mineField;
        this.id = id;
        this.row = row;
        this.column = column;
        this.length = length;
        this.vertical = true;

        hit = 0;
        this.setBounds(this.mineField.getMineBounds(this));
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean h) {
        vertical = h;
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

    //set start column
    public void setColumn(int column) {
        this.column = column;
    }

    //returns number of hit
    public int getHit() {
        return hit;
    }

    //increment count of hit
    public void setHit() {
        hit++;
    }

    public void resetHit() {
        hit = 0;
    }

}
