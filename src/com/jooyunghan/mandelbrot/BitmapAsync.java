package com.jooyunghan.mandelbrot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

public class BitmapAsync extends View {
	private static final int TEXT_PADDING = 10;
	private static final float TEXT_SIZE = 40.0f;
	private Paint paint_text = new Paint();

	private Bitmap bmp;
	private AsyncTask<Void, Void, Bitmap> task;
	private long start;
	private long end1;
	private long end2;

	public BitmapAsync(Context context) {
		this(context, null);
	}

	public BitmapAsync(Context context, AttributeSet attr) {
		super(context, attr);
		paint_text.setTextSize(TEXT_SIZE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (bmp == null) {
			start = System.currentTimeMillis();
			task = new MandelbrotTask(canvas.getWidth(), canvas.getHeight()).execute();
			end1 = System.currentTimeMillis();
			canvas.drawText(String.format("#1 onDraw(): %d ms", (end1-start)), TEXT_PADDING, TEXT_SIZE + TEXT_PADDING, paint_text);
		} else {
			canvas.drawBitmap(bmp, 0, 0, null);
			end2 = System.currentTimeMillis();
			canvas.drawText(String.format("#1 onDraw(): %d ms", (end1-start)), TEXT_PADDING, TEXT_SIZE + TEXT_PADDING, paint_text);
			canvas.drawText(String.format("#2 onDraw(): %d ms", (end2-start)), TEXT_PADDING, (TEXT_SIZE + TEXT_PADDING)*2, paint_text);
		}
	}

	class MandelbrotTask extends AsyncTask<Void, Void, Bitmap> {
		private static final int ITERATION = 1000;
		private int width;
		private int height;
		private double dx_begin;
		private double dy_begin;
		private double scale;
		
		public MandelbrotTask(int width, int height) {
			this.width = width;
			this.height = height;
			setScale();
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			mandelbrot(bmp);
			return bmp;		
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			setBmp(result);
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
			for (int y = 0; y < height; y++) {
				double dy = y / scale + dy_begin;
				for (int x = 0; x < width; x++) {
					double dx = x / scale + dx_begin;

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
					if (iter == ITERATION) {
						bmp.setPixel(x, y, Color.BLACK);
					} else {
						bmp.setPixel(x, y, Color.WHITE);
					}
				}
			}
		}
	}

	public void setBmp(Bitmap bmp2) {
		if (bmp != null) {
			bmp.recycle();
			bmp = null;
		}
		bmp = bmp2;
		invalidate();
	}
}
