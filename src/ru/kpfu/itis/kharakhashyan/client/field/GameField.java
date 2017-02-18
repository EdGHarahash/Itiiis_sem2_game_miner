package ru.kpfu.itis.kharakhashyan.client.field;

import ru.kpfu.itis.kharakhashyan.client.Client;
import ru.kpfu.itis.kharakhashyan.client.Mine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import java.awt.Graphics2D;

//this class make grid of cells and mine panels
public class GameField extends JLayeredPane {

    private static final Dimension PANE_SIZE = new Dimension(300, 300);//constant field size

    private Client client;

    private GridField gridField;//cell grid field

    private MineField mineField;//the mine layout field

    private boolean topRect;//transparent layer flag

    private int mineBoombed;//count of boombed mines

    PlayerMouseAdapter playerMouseAdapter;//adapter for mouse

    //initialize new instance of new game field
    public GameField(Client client) {
        this.client = client;

        gridField = new GridField(this);
        gridField.setSize(PANE_SIZE);

        mineField = new MineField(this);
        mineField.setSize(PANE_SIZE);
        mineField.setOpaque(false);

        setSize(PANE_SIZE);
        add(gridField, JLayeredPane.DEFAULT_LAYER);
        add(mineField, JLayeredPane.PALETTE_LAYER);

        playerMouseAdapter = new PlayerMouseAdapter(this);
    }

    //gets the mine bounds
    //return position and width, height of mine lable to be draw.
    public Rectangle getMineBounds(Mine s) {
        int width, height;
        Point coordinates = new Point(20 * s.getColumn(), 20 * s.getRow());

        if (s.isVertical()) {
            height = 20;
            width = 20 * s.getLength();
        } else {
            height = 20 * s.getLength();
            width = 20;
        }

        //set Flags after mine has fixed position and ready to draw over these cells
        gridField.setMineOccupiedCellFlag(s); //set mine grid flags

        return new Rectangle(coordinates.x + 2, coordinates.y + 2, width - 4, height - 4);
    }

    //class for handling mouse events
    private class PlayerMouseAdapter extends MouseAdapter {

        private GameField gameField;

        private int dragLabelWidth;

        private int dragLabelHeight;

        private Mine clickedMine = null;

        private Point dropCell;

        private Point safeCell;

        //initialize new instance of mouse adapter
        public PlayerMouseAdapter(GameField gameField) {
            this.gameField = gameField;
            safeCell = new Point();
        }

        public void mouseClicked(MouseEvent me) {
            if (mineField.getComponentAt(me.getPoint()) instanceof Mine) {
                clickedMine = (Mine) mineField.getComponentAt(me.getPoint());
                safeCell.setLocation(clickedMine.getRow(), clickedMine.getColumn());

                gridField.clearMineCell(clickedMine);

                clickedMine.setVertical(!clickedMine.isVertical());
                //if mine collide then dont change orientation
                if (gridField.isMineCollide(me.getPoint(), clickedMine))
                    clickedMine.setVertical(!clickedMine.isVertical());

                dropCell = gameField.gridField.matchMineCellAtPoint(me.getPoint(), clickedMine, safeCell);

                clickedMine.setRow(dropCell.x);
                clickedMine.setColumn(dropCell.y);

                clickedMine.setBounds(getMineBounds(clickedMine));
                gridField.resetCellHighlight();
            }
        }

        public void mousePressed(MouseEvent me) {

            if (mineField.getComponentAt(me.getPoint()) instanceof Mine) {
                clickedMine = (Mine) mineField.getComponentAt(me.getPoint());
                safeCell.setLocation(clickedMine.getRow(), clickedMine.getColumn());

                dragLabelWidth = 15 - 4;
                dragLabelHeight = 15 - 4;

                int x = me.getPoint().x - dragLabelWidth;
                int y = me.getPoint().y - dragLabelHeight;
                clickedMine.setLocation(x, y);

                mineField.repaint();

                dropCell = gameField.gridField.matchMineCellAtPoint(me.getPoint(), clickedMine, safeCell);

            }
        }

        public void mouseDragged(MouseEvent me) {
            if (clickedMine == null) {
                return;
            }

            if (!clickedMine.getParent().getBounds().contains(me.getPoint())) return;

            int x = me.getPoint().x - dragLabelWidth;
            int y = me.getPoint().y - dragLabelHeight;

            clickedMine.setLocation(x, y);

            repaint(); //minefield repaint


            dropCell = gameField.gridField.matchMineCellAtPoint(me.getPoint(), clickedMine, safeCell);
            safeCell = dropCell;
        }

        public void mouseReleased(MouseEvent me) {
            if (clickedMine == null) {
                return;
            }

            gridField.clearMineCell(clickedMine);

            clickedMine.setRow(dropCell.x);
            clickedMine.setColumn(dropCell.y);
            clickedMine.setBounds(getMineBounds(clickedMine));

            repaint();
            clickedMine = null;
            gridField.resetCellHighlight();
        }
    }

    //enable mouse events
    public void enableMouseEvents(int senderId) {
        if (senderId == 1) {
            this.addMouseListener(playerMouseAdapter);
            this.addMouseMotionListener(playerMouseAdapter);
        } else {
            gridField.enableMouseEvents();
        }

        fade(false);
    }

    //disable mouse events
    public void disableMouseEvents(int senderId) {
        if (senderId == 1) {
            this.removeMouseListener(playerMouseAdapter);
            this.removeMouseMotionListener(playerMouseAdapter);
        } else {
            gridField.disableMouseEvents();
        }

        fade(true);
    }

    //hide all mines
    public void hideAllMines() {
        mineField.hideAllMines();
    }


    //sets the opponent mine position
    public void setOpponentMinePosition(int[][][] flags) {
        gridField.setOpponentMinePosition(flags);
    }

    //get my mine position
    public int[][][] getPlayerMinePosition() {
        return gridField.getPlayerMinePosition();
    }

    //sets the opponent mine location.
    public void setOpponentMineLocation(int[][] mineLocation) {
        mineField.setOpponentMineLocation(mineLocation);
        repaint();
    }

    //gets the my mine location
    public int[][] getPlayerMineLocation() {
        return mineField.getPlayerMineLocation();
    }

    //assign the mine boom of specified id
    public boolean setMineBoomById(int mineId) {
        if (mineField.setMineBoomById(mineId)) {
            System.out.println("Mine id:" + mineId + " destroyed.");
            mineBoombed++;
            mineField.showMine(mineId);
            return true;
        }
        return false;
    }

    //sets opponent's mouse hit at specified cell position
    public void setHitAtCell(int row, int column) {
        gridField.setHitAtCell(row, column);
    }

    //sets player's mouse hit at cell position
    public void onMouseHitAtCell(int row, int column, boolean hit, boolean sdst) {
        client.opponentMouseHit(row, column, hit, sdst);
    }

    //toggle transparent layer on game field
    public void fade(boolean f) {
        topRect = f;
        repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (topRect) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }


    //reset current field
    public void resetGameBoard() {
        gridField.resetGridBoard();
        gridField.resetCellHighlight();
        mineField.resetMineBoard();
        mineBoombed = 0;
    }

    public int getMineBoombed() {
        return mineBoombed;
    }


}
