package com.shanbay.stageprogress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Otway on 2018/7/6.
 */

public class BeadProgressBar extends View {

	private int mNodeNum;
	private int mNodeIndex;
	private Drawable mNodeIcon;

	private final int mGap = 2;
	private int mRadius;
	private Paint mPaint;
	private List<Integer> mNodeList;


	public BeadProgressBar(Context context) {
		this(context, null);
	}

	public BeadProgressBar(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BeadProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BeadProgressBar);
		mNodeNum = array.getInteger(R.styleable.BeadProgressBar_bead_node_num, 3);
		mNodeIndex = array.getInteger(R.styleable.BeadProgressBar_bead_node_index, 1);
		mNodeIcon = array.getDrawable(R.styleable.BeadProgressBar_bead_node_icon);
		int axisColor = array.getColor(R.styleable.BeadProgressBar_bead_axis_color, Color.parseColor("#e0e0e0"));
		array.recycle();

		initNodeDrawable(mNodeIcon);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(axisColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2.0f);

		mNodeList = new ArrayList<>();
	}

	private void initNodeDrawable(Drawable drawable) {
		if (drawable != null) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			mRadius = drawable.getIntrinsicWidth() / 2;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int minGap = mRadius * 4;

		int minWidth = 2 * mRadius + mGap * 2 + (mNodeNum - 1) * minGap;
		int minHeight = mRadius * 2 + 2 * mGap; // 上下各留1像素

		int width;
		int height;

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		width = Math.max(widthSize, minWidth);
		height = Math.max(heightSize, minHeight);

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		initNodeList();
	}

	private void initNodeList() {
		mNodeList.clear();

		if (mNodeNum < 2) {
			throw new IllegalArgumentException("The node number must greater than one");
		}

		if (mNodeIndex >= mNodeNum) {
			throw new IllegalArgumentException("The index of node out of the range of node");
		}

		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();

		int startX = paddingLeft + mRadius + mGap;
		int endX = getMeasuredWidth() - paddingRight - mGap - mRadius;

		int space = (endX - startX) / (mNodeNum - 1);

		for (int i = 0; i < mNodeNum; i++) {
			int x = startX + i * space;
			if (i == mNodeNum - 1) {
				x = endX;
			}
			mNodeList.add(x);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int markH = mRadius * 2 / 5; // 刻度高度
		int startX = mNodeList.get(0);
		int endX = mNodeList.get(mNodeList.size() - 1);
		int height = getHeight();

		mPaint.setStrokeWidth(2.0f);
		canvas.drawLine(startX, height / 2, endX, height / 2, mPaint);

		int startY = (height - markH) / 2;
		int endY = (height + markH) / 2;

		mPaint.setStrokeWidth(4.0f);
		int indexX = 0;
		for (int i = 0, size = mNodeList.size(); i < size; i++) {
			int pointX = mNodeList.get(i);
			canvas.drawLine(pointX, startY, pointX, endY, mPaint);

			if (i == mNodeIndex) {
				indexX = pointX;
			}
		}

		Bitmap bitmap = ((BitmapDrawable) mNodeIcon).getBitmap();
		canvas.drawBitmap(bitmap, indexX - mRadius, height / 2 - mRadius, mPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && !mNodeList.isEmpty()) {
			float x = event.getX();
			for (int i = 0, size = mNodeList.size(); i < size; i++) {
				int nodeX = mNodeList.get(i);
				if (Math.abs(x - nodeX) <= mRadius * 2) { // 选中

					if (mNodeIndex == i) {
						return false;
					}

					mNodeIndex = i;
					invalidate();
					performClick();

					if (mOnNodeSelectedChangeListener != null) {
						mOnNodeSelectedChangeListener.onNodeSelected(i);
					}
					return true;
				}
			}
		}

		return super.onTouchEvent(event);
	}

	public void setNodeIndex(int index) {
		if (index >= mNodeNum) {
			throw new IllegalArgumentException("The index of node out of the range of node");
		}

		this.mNodeIndex = index;
		invalidate();
	}

	public void setNodeNum(int num) {
		this.mNodeNum = num;
		requestLayout();
	}

	@Override
	public boolean performClick() {
		super.performClick();
		return true;
	}

	private OnNodeSelectedChangeListener mOnNodeSelectedChangeListener;

	public void setOnNodeSelectedChangeListener(OnNodeSelectedChangeListener onNodeSelectedChangeListener) {
		this.mOnNodeSelectedChangeListener = onNodeSelectedChangeListener;
	}

	public interface OnNodeSelectedChangeListener {
		void onNodeSelected(int index);
	}
}
