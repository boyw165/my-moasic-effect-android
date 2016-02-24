package boyw165.com.my_mosaic_stickers.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import boyw165.com.my_mosaic_stickers.tool.LogUtils;

/**
 * This is the common multi-touch handler for collage view.
 */
public class CollageMultiTouchListener implements View.OnTouchListener {

    private static final String TAG = "MultiTouch";

    private int mActivePointerId0 = -1;
    private int mActivePointerId1 = -1;
    private boolean mIsDragGestureInProgress = false;
    private boolean mIsScaleGestureInProgress = false;

    /**
     * Transform information used by all gesture detectors.
     */
    private TransformInfo mTransformInfo = new TransformInfo();

    public CollageMultiTouchListener(Context context) {
        // DO NOTHING.
    }

    public CollageMultiTouchListener(Context context, TransformInfo info) {
        mTransformInfo = info;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        TransformInfo info = mTransformInfo;

        // Start fresh.
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                LogUtils.log(TAG, "ACTION_DOWN/ACTION_CANCEL/ACTION_UP: reset()");
                mTransformInfo.reset();
                mIsScaleGestureInProgress = false;
                break;
            }
        }

        // Gesture detection.
        if (!mIsScaleGestureInProgress) {
            // If it's in the progress of translation.
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final float x = MotionEventCompat.getX(event, pointerIndex);
                    final float y = MotionEventCompat.getY(event, pointerIndex);

                    // Save the ID of this pointer.
                    mActivePointerId0 = MotionEventCompat.getPointerId(event, 0);
                    // Remember where we started.
                    info.prevPos.set(x, y);

                    LogUtils.log(TAG, String.format("ACTION_DOWN"));
                    break;
                }
                case MotionEvent.ACTION_POINTER_DOWN: {
                    int index0 = MotionEventCompat.findPointerIndex(event, mActivePointerId0);
                    // Always save latest pointer as active pointer #1.
                    int index1 = MotionEventCompat.getActionIndex(event);
                    int id = MotionEventCompat.getPointerId(event, index1);

                    float x0 = MotionEventCompat.getX(event, index0);
                    float y0 = MotionEventCompat.getY(event, index0);
                    float x1 = MotionEventCompat.getX(event, index1);
                    float y1 = MotionEventCompat.getY(event, index1);

                    // Clean translation.
                    info.deltaPos.set(0, 0);
                    // Update span vector.
                    info.prevSpanVec.set(x1 - x0, y1 - y0);
                    info.prevSpan = getVectorLength(info.prevSpanVec);
                    info.currSpanVec.set(info.prevSpanVec);
                    info.currSpan = getVectorLength(info.currSpanVec);
                    // Update pivot.
                    info.prevPivot.set((x0 + x1) / 2,
                                       (y0 + y1) / 2);
                    info.currPivot.set(info.prevPivot);
                    mActivePointerId1 = id;
                    // Switch to scaling mode.
                    mIsScaleGestureInProgress = true;

                    LogUtils.log(TAG, String.format("BEGIN_SCALE"));
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    LogUtils.log(TAG, String.format("ACTION_CANCEL/ACTION_UP"));
                    mActivePointerId0 = -1;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    // Do translation.
                    if (TransformInfo.isValid(info.prevPos)) {
                        int index = MotionEventCompat.getActionIndex(event);
                        float x = MotionEventCompat.getX(event, index);
                        float y = MotionEventCompat.getY(event, index);
                        float dx = x - info.prevPos.x;
                        float dy = y - info.prevPos.y;

                        info.deltaPos.set(dx, dy);
                    }
                    break;
                }
            }
        } else {
            // When scaling...
            switch (action) {
                case MotionEvent.ACTION_MOVE: {
                    int index0 = MotionEventCompat.findPointerIndex(event, mActivePointerId0);
                    int index1 = MotionEventCompat.findPointerIndex(event, mActivePointerId1);
                    float x0 = MotionEventCompat.getX(event, index0);
                    float y0 = MotionEventCompat.getY(event, index0);
                    float x1 = MotionEventCompat.getX(event, index1);
                    float y1 = MotionEventCompat.getY(event, index1);

                    // Update pivot.
                    info.currPivot.set((x0 + x1) / 2,
                                       (y0 + y1) / 2);
                    // Update span and span vector.
                    info.currSpanVec.set(x1 - x0, y1 - y0);
                    info.currSpan = getVectorLength(info.currSpanVec);
                    // Update scale.
                    float deltaScale = (info.currSpan - info.prevSpan) / info.prevSpan;
                    info.deltaScale.set(deltaScale, deltaScale);
                    // Update rotation according to given span vectors.
                    info.deltaRotation = getVectorsAngle(info.prevSpanVec,
                                                         info.currSpanVec);
                    LogUtils.log(TAG, String.format("   MOVE: pt0=(%f, %f), pt1=(%f, %f)", x0, y0, x1, y1));
                    LogUtils.log(TAG, String.format("         currPivot=%s", info.currPivot));

                    break;
                }
                case MotionEvent.ACTION_POINTER_UP: {
                    int index = MotionEventCompat.getActionIndex(event);
                    if (index == mActivePointerId0 || index == mActivePointerId1) {
                        // TODO: Because it might back to translation mode, it
                        // has to update prevPos and more.
                        if (index == mActivePointerId0) {
                            mActivePointerId0 = -1;
                        } else if (index == mActivePointerId1) {
                            mActivePointerId1 = -1;
                        }
                        mIsScaleGestureInProgress = false;
                    }
                    break;
                }
//                case MotionEvent.ACTION_POINTER_UP: {
//                    final int index = MotionEventCompat.getActionIndex(event);
//                    final int id = MotionEventCompat.getPointerId(event, index);
//                    final int count = MotionEventCompat.getPointerCount(event);
//
//                    if (id == mActivePointerId0) {
//                        // This was our active pointer going up. Choose a new
//                        // active pointer (always the 2nd one) and adjust accordingly.
//                        final int newIndex = (index == 0) ? 1 : 0;
//                        final float x = MotionEventCompat.getX(event, newIndex);
//                        final float y = MotionEventCompat.getY(event, newIndex);
//
//                        mPrevPoint.set(x, y);
//                        mActivePointerId0 = MotionEventCompat.getPointerId(event, newIndex);
//                    }
//                    if (id == mActivePointerId1) {
//                        mActivePointerId1 = -1;
//                    }
//                    break;
//                }
            }
        }

        // Apply transform.
        setTransform(view, mTransformInfo);

        return true;
    }

    public void onScale(TransformInfo info) {
//        float scaleFactor = detector.getScaleFactor() - 1;
//        float angle = getVectorsAngle(mPrevSpanVec, mCurrSpanVec);
//        float dx = detector.getFocusX() - mPrevPivot.x;
//        float dy = detector.getFocusY() - mPrevPivot.y;

        // Apply transformation.
//        mView.setScaleX(newScale);
//        mView.setScaleY(newScale);
//            CollageMultiTouchListener.setRotation(mView, rotation);
//            computeRenderOffset(mView, mPrevPivotX, mPrevPivotY);
//            CollageMultiTouchListener.setTranslation(mView, dx, dy);

//        LogUtils.log(TAG, String.format("   onScale: angle=%f", angle));
//        LogUtils.log(TAG, String.format("          : prevSpanVec=%s", mPrevSpanVec));
//        LogUtils.log(TAG, String.format("          : currSpanVec=%s", mCurrSpanVec));
//        LogUtils.log(TAG, String.format("          : scaleFactor=%s", detector.getScaleFactor()));
//
//        mTransformInfo.deltaX = dx;
//        mTransformInfo.deltaY = dy;
//        mTransformInfo.deltaScaleX = scaleFactor;
//        mTransformInfo.deltaScaleY = scaleFactor;
//        mTransformInfo.deltaRotation = angle;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Statics ////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Private ////////////////////////////////////////////////////////////////

    private void setTransform(View view, TransformInfo info) {
        boolean isChanged = false;

        // For translation, pivot change is always prior to position change.
        if (TransformInfo.isValid(info.prevPivot) && TransformInfo.isValid(info.currPivot) &&
            !info.prevPivot.equals(info.currPivot)) {
            float[] deltaVec = {info.currPivot.x - info.prevPivot.x,
                                info.currPivot.y - info.prevPivot.y};

            // Update pivot translation.
            view.getMatrix().mapVectors(deltaVec);
            view.setTranslationX(view.getTranslationX() + deltaVec[0]);
            view.setTranslationY(view.getTranslationY() + deltaVec[1]);
            isChanged = true;

            LogUtils.log(TAG, String.format("   PIVOT_TRANSLATION: prevPivot = %s.", info.prevPivot));
            LogUtils.log(TAG, String.format("                      currPivot = %s.", info.currPivot));
        } else if (info.deltaPos.x != 0 || info.deltaPos.y != 0) {
            float[] deltaVec = {info.deltaPos.x,
                                info.deltaPos.y};

            view.getMatrix().mapVectors(deltaVec);
            view.setTranslationX(view.getTranslationX() + deltaVec[0]);
            view.setTranslationY(view.getTranslationY() + deltaVec[1]);
            isChanged = true;

            LogUtils.log(TAG, String.format("   TRANSLATING (%f, %f).", deltaVec[0], deltaVec[1]));
        }

        if (TransformInfo.isNonZero(info.deltaScale)) {
            float scaleX = view.getScaleX() + info.deltaScale.x;
            float scaleY = view.getScaleY() + info.deltaScale.y;

            view.setScaleX(scaleX);
            view.setScaleY(scaleY);
            isChanged = true;

            LogUtils.log(TAG, String.format("   SCALING (%f, %f).",
                                            info.deltaScale.x,
                                            info.deltaScale.y));
        }

        if (TransformInfo.isNonZero(info.deltaRotation)) {
            float rotation = view.getRotation() + info.deltaRotation;

            view.setRotation(rotation);
            isChanged = true;

            LogUtils.log(TAG, String.format("   ROTATING %f.", info.deltaRotation));
        }

        if (isChanged) {
            view.invalidate();
        }
    }

    private float getVectorsAngle(PointF vecA, PointF vecB) {
        if (vecA.x == vecB.x && vecA.y == vecB.y) {
            return 0;
        } else {
            return (float) (Math.toDegrees(
                (Math.atan2(vecB.y, vecB.x) - Math.atan2(vecA.y, vecA.x))));
        }
    }

    private float getVectorLength(PointF vec) {
        return (float) Math.hypot(vec.x, vec.y);
    }

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

        public static final float INVALID = Float.MAX_VALUE;

        public PointF prevPos = new PointF(INVALID, INVALID);
        public PointF deltaPos = new PointF(0f, 0f);

        public PointF deltaScale = new PointF(0f, 0f);

        public PointF prevPivot = new PointF(INVALID, INVALID);
        public PointF currPivot = new PointF(INVALID, INVALID);

        // Span vector which is used to calculate the rotation angle.
        public PointF prevSpanVec = new PointF(INVALID, INVALID);
        public PointF currSpanVec = new PointF(INVALID, INVALID);
        public float prevSpan = 0;
        public float currSpan = 0;

        public float deltaRotation;

        public void reset() {
            prevPos.set(INVALID, INVALID);
            deltaPos.set(0f, 0f);

            deltaScale.set(0f, 0f);

            prevPivot.set(INVALID, INVALID);
            currPivot.set(INVALID, INVALID);

            prevSpanVec.set(INVALID, INVALID);
            currSpanVec.set(INVALID, INVALID);

            deltaRotation = 0;
        }

        public static boolean isValid(PointF pt) {
            return (pt != null && pt.x != INVALID && pt.y != INVALID);
        }

        public static boolean isNonZero(PointF pt) {
            return (pt != null && pt.x != 0f && pt.y != 0f);
        }

        public static boolean isNonZero(float num) {
            return num != 0;
        }
    }
}
