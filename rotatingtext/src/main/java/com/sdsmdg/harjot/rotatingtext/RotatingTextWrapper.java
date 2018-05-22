package com.sdsmdg.harjot.rotatingtext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sdsmdg.harjot.rotatingtext.models.Rotatable;
import com.sdsmdg.harjot.rotatingtext.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Harjot on 01-May-17.
 */

public class RotatingTextWrapper extends RelativeLayout {

    String text;
    ArrayList<Rotatable> rotatableList;
    ArrayList<TextView> textViews;

    boolean isContentSet = false;

    Context context;

    RelativeLayout.LayoutParams lp;

    int prevId;

    Typeface typeface;
    int size = 24;

    private double changedSize = 0;
    private boolean adaptable = false;

    List<RotatingTextSwitcher> switcherList;

    public RotatingTextWrapper(Context context) {
        super(context);
        this.context = context;
    }

    public RotatingTextWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public RotatingTextWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setContent(String text, Rotatable... rotatables) {
        this.text = text;
        rotatableList = new ArrayList<>();
        switcherList = new ArrayList<>();
        textViews = new ArrayList<>();
        Collections.addAll(rotatableList, rotatables);
        isContentSet = true;
    }

    public void setContent(String text, ArrayList<Rotatable> rotatables) {
        this.text = text;
        rotatableList = new ArrayList<>(rotatables);
        switcherList = new ArrayList<>();
        isContentSet = true;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (isContentSet) {
            String[] array = text.split("\\?");

            if (array.length == 0) {
                final RotatingTextSwitcher textSwitcher = new RotatingTextSwitcher(context);
                switcherList.add(textSwitcher);

                textSwitcher.setRotatable(rotatableList.get(0));

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    textSwitcher.setId(Utils.generateViewId());
                } else {
                    textSwitcher.setId(View.generateViewId());
                }

                prevId = textSwitcher.getId();

                lp = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                lp.addRule(CENTER_VERTICAL);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                addViewInLayout(textSwitcher, -1, lp);

            }

            for (int i = 0; i < array.length; i++) {
                final TextView textView = new TextView(context);


                textView.setText(array[i]);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    textView.setId(Utils.generateViewId());
                } else {
                    textView.setId(View.generateViewId());
                }
                textView.setTextSize(size);
                textViews.add(textView);

                if (typeface != null)
                    textView.setTypeface(typeface);

                lp = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(CENTER_VERTICAL);
                if (i == 0)
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                else
                    lp.addRule(RIGHT_OF, prevId);

                addViewInLayout(textView, -1, lp);

                if (i < rotatableList.size()) {
                    final RotatingTextSwitcher textSwitcher = new RotatingTextSwitcher(context);
                    switcherList.add(textSwitcher);
                    textSwitcher.setRotatable(rotatableList.get(i));

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        textSwitcher.setId(Utils.generateViewId());
                    } else {
                        textSwitcher.setId(View.generateViewId());
                    }
                    prevId = textSwitcher.getId();

                    lp = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lp.addRule(CENTER_VERTICAL);
                    lp.addRule(RIGHT_OF, textView.getId());

                    addViewInLayout(textSwitcher, -1, lp);
                }
            }
            isContentSet = false;
        }
        requestLayout();
    }

    public void addWord(int rotatableIndex, int wordIndex, String newWord) {
        if (!TextUtils.isEmpty(newWord) && (!newWord.contains("\n"))) {

            RotatingTextSwitcher switcher = switcherList.get(rotatableIndex);
            Rotatable toChange = rotatableList.get(rotatableIndex);

            Paint paint = new Paint();
            paint.setTextSize(toChange.getSize() * getContext().getResources().getDisplayMetrics().density);
            paint.setTypeface(toChange.getTypeface());
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            Rect result = new Rect();

            paint.getTextBounds(toChange.getLargestWord(), 0, toChange.getLargestWord().length(), result);

            double originalSize = result.width();

            String toDeleteWord = toChange.getTextAt(wordIndex);

            paint = new Paint();
            paint.setTextSize(toChange.getSize() * getContext().getResources().getDisplayMetrics().density);
            paint.setTypeface(toChange.getTypeface());
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            result = new Rect();
            paint.getTextBounds(toChange.peekLargestWord(wordIndex, newWord), 0, toChange.peekLargestWord(wordIndex, newWord).length(), result);
            double finalSize = result.width();

            Log.i("point toDeleteWord", toDeleteWord);
            Log.i("point PreviousWord", toChange.getPreviousWord());
            Log.i("point CurrentWord", toChange.getCurrentWord());

            if (finalSize < originalSize) {

                //we are replacing the largest word with a smaller new word
                if (toChange.getPreviousWord().equals(toDeleteWord)) {
                    waitForAnimationComplete(toChange.getAnimationDuration(), toChange.getLargestWord(), false, toChange, switcher, wordIndex, newWord);
                } else if (toChange.getCurrentWord().equals(toDeleteWord)) {
                    waitForAnimationComplete(toChange.getAnimationDuration() + toChange.getUpdateDuration(), toChange.getLargestWord(), true, toChange, switcher, wordIndex, newWord);

                } else {
                    toChange.setTextAt(wordIndex, newWord);
                    switcher.setText(toChange.getLargestWordWithSpace()); //provides space

                    if (adaptable && getSize() != (int) changedSize && changedSize != 0) {

                        if ((double) availablePixels() / (double) findRequiredPixel() < getSize() / changedSize)
                            reduceSize((double) findRequiredPixel() / (double) availablePixels());
                        else reduceSize(changedSize / getSize());
                    }
                }
            } else {
                toChange.setTextAt(wordIndex, newWord);

                switcher.setText(toChange.getLargestWordWithSpace());//provides space
                if (adaptable && finalSize != originalSize) {
                    int actualPixel = findRequiredPixel();

                    if (adaptable && actualPixel > availablePixels()) {
                        reduceSize((double) actualPixel / (double) availablePixels());
                    }
                }
            }
        }
    }

    private void waitForAnimationComplete(int totalTime, final String oldLargestWord, boolean positionEntering, final Rotatable toChange, final RotatingTextSwitcher switcher, final int index, final String newWord) {
        //positionEntering is true if word is animating in and false if animating out
        if (positionEntering) {
            new CountDownTimer(totalTime + 23, 22) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if (!switcher.animationRunning && !toChange.getCurrentWord().equals(oldLargestWord)) {
                        toChange.setTextAt(index, newWord);
                        //provides space
                        switcher.setText(toChange.getLargestWordWithSpace());
                        if (adaptable && getSize() != (int) changedSize && changedSize != 0) {
                            if ((double) availablePixels() / (double) findRequiredPixel() < getSize() / changedSize)
                                reduceSize((double) findRequiredPixel() / (double) availablePixels());
                            else reduceSize(changedSize / getSize());
                        }

                    }
                }

                @Override
                public void onFinish() {
                }
            }.start();
        } else {
            new CountDownTimer(totalTime + 23, 22) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if (!switcher.animationRunning) {
                        toChange.setTextAt(index, newWord);
                        //provides space
                        switcher.setText(toChange.getLargestWordWithSpace());
                        if (adaptable && getSize() != (int) changedSize && changedSize != 0) {
                            if ((double) availablePixels() / (double) findRequiredPixel() < getSize() / changedSize)
                                reduceSize((double) findRequiredPixel() / (double) availablePixels());
                            else reduceSize(changedSize / getSize());
                        }
                    }
                }

                @Override
                public void onFinish() {
                }
            }.start();
        }
    }

    private int availablePixels() {
        //returns total pixel available with parent
        View asd = (View) getParent();
        return asd.getMeasuredWidth() - asd.getPaddingLeft() - asd.getPaddingRight();
    }

    private int findRequiredPixel() {
        //returns observed wrapper size on screen including padding and margin in pixels
        int actualPixel = 0;
        MarginLayoutParams margins;

        for (RotatingTextSwitcher switcher : switcherList) {
            switcher.measure(0, 0);
            actualPixel += switcher.getMeasuredWidth();
        }

        for (TextView id : textViews) {
            id.measure(0, 0);
            actualPixel += id.getMeasuredWidth();
        }

        margins = MarginLayoutParams.class.cast(getLayoutParams());
        actualPixel += margins.leftMargin;
        actualPixel += margins.rightMargin;
        actualPixel += getPaddingLeft();
        actualPixel += getPaddingRight();
        return actualPixel;
    }

    public void reduceSize(double factor) {
        double initialSizeWrapper = (changedSize == 0) ? getSize() : changedSize;

        double newWrapperSize = (double) initialSizeWrapper / factor;

        for (RotatingTextSwitcher switcher : switcherList) {
            double initialSizeRotatable = switcher.getTextSize();

            double newRotatableSize = initialSizeRotatable / factor;
            switcher.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) newRotatableSize);

        }
        for (TextView id : textViews) {
            id.setTextSize((float) newWrapperSize);

        }
        MarginLayoutParams margins = MarginLayoutParams.class.cast(getLayoutParams());

        margins.leftMargin = (int) (margins.leftMargin / factor);
        margins.rightMargin = (int) (margins.rightMargin / factor);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        if (paddingLeft != 0) {
            if (paddingRight == 0)
                setPadding((int) (paddingLeft / factor), 0, 0, 0);
            else
                setPadding((int) ((paddingLeft / factor)), 0, (int) ((paddingRight / factor)), 0);

        } else if (paddingRight != 0) {
            setPadding(0, 0, (int) (paddingRight / factor), 0);
        }

        changedSize=(changedSize==0)?getSize() / factor:changedSize / factor;

        if (adaptable && findRequiredPixel() > availablePixels()) {
            reduceSize((double) findRequiredPixel() / (double) availablePixels());
        }
    }

    public void setAdaptable(boolean adaptable) {
        this.adaptable = adaptable;
    }

    public void shiftRotatable(int index) {

    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void pause(int position) {
        switcherList.get(position).pause();
    }

    public void resume(int position) {
        switcherList.get(position).resume();
    }

}
