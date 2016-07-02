package kabranov.games.bubbleburstgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class ScoreText extends View {
    private int mWidth;
    private int mHeight;
    static int nBubbles=0;
    static int nPopped=0;
    static int nMissed=0;
    
    static int nDied=0;
    static int letterSize=25;


    public ScoreText(Context context) {
        super(context);
    }

    public ScoreText(Context context, AttributeSet attribs) {
        super(context, attribs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint(); 
        paint.setColor(Color.BLUE); 
        paint.setStyle(Style.FILL); 
        canvas.drawPaint(paint);

       // paint.setColor(Color.BLUE);
       // canvas.drawLine(0, 0, mWidth, mHeight, paint);
       // canvas.drawLine(mWidth, 0, 0, mHeight, paint);

        paint.setColor(Color.RED);
        paint.setTextSize(letterSize); 
        canvas.drawText("missed: "+nMissed+"  popped: "+nPopped, 25, 25, paint);
        canvas.save();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        mHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }
}