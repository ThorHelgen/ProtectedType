package com.thorhelgen.protectedtype;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DecryptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConstraintLayout content = (ConstraintLayout)getLayoutInflater().inflate(R.layout.decryption_activity, null);

        EncryptionUtil encryption = null;
        try {
            encryption = EncryptionUtil.getInstance(this);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        // Encrypted characters in Base64 encoding are separated by signs "="
        // Splitting text that is selected by the user to separate symbols encrypted by RSA and encoded by Base64.
        String[] splitToSymbols = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString().split("=");
        // Decrypting the text by each symbol
        StringBuilder decryptedText = new StringBuilder("");
        for (String symbol : splitToSymbols) {
            if (symbol.length() < 300) {
                continue;
            }

            try {
                decryptedText.append(encryption.decryptChar(symbol));
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
        // Setting decrypted text to dialog view
        setTitle(decryptedText.toString());
        // Preventing screenshots
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(content);
    }
}