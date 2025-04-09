package com.example.quanlynhahang.Ultil;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.quanlynhahang.R;

public class CustomToast {
    private final View layout; // Keep layout final
    private final Context context; // Store context properly

    public CustomToast(Context context) {
        this.context = context; // Assign context to prevent NullPointerException
        LayoutInflater inflater = LayoutInflater.from(context);
        layout = inflater.inflate(R.layout.custom_toast, null);
    }

    public void showToast(String message, boolean isSuccess) {
        int typeIcon = isSuccess ? R.drawable.checked : R.drawable.unchecked;
        int border = isSuccess ? R.drawable.success_border : R.drawable.error_border;

        TextView textView = layout.findViewById(R.id.message);
        ImageView icon = layout.findViewById(R.id.imageView12);
        LinearLayout linearLayout = layout.findViewById(R.id.linear);

        if (textView != null) textView.setText(message);
        if (icon != null) icon.setImageResource(typeIcon);
        if (linearLayout != null) {
            linearLayout.setBackground(AppCompatResources.getDrawable(context, border));
        } else {
            Log.e("CustomToast", "LinearLayout (R.id.linear) is null. Check custom_toast.xml.");
        }

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.show();
    }
}
