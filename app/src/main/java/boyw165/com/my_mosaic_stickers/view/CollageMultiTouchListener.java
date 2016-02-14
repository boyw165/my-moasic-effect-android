package boyw165.com.my_mosaic_stickers.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import boyw165.com.my_mosaic_stickers.tool.LogUtils;

/**
 * This is the common multi-touch handler for collage view.
 */
public class CollageMultiTouchListener implements View.OnTouchListener,
                                                  GestureDetector.OnGestureListener,
                                                  ScaleGestureDetector.OnScaleGestureListener {

    private static final String TAG = "MultiTouch";

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    // For dragging ///////////////////////////////////////////////////////////
    private int mActivePointerId0 = -1;
    private PointF mPrevPoint = new PointF();

    // For scaling and rotation ///////////////////////////////////////////////
    private int mActivePointerId1 = -1;
    private PointF mPrevPivot = new PointF();
    /**
     * Previous span vector which is used to calculate the rotation angle.
     */
    private PointF mPrevSpanVec = new PointF();
    /**
     * Current span vector which is used to calculate the rotation angle.
     */
    private PointF mCurrSpanVec = new PointF();

    /**
     * Transform information used by all gesture detectors.
     */
    private TransformInfo mTransformInfo = new TransformInfo();

    public CollageMultiTouchListener(Context context) {
        mGestureDetector = new GestureDetector(context, this);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        mTransformInfo.reset();

        // Save everything for gesture detection.
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final float x = MotionEventCompat.getX(event, pointerIndex);
                final float y = MotionEventCompat.getY(event, pointerIndex);

                // Remember where we started (for dragging).
                mPrevPoint.set(x, y);
                // Save the ID of this pointer (for dragging).
                mActivePointerId0 = MotionEventCompat.getPointerId(event, 0);

                LogUtils.log(TAG, String.format("ACTION_DOWN"));
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                int count = MotionEventCompat.getPointerCount(event);

                if (count >= 2) {
                    int index0 = MotionEventCompat.findPointerIndex(event, mActivePointerId0);
                    // Always save latest pointer as active pointer #1.
                    int index1 = MotionEventCompat.getActionIndex(event);
                    int id = MotionEventCompat.getPointerId(event, index1);
                    mActivePointerId1 = id;

                    float x0 = MotionEventCompat.getX(event, index0);
                    float y0 = MotionEventCompat.getY(event, index0);
                    float x1 = MotionEventCompat.getX(event, index1);
                    float y1 = MotionEventCompat.getY(event, index1);

                    mPrevSpanVec.set(x1 - x0, y1 - y0);
                    mCurrSpanVec.set(mPrevSpanVec.x, mPrevSpanVec.y);

//                LogUtils.log(TAG, String.format("ACTION_POINTER_DOWN prevSpanVec=%s",
//                                                mPrevSpanVec));
//                LogUtils.log(TAG, String.format("                    currSpanVec=%s",
//                                                mCurrSpanVec));
//                LogUtils.log(TAG, String.format("                    p0=(%f, %f)",
//                                                x0, y0));
//                LogUtils.log(TAG, String.format("                    p1=(%f, %f)",
//                                                x1, y1));
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int index = MotionEventCompat.getActionIndex(event);
                final int id = MotionEventCompat.getPointerId(event, index);
                final int count = MotionEventCompat.getPointerCount(event);

                if (id == mActivePointerId0) {
                    // This was our active pointer going up. Choose a new
                    // active pointer (always the 2nd one) and adjust accordingly.
                    final int newIndex = (index == 0) ? 1 : 0;
                    final float x = MotionEventCompat.getX(event, newIndex);
                    final float y = MotionEventCompat.getY(event, newIndex);

                    mPrevPoint.set(x, y);
                    mActivePointerId0 = MotionEventCompat.getPointerId(event, newIndex);
                }
                if (id == mActivePointerId1) {
                    if (count <= 2) {
                        mActivePointerId1 = -1;
                    } else {
                        int newIndex = index + 1;
                        mActivePointerId1 = MotionEventCompat.findPointerIndex(
                            event, newIndex);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                LogUtils.log(TAG, String.format("ACTION_CANCEL/ACTION_UP"));
                mActivePointerId0 = -1;
                mActivePointerId1 = -1;
                mPrevSpanVec.set(0, 0);
                mCurrSpanVec.set(0, 0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int index0 = MotionEventCompat.findPointerIndex(event, mActivePointerId0);
                final int index1 = MotionEventCompat.findPointerIndex(event, mActivePointerId1);

                // Calculate current span vector if there're two touch points.
                if (index0 != -1 && index1 != -1) {
                    final float x0 = MotionEventCompat.getX(event, index0);
                    final float y0 = MotionEventCompat.getY(event, index0);
                    final float x1 = MotionEventCompat.getX(event, index1);
                    final float y1 = MotionEventCompat.getY(event, index1);

//                    mPrevSpanVec.set(mCurrSpanVec.x, mCurrSpanVec.y);
                    mCurrSpanVec.set(x1 - x0, y1 - y0);

//                    LogUtils.log(TAG, String.format("   ACTION_PINCH: prevSpanVec=%s",
//                                                    mPrevSpanVec));
//                    LogUtils.log(TAG, String.format("                 currSpanVec=%s",
//                                                    mCurrSpanVec));
//                    LogUtils.log(TAG, String.format("                 p0=(%f, %f)",
//                                                    x0, y0));
//                    LogUtils.log(TAG, String.format("                 p1=(%f, %f)",
//                                                    x1, y1));
                }
                break;
            }
        }

        // Do dragging detection:
        // Unlike {@code getAction}, {@code getActionMasked} it returns the
        // masked action being perform, without including the pointer index
        // bits.
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                // Use active pointer #0 to calculate translation.
                final int index1 = MotionEventCompat.findPointerIndex(event, mActivePointerId0);
                final float x = MotionEventCompat.getX(event, index1);
                final float y = MotionEventCompat.getY(event, index1);
                // Find the index of the active pointer and fetch its position.
                // Calculate the distance moved.
                final float dx = x - mPrevPoint.x;
                final float dy = y - mPrevPoint.y;

                mTransformInfo.deltaX = dx;
                mTransformInfo.deltaY = dy;
                LogUtils.log(TAG, String.format("   ACTION_MOVE (x=%f, y=%f)", x, y));
                break;
            }
        }

        // Do scale (or rotation) detection, which probably will override
        // previous gesture detection.
        mScaleGestureDetector.onTouchEvent(event);

        // Apply transform.
        setTransform(view, mTransformInfo);

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        LogUtils.log(TAG, String.format("onScaleBegin"));
        mPrevPivot.set(detector.getFocusX(), detector.getFocusY());
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor() - 1;
        float angle = getVectorsAngle(mPrevSpanVec, mCurrSpanVec);
        float dx = detector.getFocusX() - mPrevPivot.x;
        float dy = detector.getFocusY() - mPrevPivot.y;

        // Apply transformation.
//        mView.setScaleX(newScale);
//        mView.setScaleY(newScale);
//            CollageMultiTouchListener.setRotation(mView, rotation);
//            computeRenderOffset(mView, mPrevPivotX, mPrevPivotY);
//            CollageMultiTouchListener.setTranslation(mView, dx, dy);

        LogUtils.log(TAG, String.format("   onScale: angle=%f", angle));
        LogUtils.log(TAG, String.format("          : prevSpanVec=%s", mPrevSpanVec));
        LogUtils.log(TAG, String.format("          : currSpanVec=%s", mCurrSpanVec));
        LogUtils.log(TAG, String.format("          : scaleFactor=%s", detector.getScaleFactor()));

        mTransformInfo.deltaX = dx;
        mTransformInfo.deltaY = dy;
        mTransformInfo.deltaScaleX = scaleFactor;
        mTransformInfo.deltaScaleY = scaleFactor;
        mTransformInfo.deltaRotation = angle;

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        LogUtils.log(TAG, String.format("onScaleEnd"));
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Statics ////////////////////////////////////////////////////////////////

    public static void setTransform(View view, TransformInfo info) {
        if (info.deltaX != 0.0f || info.deltaY != 0.0f) {
            float[] deltaVec = {info.deltaX,
                                info.deltaY};

            view.getMatrix().mapVectors(deltaVec);
            view.setTranslationX(view.getTranslationX() + deltaVec[0]);
            view.setTranslationY(view.getTranslationY() + deltaVec[1]);
            view.invalidate();
        }
        if (info.deltaRotation != 0.0f) {
            view.setRotation(view.getRotation() + info.deltaRotation);
        }
        if (info.deltaScaleX != 0.0f || info.deltaScaleY != 0.0f) {
            float scaleX = view.getScaleX() + info.deltaScaleX;
            float scaleY = view.getScaleY() + info.deltaScaleY;

            view.setScaleX(scaleX);
            view.setScaleY(scaleY);
        }
    }

    public static float getVectorsAngle(PointF vecA, PointF vecB) {
        if (vecA.x == vecB.x && vecA.y == vecB.y) {
            return 0;
        } else {
            return (float) (Math.toDegrees(
                (Math.atan2(vecB.y, vecB.x) - Math.atan2(vecA.y, vecA.x))));
//        float lenA = getVectorLength(vecA);
//        float lenB = getVectorLength(vecB);
//        return (float) (Math.toDegrees(
//            (Math.acos((vecA.x * vecB.x + vecA.y * vecB.y) /
//                       (lenA * lenB)))));
        }
    }

//    public static float getVectorLength(PointF vec) {
//        return (float) Math.hypot(vec.x, vec.y);
//    }

//    public static void normalizeVector(PointF vec) {
//        float length = getVectorLength(vec);
//        vec.x /= length;
//        vec.y /= length;
//    }

    ///////////////////////////////////////////////////////////////////////////
    // Private ////////////////////////////////////////////////////////////////

    private void computeRenderOffset(View view, float pivotX, float pivotY) {
        if (view.getPivotX() == pivotX && view.getPivotY() == pivotY) {
            return;
        }

        float[] prevPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(prevPoint);

        view.setPivotX(pivotX);
        view.setPivotY(pivotY);

        float[] currPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(currPoint);

        float offsetX = currPoint[0] - prevPoint[0];
        float offsetY = currPoint[1] - prevPoint[1];

        view.setTranslationX(view.getTranslationX() - offsetX);
        view.setTranslationY(view.getTranslationY() - offsetY);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * Used to apply transform to view.
     */
    public static class TransformInfo {
        public float deltaX;
        public float deltaY;
        public float deltaScaleX;
        public float deltaScaleY;
        public float deltaRotation;

        public void reset() {
            deltaX = 0;
            deltaY = 0;
            deltaScaleX = 0;
            deltaScaleY = 0;
            deltaRotation = 0;
        }
    }
}
