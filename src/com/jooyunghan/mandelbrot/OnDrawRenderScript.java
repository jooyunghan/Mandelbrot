package com.jooyunghan.mandelbrot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Double2;
import android.renderscript.RenderScript;
import android.util.AttributeSet;
import android.view.View;

public class OnDrawRenderScript extends View {
	private RenderScript mRS;
	private ScriptC_mandelbrot mScript;

	private static final int TEXT_PADDING = 10;
	private static final float TEXT_SIZE = 40.0f;
	private Paint paint_text = new Paint();

	private int width;
	private int height;
	private double scale;
	private double dx_begin;
	private double dy_begin;

	public OnDrawRenderScript(Context context) {
		this(context, null);
	}

	public OnDrawRenderScript(Context context, AttributeSet attr) {
		super(context, attr);
		createScript();
		paint_text.setTextSize(TEXT_SIZE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		long start = System.currentTimeMillis();
		width = canvas.getWidth();
		height = canvas.getHeight();
		setScale();

		Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Allocation out = Allocation.createFromBitmap(mRS, b);
		mScript.set_d_begin(new Double2(dx_begin, dy_begin));
		mScript.set_scale(scale);
		mScript.forEach_root(out);
		out.copyTo(b);
		canvas.drawBitmap(b, 0, 0, null);
		b.recycle();
		
		long end = System.currentTimeMillis();
		canvas.drawText(String.format("%d ms", (end - start)), TEXT_PADDING,
				TEXT_SIZE + TEXT_PADDING, paint_text);
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

	private void createScript() {
		mRS = RenderScript.create(getContext());
		mScript = new ScriptC_mandelbrot(mRS, getResources(), R.raw.mandelbrot);
	}

	@Override
	protected void onDetachedFromWindow() {
		mRS.destroy();
		super.onDetachedFromWindow();
	}

}
