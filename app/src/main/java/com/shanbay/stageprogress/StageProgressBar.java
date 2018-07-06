package com.shanbay.stageprogress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Otway on 2018/6/21.
 */
public class StageProgressBar extends View {

	// 普通节点半径
	private int mNormalNodeRadius;
	// 节点连线宽度
	private int mLineWidth;

	// 选中节点半径
	private int mSelectedNodeRadius;

	// 节点个数
	private int mNodeNum;

	// 选中节点位置
	private int mNodeIndex;

	private Paint mNormalPaint;
	private Paint mSelectedPaint;
	private Path mNormalPath;
	private Path mSelectedPath;

	private List<Integer> mNodeXList;

	public StageProgressBar(Context context) {
		this(context, null);
	}

	public StageProgressBar(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StageProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StageProgressBar);
		int normalColor = ta.getColor(R.styleable.StageProgressBar_normal_color, Color.GRAY);
		int selectedColor = ta.getColor(R.styleable.StageProgressBar_selected_color, Color.GREEN);
		mNormalNodeRadius = ta.getDimensionPixelSize(R.styleable.StageProgressBar_normal_node_radius, 5);
		mLineWidth = mNormalNodeRadius * 2 / 3;

		mSelectedNodeRadius = ta.getDimensionPixelSize(R.styleable.StageProgressBar_selected_node_radius, 10);
		mNodeNum = ta.getInteger(R.styleable.StageProgressBar_node_num, 2);
		mNodeIndex = ta.getInteger(R.styleable.StageProgressBar_current_node_index, 0);
		ta.recycle();

		mNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNormalPaint.setColor(normalColor);
		mNormalPaint.setStyle(Paint.Style.FILL);

		mSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mSelectedPaint.setColor(selectedColor);
		mSelectedPaint.setStyle(Paint.Style.FILL);

		mNormalPath = new Path();
		mSelectedPath = new Path();
		mNodeXList = new ArrayList<>();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int minGap = mSelectedNodeRadius * 2 * 4;

		int minWidth = 2 * mSelectedNodeRadius + (mNodeNum - 1) * minGap + 4;// 两边各留2像素
		int minHeight = mSelectedNodeRadius * 2 + 2; // 上下各留1像素

		int width;
		int height;

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		width = Math.max(widthSize, minWidth);
		height = Math.max(heightSize, minHeight);

		setMeasuredDimension(width, height);
	}

	private void initPath() {
		mNodeXList.clear();
		mNormalPath.reset();
		mSelectedPath.reset();

		if (mNodeNum < 2) {
			throw new IllegalArgumentException("The node number must greater than one");
		}

		if (mNodeIndex >= mNodeNum) {
			throw new IllegalArgumentException("The index of node out of the range of node");
		}

		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();

		int startX = paddingLeft + mSelectedNodeRadius + 2;
		int endX = getWidth() - paddingRight - 2 - mSelectedNodeRadius;

		int gap = (endX - startX) / (mNodeNum - 1);

		// 选中线
		if (mNodeIndex > 0) {
			if (mNodeIndex == mNodeNum - 1) {
				mSelectedPath.addRect(startX, (getHeight() - mLineWidth) / 2, endX, (getHeight() + mLineWidth) / 2, Path.Direction.CCW);
			} else {
				mSelectedPath.addRect(startX, (getHeight() - mLineWidth) / 2, startX + gap * mNodeIndex, (getHeight() + mLineWidth) / 2, Path.Direction.CCW);
			}
		}

		// 选中节点
		for (int i = 0; i <= mNodeIndex; i++) {
			if (i == mNodeNum - 1) {
				mSelectedPath.addCircle(endX, getHeight() / 2, mSelectedNodeRadius, Path.Direction.CCW);
				mNodeXList.add(endX);
				break;
			}
			mSelectedPath.addCircle(startX + gap * i, getHeight() / 2, mSelectedNodeRadius, Path.Direction.CCW);
			mNodeXList.add(startX + gap * i);
		}
		mSelectedPath.close();

		// 背景线
		if (mNodeIndex != mNodeNum - 1) {
			mNormalPath.addRect(startX + gap * mNodeIndex, (getHeight() - mLineWidth) / 2, endX, (getHeight() + mLineWidth) / 2, Path.Direction.CCW);
		}

		// 背景节点
		for (int i = 0; i < mNodeNum; i++) {
			if (i <= mNodeIndex) {
				continue;
			}

			if (i == mNodeNum - 1) {
				mNormalPath.addCircle(endX, getHeight() / 2, mNormalNodeRadius, Path.Direction.CCW);
				mNodeXList.add(endX);
				break;
			}
			mNormalPath.addCircle(startX + (gap * i), getHeight() / 2, mNormalNodeRadius, Path.Direction.CCW);
			mNodeXList.add(startX + (gap * i));
		}
		mNormalPath.close();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		initPath();
		canvas.drawPath(mNormalPath, mNormalPaint);// 先画背景
		canvas.drawPath(mSelectedPath, mSelectedPaint); // 选中效果
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && !mNodeXList.isEmpty()) {
			float x = event.getX();
			for (int i = 0, size = mNodeXList.size(); i < size; i++) {
				int nodeX = mNodeXList.get(i);
				if (Math.abs(x - nodeX) <= mSelectedNodeRadius * 4) { // 选中

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


	@Override
	public boolean performClick() {
		super.performClick();
		return true;
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
		invalidate();
	}

	private OnNodeSelectedChangeListener mOnNodeSelectedChangeListener;

	public void setOnNodeSelectedChangeListener(OnNodeSelectedChangeListener onNodeSelectedChangeListener) {
		this.mOnNodeSelectedChangeListener = onNodeSelectedChangeListener;
	}

	public interface OnNodeSelectedChangeListener {
		void onNodeSelected(int index);
	}
}
