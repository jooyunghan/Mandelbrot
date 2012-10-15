package com.jooyunghan.mandelbrot;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.View;

public class MandelbrotView3 extends View {
	private static final int PATCH_SIZE = 100;
	private ArrayList<AsyncTask<Void, Void, Result>> tasks = new ArrayList<AsyncTask<Void, Void, Result>>();
	public ArrayList<Result> queue = new ArrayList<Result>();

	public MandelbrotView3(Context context) {
		super(context);
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

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		cancelTask();
		startTask(w, h);
	}

	private void startTask(int w, int h) {
		
		for (int x = 0; x < w; x += PATCH_SIZE) {
			for (int y = 0; y < h; y += PATCH_SIZE) {
				Rect clip = new Rect(x, y, x + PATCH_SIZE, y + PATCH_SIZE);
				tasks.add(new MandelbrotTask(w, h, clip)
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
			}
		}
	}

	static class Result {
		public Result(Rect clip2, Bitmap bmp2) {
			this.clip = clip2;
			this.bmp = bmp2;
		}

		Rect clip;
		Bitmap bmp;
	}

	class MandelbrotTask extends AsyncTask<Void, Void, Result> {
		private static final int ITERATION = 1000;
		private int width;
		private int height;
		private double dx_begin;
		private double dy_begin;
		private double scale;
		private Rect clip;

		public MandelbrotTask(int width, int height, Rect clip) {
			this.width = width;
			this.height = height;
			this.clip = clip;
			setScale();
		}

		@Override
		protected Result doInBackground(Void... params) {
			return mandelbrot(clip);
		}

		@Override
		protected void onPostExecute(Result result) {
			queue.add(result);
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

		private Result mandelbrot(Rect clip) {
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
						bmp.setPixel(x, y, 0xFF000000);
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
						bmp.setPixel(x, y, 0xFF000000);
					} else { // out, then use iter for color
						// int color = 0xFF000000 | iter;
						bmp.setPixel(x, y, 0xFFFFFFFF);
					}
				}
			}
			return new Result(clip, bmp);
		}

	}
}
