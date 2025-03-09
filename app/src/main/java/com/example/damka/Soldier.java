package com.example.damka;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Soldier extends Shape {
    protected Paint paint;
    protected int radius, lastX, lastY, column, row, lastColumn, lastRow;
    protected final int side;

    public Soldier(int x, int y, int color, int radius, int column, int row, int side) {
        super(x, y, color);
        this.radius = radius;
        this.side = side;
        paint = new Paint();
        paint.setColor(color);
        lastX = x;
        lastY = y;
        this.column = column;
        this.row = row;
        lastColumn = column;
        lastRow = row;
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, paint);
    }

    protected void Move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isIdentical(Soldier selectedSoldier) {
        if (x == selectedSoldier.x && y == selectedSoldier.y && side == selectedSoldier.side)
            return true;
        return false;
    }
}
