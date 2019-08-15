package com.shanbay.stageprogress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Otway on 2019/8/12.
 */
public class StepProgressBar extends View {

	private static final String TAG = "StepProgressBar";
	/**
	 * 节点个数
	 */
	private int mStepNum;

	/**
	 * 选中节点位置
	 */
	private int mIndex;

	/**
	 * 节点圆环颜色
	 */
	private int mSelectedRingColor;
	private int mUnSelectedRingColor;

	/**
	 * 节点颜色
	 */
	private int mUnSelectedBeadColor;
	private int mSelectedBeadColor;

	/**
	 * 节点半径
	 */
	private int mSelectedBeadRadius;
	private int mUnSelectedBeadRadius;

	/**
	 * 节点圆环半径
	 */
	private int mSelectedRingRadius;
	private int mUnSelectedRingRadius;

	/**
	 * 是否设置选中颜色（单色）
	 */
	private boolean mHasSelectedEndColor;

	/**
	 * 进度条颜色
	 */
	private int mUnSelectedLineColor;
	private int mSelectedLineStartColor;
	private int mSelectedLineEndColor;

	/**
	 * 进度条高度
	 */
	private int mSelectedLineHeight;
	private int mUnSelectedLineHeight;

	private final int mGap = 2;
	private Paint mPaint;
	private List<Integer> mNodeList;
	private LinearGradient mLinearGradient;

	public StepProgressBar(Context context) {
		this(context, null);
	}

	public StepProgressBar(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StepProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StepProgressBar);
		mStepNum = array.getInteger(R.styleable.StepProgressBar_step_progress_step_num, 3);
		mIndex = array.getInteger(R.styleable.StepProgressBar_step_progress_index, 0);
		mUnSelectedLineColor = array.getColor(R.styleable.StepProgressBar_step_progress_unselected_line_color, Color.parseColor("#e8f5c9"));
		mSelectedLineStartColor = array.getColor(R.styleable.StepProgressBar_step_progress_selected_line_start_color, Color.parseColor("#9bd168"));
		mSelectedLineEndColor = array.getColor(R.styleable.StepProgressBar_step_progress_selected_line_end_color, Color.parseColor("#9bd168"));
		mHasSelectedEndColor = array.hasValue(R.styleable.StepProgressBar_step_progress_selected_line_end_color);

		mUnSelectedRingColor = array.getColor(R.styleable.StepProgressBar_step_progress_unselected_ring_color, Color.WHITE);
		mSelectedRingColor = array.getColor(R.styleable.StepProgressBar_step_progress_selected_ring_color, Color.WHITE);

		mUnSelectedBeadColor = array.getColor(R.styleable.StepProgressBar_step_progress_unselected_bead_color, Color.parseColor("#e8f5c9"));
		mSelectedBeadColor = array.getColor(R.styleable.StepProgressBar_step_progress_selected_bead_color, Color.parseColor("#9bd168"));

		mSelectedLineHeight = array.getDimensionPixelSize(R.styleable.StepProgressBar_step_progress_selected_line_height, 12);
		mUnSelectedLineHeight = array.getDimensionPixelSize(R.styleable.StepProgressBar_step_progress_unselected_line_height, 8);

		mSelectedBeadRadius = array.getDimensionPixelSize(R.styleable.StepProgressBar_step_progress_selected_bead_radius, 18);
		mUnSelectedBeadRadius = array.getDimensionPixelSize(R.styleable.StepProgressBar_step_progress_unselected_bead_radius, 16);

		mSelectedRingRadius = array.getDimensionPixelSize(R.styleable.StepProgressBar_step_progress_selected_ring_radius, 30);
		mUnSelectedRingRadius = array.getDimensionPixelSize(R.styleable.StepProgressBar_step_progress_unselected_ring_radius, 24);
		array.recycle();

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(mUnSelectedLineColor);
		mPaint.setStyle(Paint.Style.FILL);

		mNodeList = new ArrayList<>();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int maxRing = Math.max(mSelectedRingRadius, mUnSelectedRingRadius);

		int minWidth = mStepNum * 2 * maxRing + (mStepNum - 1) * 50 + mGap * 2;
		int minHeight = maxRing * 2 + mGap * 2;

		int width;
		int height;

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		width = Math.max(widthSize, minWidth);
		height = Math.max(heightSize, minHeight);

		if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT &&
				getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
			setMeasuredDimension(minWidth, minHeight);
		} else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
			setMeasuredDimension(minWidth, height);
		} else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
			setMeasuredDimension(width, minHeight);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		initNodeList();
	}

	@Override
	public void invalidate() {
		invalidateGradient();
		super.invalidate();
	}

	private void invalidateGradient() {
		if (mNodeList == null || mNodeList.isEmpty()) {
			return;
		}
		int maxRingRadius = Math.max(mSelectedRingRadius, mUnSelectedRingRadius);
		int startX = mNodeList.get(0);
		float centerY = (getHeight() >> 1);
		int indexX = mNodeList.get(mIndex);
		mLinearGradient = new LinearGradient(startX + maxRingRadius, centerY, indexX, centerY, mSelectedLineStartColor, mSelectedLineEndColor, Shader.TileMode.CLAMP);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		invalidateGradient();
	}

	private void initNodeList() {
		mNodeList.clear();

		if (mStepNum < 2) {
			throw new IllegalArgumentException("The node number must greater than one");
		}

		if (mIndex >= mStepNum) {
			throw new IllegalArgumentException("The index of node out of the range of node");
		}
		int maxRingRadius = Math.max(mSelectedRingRadius, mUnSelectedRingRadius);

		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();

		int startX = paddingLeft + mGap + maxRingRadius;
		int endX = getMeasuredWidth() - paddingRight - mGap - maxRingRadius;

		int space = (endX - startX) / (mStepNum - 1);

		for (int i = 0; i < mStepNum; i++) {
			int x = startX + i * space;
			if (i == mStepNum - 1) {
				x = endX;
			}
			mNodeList.add(x);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mNodeList == null || mNodeList.isEmpty()) {
			return;
		}

		int startX = mNodeList.get(0);
		int size = mNodeList.size();
		int endX = mNodeList.get(size - 1);
		float centerY = (getHeight() >> 1);

		// 未选中 线
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mUnSelectedLineHeight);
		mPaint.setColor(mUnSelectedLineColor);
		mPaint.setStyle(Paint.Style.STROKE);
		canvas.drawLine(startX, centerY, endX, centerY, mPaint);

		// 选中线
		int indexX = mNodeList.get(mIndex);
		if (mIndex > 0) {
			mPaint.setStrokeWidth(mSelectedLineHeight);
			if (mHasSelectedEndColor) {
				mPaint.setShader(mLinearGradient);
				canvas.drawLine(startX - mUnSelectedBeadRadius, centerY, indexX, centerY, mPaint);
				mPaint.setShader(null);
			} else {
				mPaint.setColor(mSelectedLineStartColor);
				canvas.drawLine(startX - mUnSelectedBeadRadius, centerY, indexX, centerY, mPaint);
			}
		}

		// 未选中节点
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setStrokeWidth(1.0f);
		for (int i = size - 1; i >= 0; i--) {
			int x = mNodeList.get(i);
			if (i >= mIndex) {
				mPaint.setColor(mUnSelectedRingColor);
				canvas.drawCircle(x, centerY, mUnSelectedRingRadius, mPaint);

				mPaint.setColor(mUnSelectedBeadColor);
				canvas.drawCircle(x, centerY, mUnSelectedBeadRadius, mPaint);
			}

			if (i == mIndex && mIndex != 0) {
				mPaint.setColor(mSelectedRingColor);
				canvas.drawCircle(x, centerY, mSelectedRingRadius, mPaint);

				mPaint.setColor(mSelectedBeadColor);
				canvas.drawCircle(x, centerY, mSelectedBeadRadius, mPaint);
			}
		}
	}

	public void setNodeIndex(int index) {
		if (index >= mStepNum) {
			throw new IllegalArgumentException("The index of node out of the range of node");
		}

		this.mIndex = index;
		Log.d(TAG, "setNodeIndex: mNodeIndex -> " + index);
	}

	public void setNodeNum(int num) {
		this.mStepNum = num;
		initNodeList();
	}
}
