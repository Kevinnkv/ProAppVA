package com.vuongnk.appandroid.dialog;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.vuongnk.appandroid.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CustomDialogFragment extends DialogFragment {
    public static final String TAG = "CustomDialog";

    private ImageView imgStatus;
    private TextView tvTitle, tvMessage;
    private LinearLayout buttonContainer;
    private DialogType dialogType;
    private OnActionClickListener actionClickListener;
    private List<String> buttonTexts = new ArrayList<>();
    private OnMultiButtonClickListener multiButtonClickListener;

    public enum DialogType {
        SUCCESS, ERROR, WARNING
    }

    // Giao diện mới cho nhiều nút
    public interface OnMultiButtonClickListener {
        void onButtonClick(int buttonIndex);
    }

    // Giữ nguyên giao diện cũ để tương thích
    public interface OnActionClickListener {
        void onActionClick();
    }

    // Phương thức tạo instance cũ
    public static CustomDialogFragment newInstance(
            DialogType type,
            String title,
            String message,
            String buttonText) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("type", type);
        args.putString("title", title);
        args.putString("message", message);
        args.putString("buttonText", buttonText);
        fragment.setArguments(args);
        return fragment;
    }

    // Phương thức mới hỗ trợ nhiều nút
    public static CustomDialogFragment newInstanceMultiButton(
            DialogType type,
            String title,
            String message,
            List<String> buttonTexts) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("type", type);
        args.putString("title", title);
        args.putString("message", message);
        args.putStringArrayList("buttonTexts", new ArrayList<>(buttonTexts));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_dialog, container, false);

        // Ánh xạ view
        imgStatus = view.findViewById(R.id.img_status);
        tvTitle = view.findViewById(R.id.tv_title);
        tvMessage = view.findViewById(R.id.tv_message);
        buttonContainer = view.findViewById(R.id.button_container);

        // Lấy dữ liệu từ arguments
        if (getArguments() != null) {
            dialogType = (DialogType) getArguments().getSerializable("type");
            String title = getArguments().getString("title");
            String message = getArguments().getString("message");

            // Kiểm tra nếu có nhiều nút
            List<String> multiButtonTexts = getArguments().getStringArrayList("buttonTexts");
            if (multiButtonTexts != null && !multiButtonTexts.isEmpty()) {
                setupMultiButtonDialog(title, message, multiButtonTexts);
            } else {
                // Giữ nguyên logic cũ cho 1 nút
                String buttonText = getArguments().getString("buttonText");
                setupSingleButtonDialog(title, message, buttonText);
            }
        }

        return view;
    }

    // Phương thức setup cho dialog 1 nút (giữ nguyên logic cũ)
    private void setupSingleButtonDialog(String title, String message, String buttonText) {
        // Tạo nút duy nhất như code cũ
        MaterialButton btnAction = buttonContainer.findViewById(R.id.btn_primary);

        // Set icon và màu button theo type
        switch (dialogType) {
            case SUCCESS:
                imgStatus.setImageResource(R.drawable.ic_success);
                btnAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.green)));
                break;
            case WARNING:
                imgStatus.setImageResource(R.drawable.ic_warning);
                btnAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.orange)));
                break;
            case ERROR:
                imgStatus.setImageResource(R.drawable.ic_error);
                btnAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.red)));
                break;
        }

        // Set nội dung
        tvTitle.setText(title);
        tvMessage.setText(message);
        btnAction.setText(buttonText);

        // Set click listener
        btnAction.setOnClickListener(v -> {
            if (actionClickListener != null) {
                actionClickListener.onActionClick();
            }
            dismiss();
        });
    }

    // Phương thức setup cho dialog nhiều nút
    private void setupMultiButtonDialog(String title, String message, List<String> buttonTexts) {
        // Set icon
        switch (dialogType) {
            case SUCCESS:
                imgStatus.setImageResource(R.drawable.ic_success);
                break;
            case WARNING:
                imgStatus.setImageResource(R.drawable.ic_warning);
                break;
            case ERROR:
                imgStatus.setImageResource(R.drawable.ic_error);
                break;
        }

        // Set nội dung
        tvTitle.setText(title);
        tvMessage.setText(message);

        // Xóa nút cũ
        buttonContainer.removeAllViews();

        // Tạo các nút mới
        for (int i = 0; i < buttonTexts.size(); i++) {
            MaterialButton button = createButton(buttonTexts.get(i), i);
            buttonContainer.addView(button);
        }
    }

    // Tạo nút động
    private MaterialButton createButton(String text, int index) {
        MaterialButton button = new MaterialButton(requireContext());

        // Thiết lập layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.setMargins(8, 0, 8, 0);
        button.setLayoutParams(params);

        // Thiết lập style
        button.setText(text);
//        button.setTextAllCaps(false);

        // Xử lý màu sắc
        int backgroundColor;
        switch (dialogType) {
            case SUCCESS:
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.green);
                break;
            case WARNING:
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.orange);
                break;
            case ERROR:
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.red);
                break;
            default:
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.primary);
        }

        // Nút đầu tiên có màu đậm hơn, các nút sau là outline
        if (index == 0) {
            button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        } else {
            button.setStrokeColor(ColorStateList.valueOf(backgroundColor));
            button.setTextColor(backgroundColor);
            button.setStrokeWidth(2); // Đặt độ dày của đường viền
            button.setStrokeColor(ColorStateList.valueOf(Color.BLACK)); // Đặt màu đường viền
            button.setBackgroundColor(Color.TRANSPARENT); // Làm nền trong suốt
        }

        // Sự kiện click
        button.setOnClickListener(v -> {
            if (multiButtonClickListener != null) {
                multiButtonClickListener.onButtonClick(index);
            }
            dismiss();
        });

        return button;
    }

    // Giữ nguyên phương thức cũ để tương thích
    public void setOnActionClickListener(OnActionClickListener listener) {
        this.actionClickListener = listener;
    }

    // Phương thức mới cho nhiều nút
    public void setOnMultiButtonClickListener(OnMultiButtonClickListener listener) {
        this.multiButtonClickListener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // Lấy kích thước màn hình
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            // Tính toán width = 2/3 màn hình
            int width = (int) (displayMetrics.widthPixels * 0.8);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;

            // Set kích thước và background cho dialog
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}