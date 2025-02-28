package com.example.damka;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class Square extends Shape {

    private Paint paint;
    protected Soldier soldier;
    protected int column, row, width, height;

    public Square(int x, int y, int color, int width, int height, int column, int row) {
        super(x, y, color);
        this.width = width;
        this.height = height;
        this.column = column;
        this.row = row;
        paint = new Paint();
        paint.setColor(color);
        soldier = null;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint);
        if(this.soldier!=null)
            soldier.draw(canvas);
    }

    public boolean didUserTouchMe(int xu, int yu) {
        return (xu > x && xu < x + width && yu > y && yu < y + height);
    }

    public int getState() {
        if(this.soldier==null)
            return 0;//empty square
        if(this.soldier.side == 1)
        {
            if(soldier instanceof King)
                return 3;//side1, king
            else
                return 1;//side1, soldier
        }
        if(this.soldier.side == 2)
        {
            if(soldier instanceof King)
                return 4;//side2, king
            else
                return 2;//side2, soldier
        }
        Log.d("getState()", "Error in getState()");
        return -1;//impossible, just preventing error
    }
}

