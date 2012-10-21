package com.jooyunghan.mandelbrot;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MandelbrotView2 extends View {
	private static final int COLOR_IN = 0xFFaa3300;
	private static final int COLOR_OUT = 0xffffffff;
	
	private static final String TAG = "MandelbrotView2";
	private AsyncTask<Void, Result, Void> task;
	public ArrayList<Result> queue = new ArrayList<Result>();
	private int width;
	private int height;
	private int count;

	public MandelbrotView2(Context context) {
		this(context, null);
	}
	
	public MandelbrotView2(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		for (Result r : queue) {
			canvas.drawBitmap(r.bmp, r.clip.left, r.clip.top, null);
		}

	}
	
	@Override
	protected void onDetachedFromWindow() {
		if (task != null) {
			task.cancel(true);
			task = null;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (task != null) {
			task.cancel(true);
			task = null;
		}
		queue.clear();
		task = new MandelbrotTask(w, h).execute();
	}

	static class Result {
		public Result(Rect clip2, Bitmap bmp2) {
			this.clip = clip2;
			this.bmp = bmp2;
		}

		Rect clip;
		Bitmap bmp;
	}

	class MandelbrotTask extends AsyncTask<Void, Result, Void> {
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
		protected Void doInBackground(Void... params) {
			for (int x = 0; x < width; x += 100) {
				for (int y = 0; y < height; y += 100) {
					Rect clip = new Rect(x, y, x + 100, y + 100);
					Bitmap bmp = mandelbrot(clip);
					if (isCancelled()) {
						return null;
					}
					publishProgress(new Result(clip, bmp));
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Result... values) {
			for (Result r : values) {
				queue.add(r);
			}
			postInvalidate();
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

		private Bitmap mandelbrot(Rect clip) {
			int x_begin = clip.left;
			int y_begin = clip.top;

			Bitmap bmp = Bitmap.createBitmap(clip.width(), clip.height(),
					Bitmap.Config.ARGB_8888);

			int w = bmp.getWidth();
			int h = bmp.getHeight();
			for (int y = 0; y < h; y++) {
				double dy = (y_begin + y) / scale + dy_begin;
				for (int x = 0; x < w; x++) {
					double dx = (x_begin + x) / scale + dx_begin;

					if (dx < -2.0 || dx > 1.0 || dy < -1.0 || dy > 1.0) {
						bmp.setPixel(x, y, COLOR_OUT);
					}

					if (isCancelled())
						return null;

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
						bmp.setPixel(x, y, COLOR_IN);
					} else {
						bmp.setPixel(x, y, COLOR_OUT);
					}
				}
			}
			return bmp;
		}

	}
}
