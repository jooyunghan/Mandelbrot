package com.jooyunghan.mandelbrot;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

public class AsyncTileConc4 extends View {
	private static final int TEXT_PADDING = 10;
	private static final float TEXT_SIZE = 40.0f;
	private Paint paint_text = new Paint();
	
	private static final int COLOR_IN = 0xFFaa0033;
	private static final int COLOR_OUT = 0xffffffff;
	
	private static final int PATCH_SIZE = 100;
	private ArrayList<AsyncTask<Void, Void, Result>> tasks = new ArrayList<AsyncTask<Void, Void, Result>>();
	public ArrayList<Result> queue = new ArrayList<Result>();
	private Executor exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private long start;
	private long end1;
	private long end2;

	private int width;
	private int height;
	private double dx_begin;
	private double dy_begin;
	private double scale;
	
	public AsyncTileConc4(Context context) {
		this(context, null);
	}
	
	public AsyncTileConc4(Context context, AttributeSet attr) {
		super(context, attr);
		paint_text.setTextSize(TEXT_SIZE);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (queue.isEmpty()) {
			start = System.currentTimeMillis();
			startTask(canvas.getWidth(), canvas.getHeight());
			end1 = System.currentTimeMillis();
			canvas.drawText(String.format("#1 onDraw(): %d ms", (end1-start)), TEXT_PADDING, TEXT_SIZE + TEXT_PADDING, paint_text);
		} else {
			for (Result r : queue) {
				canvas.drawBitmap(r.bmp, r.x, r.y, null);
			}
			end2 = System.currentTimeMillis();
			canvas.drawText(String.format("#1 onDraw(): %d ms", (end1-start)), TEXT_PADDING, TEXT_SIZE + TEXT_PADDING, paint_text);
			canvas.drawText(String.format("#2 onDraw(): %d ms", (end2-start)), TEXT_PADDING, (TEXT_SIZE + TEXT_PADDING)*2, paint_text);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		cancelTask();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		cancelTask();
	}

	private void cancelTask() {
		for (AsyncTask<Void, Void, Result> task : tasks)
			task.cancel(true);
		tasks.clear();
		for (Result r : queue)
			r.bmp.recycle();
		queue.clear();
	}

	private void startTask(int w, int h) {
		width = w;
		height = h;
		setScale();
		for (int x = 0; x < w; x += PATCH_SIZE) {
			for (int y = 0; y < h; y += PATCH_SIZE) {
				tasks.add(new MandelbrotTask(x, y).executeOnExecutor(exec));
			}
		}
	}

	static class Result {
		public Result(int x, int y, Bitmap bmp2) {
			this.x = x;
			this.y = y;
			this.bmp = bmp2;
		}

		int x;
		int y;
		Bitmap bmp;
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

	class MandelbrotTask extends AsyncTask<Void, Void, Result> {
		private static final int ITERATION = 1000;

		private int x_begin;
		private int y_begin;

		public MandelbrotTask(int x, int y) {
			this.x_begin = x;
			this.y_begin = y;
		}

		@Override
		protected Result doInBackground(Void... params) {
			return mandelbrot();
		}

		@Override
		protected void onPostExecute(Result result) {
			queue.add(result);
			postInvalidate();
		}

		private Result mandelbrot() {
			Bitmap bmp = Bitmap.createBitmap(PATCH_SIZE, PATCH_SIZE,
					Bitmap.Config.ARGB_8888);

			int w = bmp.getWidth();
			int h = bmp.getHeight();
			for (int y = 0; y < h; y++) {
				double dy = (y_begin + y) / scale + dy_begin;
				for (int x = 0; x < w; x++) {
					double dx = (x_begin + x) / scale + dx_begin;

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
					} else { // out, then use iter for color
						bmp.setPixel(x, y, COLOR_OUT);
					}
				}
			}
			return new Result(x_begin, y_begin, bmp);
		}

	}
}
