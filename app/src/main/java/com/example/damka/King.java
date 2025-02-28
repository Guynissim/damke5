package com.example.damka;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class King extends Soldier{
    public King(int x, int y, int color, int radius,int column,int row, int side) {
        super(x, y, color, radius,column,row, side);
        lastX = x;
        lastY = y;
        lastColumn = column;
        lastRow = row;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x,y,radius/2, paint);
    }
    @Override
    protected void Move(int x, int y) {
        super.Move(x, y);
    }
}

