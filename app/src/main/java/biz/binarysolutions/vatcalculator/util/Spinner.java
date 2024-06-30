package biz.binarysolutions.vatcalculator.util;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;

import biz.binarysolutions.vatcalculator.R;

/**
 *
 */
public class Spinner extends MaterialAutoCompleteTextView {

    /**
     *
     */
    private List<String> items;

    public Spinner(@NonNull Context context) {
        super(context);
        setKeyListener(null);
    }

    public Spinner(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        setKeyListener(null);
    }

    public Spinner(@NonNull Context context, @Nullable AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        setKeyListener(null);
    }

    /**
     *
     */
    public void setAdapter(List<String> items) {

        this.items = items;

        SpinnerAdapter adapter = new SpinnerAdapter(
            getContext(),
            R.layout.list_item,
            items
        );

        setAdapter(adapter);
        setSelectedItem(0);
    }

    /**
     *
     * @param index
     */
    public void setSelectedItem(int index) {

        if (items == null) {
            return;
        }

        if (index >= items.size()) {
            return;
        }

        setText(items.get(index), false);
    }
}
