package ru.kpfu.itis.kharakhashyan.client.field;

import ru.kpfu.itis.kharakhashyan.client.Cell;
import ru.kpfu.itis.kharakhashyan.client.Mine;

import java.awt.*;
import javax.swing.JPanel;

public class GridField extends JPanel {

    private GameField gameField;//parent game field

    private Cell[][] cells = new Cell[15][15];//array of cells

    private int[][][] flags = new int[15][15][2];//array of mine position and boom flags
    //x,y,mine id and boom array

    //initialize a new grid field
    public GridField(GameField gameField) {
        this.gameField = gameField;

        setLayout(new GridLayout(15, 15, 0, 0));

        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++) {
                add(cells[i][j] = new Cell(i, j, this));

                flags[i][j][0] = 0; //isMine
                flags[i][j][1] = 0; //isHit
            }
        this.setPreferredSize(new Dimension(300, 300));
    }

    //reset grid field
    public void resetGridBoard() {
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++) {
                flags[i][j][0] = 0; //isMine
                flags[i][j][1] = 0; //isHit
            }
    }

    //enable mouse events
    public void enableMouseEvents() {
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++)
                cells[i][j].enableCellMouseEvents();
    }

    //disable mouse events
    public void disableMouseEvents() {
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++)
                cells[i][j].disableCellMouseEvents();
    }

    //this method mark cells that are a mine (due to mine id)
    public void setMineOccupiedCellFlag(Mine s) {
        //set mine flags
        int rowStart = s.getRow(), colStart = s.getColumn();
        int rowEnd, colEnd;

        if (s.isVertical()) {
            rowEnd = rowStart;
            colEnd = colStart + s.getLength() - 1;

        } else {
            colEnd = colStart;
            rowEnd = rowStart + s.getLength() - 1;
        }

        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = colStart; c <= colEnd; c++) {
                flags[r][c][0] = s.getId();
            }
        }

    }

    //clear mine cells
    public void clearMineCell(Mine s) {
        int rowStart = s.getRow(), colStart = s.getColumn();
        int rowEnd, colEnd;

        if (s.isVertical()) {
            rowEnd = rowStart;
            colEnd = colStart + s.getLength() - 1;

        } else {
            colEnd = colStart;
            rowEnd = rowStart + s.getLength() - 1;
        }

        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = colStart; c <= colEnd; c++) {
                flags[r][c][0] = 0;
            }
        }

    }

    //peace of old now useless code
    public boolean isMineCollide(Point point, Mine s) {
        return false;
    }

    //reset cell hover color
    public void resetCellHighlight() {
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++)
                cells[i][j].resetHighlight();
    }

    //idk
    public Point matchMineCellAtPoint(Point point, Mine s, Point safeCell) {
        int mineLength = 1;
        int rowStart, colStart, rowEnd, colEnd;

        Cell startCell = (Cell) this.getComponentAt(point);
        rowStart = startCell.getRow();
        colStart = startCell.getColumn();

        resetCellHighlight();

        if (s.isVertical()) {
            if (colStart > 15 - mineLength) colStart = 15 - mineLength;
            colEnd = colStart + mineLength;
            if (colEnd > 15) colEnd = 15;

            for (int c = colStart; c < colEnd; c++) {
                //if collision, set safeCell
                if (flags[rowStart][c][0] != 0 && flags[rowStart][c][0] != s.getId()) {
                    rowStart = safeCell.x;
                    colStart = safeCell.y;

                    if (colStart > 15 - mineLength) colStart = 15 - mineLength;
                    colEnd = colStart + mineLength;
                    if (colEnd > 15) colEnd = 15;

                    break;
                }
            }

            for (int c = colStart; c < colEnd; c++) {
                cells[rowStart][c].highLight();
            }

        } else {
            if (rowStart > 15 - mineLength) rowStart = 15 - mineLength;
            rowEnd = rowStart + mineLength;
            if (rowEnd > 15) rowEnd = 15;

            for (int r = rowStart; r < rowEnd; r++) {
                //if collision, set safeCell
                if (flags[r][colStart][0] != 0 && flags[r][colStart][0] != s.getId()) {
                    rowStart = safeCell.x;
                    colStart = safeCell.y;

                    if (rowStart > 15 - mineLength) rowStart = 15 - mineLength;
                    rowEnd = rowStart + mineLength;
                    if (rowEnd > 15) rowEnd = 15;

                    break;
                }
            }

            for (int r = rowStart; r < rowEnd; r++) {
                cells[r][colStart].highLight();
            }
        }
        return new Point(rowStart, colStart);
    }


    //sets the opponent mine position
    public void setOpponentMinePosition(int[][][] flags2) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                flags[i][j][0] = flags2[i][j][0]; //isMine
                flags[i][j][1] = flags2[i][j][0]; //isHit
            }
        }
        System.out.println("Enemy mines has been located");
    }

    //gets the player's mine position
    public int[][][] getPlayerMinePosition() {
        return flags;
    }

    public void getSum(int row, int column){
        int mineQuan=0;
        int coord[][]= gameField.getPlayerMineLocation();
        if  (row>0 && row<14 && 0<column && column<14) {
            for (int r = 0; r < 7; r++) {
                if ((Math.abs(coord[r][1] - row)<=1) && (Math.abs(coord[r][2] - column)<=1)) {
                    mineQuan++;
                }
            }
        }
        cells[row][column].setSum(mineQuan);
    }

    //sets the hit at specified cell
    public int setHitAtCell(int row, int column) {
        int mineQuan=0;
        flags[row][column][1] = 1; //isHit
        int coord[][]= gameField.getPlayerMineLocation();
        if  (row>0 && row<14 && 0<column && column<14) {
            for (int r = 0; r < 7; r++) {
                    if ((Math.abs(coord[r][1] - row)<=1) && (Math.abs(coord[r][2] - column)<=1)) {
                        mineQuan++;
                    }
            }
        }
        cells[row][column].setSum(mineQuan);

        if (flags[row][column][0] > 0) {
            System.out.println("Mine id hit:" + flags[row][column][0]);
            cells[row][column].setMineBoom();
            gameField.setMineBoomById(flags[row][column][0]);
        } else {
            cells[row][column].setMissedHit();
        }
        return flags[row][column][0];
    }

    //player's mouse action on cell
    public void onMouseHitAtCell(int row, int column) {

        flags[row][column][1] = 1; //isHit

        if (flags[row][column][0] > 0) {
            System.out.println("Mine id hit:" + flags[row][column][0]);

            cells[row][column].setMineBoom();

            gameField.onMouseHitAtCell(row, column, true, gameField.setMineBoomById(flags[row][column][0]));
            return;
        }
        cells[row][column].setMissedHit();
        gameField.onMouseHitAtCell(row, column, false, false);

    }

}
