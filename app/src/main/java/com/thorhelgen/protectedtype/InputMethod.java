package com.thorhelgen.protectedtype;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class InputMethod
        extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView view;
    private Keyboard[] layouts;
    private byte currentLayoutIdx;
    private boolean capsFlag;
    private Switch encryptSwt;
    private boolean isEncryptionOn = false;
    private EncryptionUtil encryption;
    private boolean isSymbolicOn = false;

    @Override
    public ViewGroup onCreateInputView() {
        capsFlag = false;
        currentLayoutIdx = 0;
        // Loading keyboard layouts
        initKeyboardsArray();
        // Viewing keyboard
        ConstraintLayout layoutView = (ConstraintLayout)getLayoutInflater()
                                            .inflate(R.layout.keyboard_layout, null);
        view = (KeyboardView)layoutView.getViewById(R.id.keyboard_view);
        view.setKeyboard(layouts[currentLayoutIdx]);
        view.setOnKeyboardActionListener(this);
        // Viewing top menu of the keyboard
        ConstraintLayout menuLayout = (ConstraintLayout)layoutView.getViewById(R.id.keyboardMenuLayout);
        encryptSwt = (Switch)menuLayout.getViewById(R.id.encryptSwt);
        encryptSwt.setOnCheckedChangeListener(this::encryptionSwtOnChangeListener);

        try {
            encryption = EncryptionUtil.getInstance(getApplicationContext());
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return layoutView;
    }

    @Override
    public void onPress(int primaryCode) {    }

    @Override
    public void onRelease(int primaryCode) {    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            capsFlag = !capsFlag;
            // Setting the keys to uppercase
            layouts[currentLayoutIdx].setShifted(capsFlag);
            view.invalidateAllKeys();
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
            // Switching to the next layout
            if (++currentLayoutIdx == layouts.length - 1) {
                currentLayoutIdx = 0;
            }
            view.setKeyboard(layouts[currentLayoutIdx]);
        } else if (primaryCode == -15) {
            isSymbolicOn = !isSymbolicOn;
            if (isSymbolicOn) {
                // Switching to the symbolic layout
                view.setKeyboard(layouts[layouts.length - 1]);
            } else {
                view.setKeyboard(layouts[currentLayoutIdx]);
            }
        } else if (primaryCode == Keyboard.KEYCODE_DONE) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            // Deleting a symbol to the left of the cursor
            inputConnection.deleteSurroundingText(1, 0);
        } else {
            char primaryCodeUTF16 = (char)primaryCode;
            if (capsFlag && Character.isLetter(primaryCodeUTF16)) {
                primaryCodeUTF16 = Character.toUpperCase(primaryCodeUTF16);
            }
            // Code letter
            String finalSymbol = String.valueOf(primaryCodeUTF16);
            if (isEncryptionOn) {
                try {
                    finalSymbol = encryption.encryptChar(primaryCodeUTF16);
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Committing the symbol to a text field
            inputConnection.commitText(finalSymbol, 1);
        }
    }

    @Override
    public void onText(CharSequence text) {    }

    @Override
    public void swipeLeft() {    }

    @Override
    public void swipeRight() {    }

    @Override
    public void swipeDown() {    }

    @Override
    public void swipeUp() {    }

    private void initKeyboardsArray() {
        layouts = new Keyboard[] {
                new Keyboard(this, R.xml.ru_keyboard_layout),
                new Keyboard(this, R.xml.en_keyboard_layout),
                new Keyboard(this, R.xml.symbolic_keyboard_layout)
        };
    }

    private void encryptionSwtOnChangeListener(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (encryption == null) {
                try {
                    encryption = EncryptionUtil.getInstance(getApplicationContext());
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    if (e.getMessage() == getString(R.string.no_interlocutor_ex)) {
                        Toast.makeText(this, getString(R.string.interlocutor_required), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error. Check the cryptographic keys", Toast.LENGTH_SHORT).show();
                    }
                    encryptSwt.setChecked(false);
                    return;
                }
            }
            isEncryptionOn = true;
            encryptSwt.setThumbResource(R.drawable.ic_encryption_on);

        }
        else {
            isEncryptionOn = false;
            encryptSwt.setThumbResource(R.drawable.ic_no_encryption);
        }
    }
}