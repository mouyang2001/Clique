package com.matthew.clique.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.matthew.clique.R;

public class MessageOptionsDialog extends BottomSheetDialogFragment {
    private MessageOptionsListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_message_options, container, false);

        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

//        TextView deleteButton = view.findViewById(R.id.textViewMessageOptionsDelete);
//
//        deleteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.onMessageOptionClicked("Delete button clicked");
//                dismiss();
//            }
//        });
//


        return view;
    }

    //todo implement listeners for option menu;
    public interface MessageOptionsListener {
        void onMessageOptionClicked(String text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (MessageOptionsListener) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + "Must implement message options listener");
        }

    }
}
