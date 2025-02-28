package com.example.damka;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Soldier extends Shape {
    protected Paint p;
    protected int radius, lastX, lastY, column, row, lastColumn, lastRow;
    protected final int side;

    public Soldier(int x, int y, int color, int radius, int column, int row, int side) {
        super(x, y, color);
        this.radius = radius;
        this.side = side;
        p = new Paint();
        p.setColor(color);
        lastX = x;
        lastY = y;
        this.column = column;
        this.row = row;
        lastColumn = column;
        lastRow = row;
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, p);
    }

    protected void Move(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
