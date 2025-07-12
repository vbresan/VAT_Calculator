package biz.binarysolutions.vatcalculator.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import biz.binarysolutions.vatcalculator.R;

public class FlavorSpecific {

    private final Activity activity;

    private void donate(View view) {

        AlertDialog.Builder builder =
            new MaterialAlertDialogBuilder(activity, R.style.AlertDialog);

        builder.setTitle(R.string.DonateDialogTitle);
        builder.setMessage(R.string.DonateDialogContent);

        builder.setPositiveButton(R.string.Continue, (dialog, which) -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(activity.getString(R.string.donation_url)));
            activity.startActivity(intent);

            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public FlavorSpecific(Activity activity) {
        this.activity = activity;
    }

    public void initializeBottomBar() {

        Button button = activity.findViewById(R.id.buttonShowSupport);
        if (button != null) {
            button.setOnClickListener(this::donate);
        }
    }
}
