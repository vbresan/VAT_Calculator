package biz.binarysolutions.vatcalculator.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.List;

/**
 *
 */
public class SpinnerAdapter extends ArrayAdapter<String> {

    /**
     *
     * @param context
     * @param resource
     * @param objects
     */
    public SpinnerAdapter
        (
            @NonNull Context      context,
                     int          resource,
            @NonNull List<String> objects
        ) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence c) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence c, FilterResults r) {
            }
        };
    }
}
