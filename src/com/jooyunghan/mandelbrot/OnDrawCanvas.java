package com.jooyunghan.mandelbrot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class OnDrawCanvas extends View {
	private static final int TEXT_PADDING = 10;
	private static final float TEXT_SIZE = 40.0f;
	private Paint paint_in = new Paint();
	private Paint paint_out = new Paint();
	private Paint paint_text = new Paint();
	
	private static final int ITERATION = 1000;
	private int width;
	private int height;
	private double dx_begin;
	private double dy_begin;
	private double scale;

	public OnDrawCanvas(Context context) {
		this(context, null);
	}

	public OnDrawCanvas(Context context, AttributeSet attr) {
		super(context, attr);
		paint_in.setColor(Color.BLACK);
		paint_out.setColor(Color.WHITE);
		paint_text.setTextSize(TEXT_SIZE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		long start = System.currentTimeMillis();
		width = canvas.getWidth();
		height = canvas.getHeight();
		setScale();
		mandelbrot(canvas);
		long end = System.currentTimeMillis();
		
		canvas.drawText(String.format("%d ms", (end-start)), TEXT_PADDING, TEXT_SIZE + TEXT_PADDING, paint_text);
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

	private void mandelbrot(Canvas canvas) {
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
					canvas.drawPoint(x, y, paint_in);
				} else {
					canvas.drawPoint(x, y, paint_out);
				}
			}
		}
	}
}
