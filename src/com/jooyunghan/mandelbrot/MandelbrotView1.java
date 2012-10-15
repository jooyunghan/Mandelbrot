package com.jooyunghan.mandelbrot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class MandelbrotView1 extends View {
	private static final String TAG = "MandelbrotView1";
	private static final int ITERATION = 1000;
	private int width;
	private int height;
	private int x_begin;
	private int y_begin;
	private double dx_begin;
	private double dy_begin;
	private double scale;
	private Rect clip = new Rect();

	public MandelbrotView1(Context context) {
		super(context);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		width = canvas.getWidth();
		height = canvas.getHeight();
		setScale();

		canvas.getClipBounds(clip);
		x_begin = clip.left;
		y_begin = clip.top;
		
		Bitmap bmp = Bitmap.createBitmap(clip.width(), clip.height(),
				Bitmap.Config.ARGB_8888);
		mandelbrot(bmp);
		canvas.drawBitmap(bmp, clip.left, clip.top, null);
		bmp.recycle();
	}

	private void setScale() {
		double scaleX = (width - 1) / 3.0;
		double scaleY = (height - 1) / 2.0;
		if (scaleX < scaleY) {
			scale = scaleX;
			dx_begin = -2.0;
			dy_begin = -(height / 2) / scale;
		} else {
			scale = scaleY;
			dx_begin = -2 - ((width - (int) (3 * scale)) / 2) / scale;
			dy_begin = -1;
		}
	}

	private void mandelbrot(Bitmap bmp) {
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		Log.d(TAG, "mandelbrot(" + w + ", " + h + ")");
		for (int y = 0; y < h; y++) {
			double dy = (y_begin + y) / scale + dy_begin;
			for (int x = 0; x < w; x++) {
				double dx = (x_begin + x) / scale + dx_begin;

				if (dx < -2.0 || dx > 1.0 || dy < -1.0 || dy > 1.0) {
					bmp.setPixel(x, y, 0xFF000000);
				}

				// iterate
				double zx = 0;
				double zy = 0;
				int iter = 0;
				while (iter < ITERATION && zx * zx + zy * zy < 4.0) {
					double zx_1 = zx * zx - zy * zy + dx;
					double zy_1 = 2 * zx * zy + dy;
					zx = zx_1;
					zy = zy_1;
					iter++;
				}
				if (iter == ITERATION) { // (dx, dy) is in mandelbrot set
					bmp.setPixel(x, y, 0xFF000000);
				} else { // out, then use iter for color
					int color = 0xFF000000 | iter;
					bmp.setPixel(x, y, color);
				}
			}
		}
		Log.d(TAG, "mandelbrot(" + w + ", " + h + ") - done");
	}
}
