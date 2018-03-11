package de.teutronic.freewifi_lueneburg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Andreas Hertel on 09.03.2018.
 */

/*public class DirectionView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder surfaceHolder;
*/
public class DirectionView extends View {

    private float winkel = 0;
    private Paint zeichenfarbe = new Paint();
    private float massstab;
    private int breite;
    private int hoehe;
    private int laenge;

    public DirectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
   //     surfaceHolder = getHolder();
   //     surfaceHolder.addCallback(this);
   //     surfaceHolder.setType(SurfaceHolder,SURFACE_TYPE_PUSH_BUFFERS);
        init();
    }
    public DirectionView(Context context) {
        super(context);
    //    surfaceHolder = getHolder();
    //    surfaceHolder.addCallback(this);
        //     surfaceHolder.setType(SurfaceHolder,SURFACE_TYPE_PUSH_BUFFERS);
        init();
    }

    private void init() {
        massstab = getResources().getDisplayMetrics().density;
        zeichenfarbe.setAntiAlias(true);
        zeichenfarbe.setColor(Color.WHITE);
        zeichenfarbe.setStyle(Paint.Style.FILL);
        breite = getWidth();
        hoehe = getHeight();
        laenge = Math.min(breite,hoehe);

    }

    public void setWinkel(float winkel) {
        this.winkel = winkel;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        if (laenge == 0) {
           breite = canvas.getWidth();
           hoehe = canvas.getHeight();
           laenge = Math.min(breite,hoehe);
        }
        if (laenge != 0) {
            Path pfad = new Path();
            pfad.moveTo(0, -laenge / 2);
            pfad.lineTo(laenge / 8, laenge / 2);
            pfad.lineTo(-laenge / 8, laenge / 2);
            pfad.close();
            canvas.translate(breite / 2, hoehe / 2);
            canvas.rotate(winkel);
            canvas.drawPath(pfad, zeichenfarbe);
        }
    }

    /*
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        zeichenfarbe.setAntiAlias(true);
        zeichenfarbe.setColor(Color.WHITE);
        zeichenfarbe.setStyle(Paint.Style.FILL);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    */
}
