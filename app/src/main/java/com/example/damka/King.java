package com.example.damka;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class King extends Soldier{
    private int crown;
    public King(int x, int y, int color,int crown, int radius,int column,int row, int side) {
        super(x, y, color, radius,column,row, side);
        lastX = x;
        lastY = y;
        lastColumn = column;
        lastRow = row;
        this.crown = crown;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(crown);
        canvas.drawCircle(x,y,radius/2, paint);
    }
    @Override
    protected void Move(int x, int y) {
        super.Move(x, y);
    }
}

