package com.example.recipies;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Random;

public class VerificationDialogFragment extends DialogFragment {
    private String code;
    private EditText edtVerificationCode;
    private Button btnVerify;
    private Button btnSendSMS;

    public VerificationDialogFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verification_dialog, container, false);
        btnVerify = view.findViewById(R.id.btnVerify);
        edtVerificationCode=view.findViewById(R.id.edtVerificationCode);

        btnSendSMS = getActivity().findViewById(R.id.buttonSendSMS);

        Bundle bundle = getArguments();
        if (bundle != null) {
            code = bundle.getString("codigo");
        }

        verificacion();

        return view;
    }

    private void verificacion() {
        btnVerify.setOnClickListener(v -> {
            String enteredCode = edtVerificationCode.getText().toString();
            if (enteredCode.equals(code)) {
                dismiss();
                Toast.makeText(getContext(), "Verificado", Toast.LENGTH_SHORT).show();
                btnSendSMS.setEnabled(false);
            } else {
                Toast.makeText(getContext(), "El código es incorrecto", Toast.LENGTH_SHORT).show();
                edtVerificationCode.setText("");
            }
        });
    }
}