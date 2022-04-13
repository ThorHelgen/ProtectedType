package com.thorhelgen.protectedtype;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import android.util.Base64;

import javax.crypto.NoSuchPaddingException;


public class SettingsActivity extends AppCompatActivity {

    private final String PUBLIC_KEY = "public_key";
    private final String PRIVATE_KEY = "private_key";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        // Viewing components of the settings on activity
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        SharedPreferences preferences = PreferenceManager
                                        .getDefaultSharedPreferences(getApplicationContext());
        if (!preferences.contains(PRIVATE_KEY)) {
            SharedPreferences.Editor preferenceEditor = preferences.edit();
            // Generating and saving RSA keys in preferences
            KeyPairGenerator kGenerator;
            try {
                kGenerator = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            kGenerator.initialize(2048);
            KeyPair kPair = kGenerator.generateKeyPair();

            preferenceEditor.putString(PUBLIC_KEY,
                                        Base64.encodeToString(kPair.getPublic().getEncoded(),
                                                                Base64.DEFAULT)).apply();
            preferenceEditor.putString(PRIVATE_KEY,
                                        Base64.encodeToString(kPair.getPrivate().getEncoded(),
                                                                Base64.DEFAULT)).apply();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final String COPY_PUBLIC_BTN_KEY = "copy_public_key";
        private final String REGENERATE_BTN_KEY = "regenerate_keys";
        private final String INTERLOCUTOR_KEY_ETD = "public_interlocutor_key";
        private final String PUBLIC_KEY = "public_key";
        private final String PRIVATE_KEY = "private_key";


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Setting and initializing preferences fragment components
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference copyPublicKeyBtn = getPreferenceManager().findPreference(COPY_PUBLIC_BTN_KEY);
            if (copyPublicKeyBtn != null) {
                copyPublicKeyBtn.setOnPreferenceClickListener(this::onCopyPublicKeyBtnClick);
            }
            Preference regenerateKeysBtn = getPreferenceManager().findPreference(REGENERATE_BTN_KEY);
            if (regenerateKeysBtn != null) {
                regenerateKeysBtn.setOnPreferenceClickListener(this::onRegenerateKeysBtnClick);
            }
            Preference interlocutorKeyEdt = getPreferenceManager().findPreference(INTERLOCUTOR_KEY_ETD);
            if (interlocutorKeyEdt != null) {
                interlocutorKeyEdt.setOnPreferenceChangeListener(this::onInterlocutorKeyChanged);
            }
        }

        private boolean onCopyPublicKeyBtnClick(Preference pref) {
            // Copying the public key from preferences to the clipboard
            String base64EncodedPublicKey = getPreferenceManager()
                    .getSharedPreferences()
                    .getString(PUBLIC_KEY, null);
            ClipData clipText = ClipData.newPlainText(PUBLIC_KEY, base64EncodedPublicKey);
            ((ClipboardManager) getContext()
                    .getSystemService(CLIPBOARD_SERVICE)
            ).setPrimaryClip(clipText);
            Toast.makeText(getContext(),
                    "Открытый ключ скопирован в буфер обмена",
                        Toast.LENGTH_SHORT).show();
            return true;
        }

        private boolean onRegenerateKeysBtnClick(Preference pref) {
            SharedPreferences.Editor preferenceEditor = PreferenceManager
                                                        .getDefaultSharedPreferences(getContext())
                                                        .edit();
            // Generating new and replacing old RSA keys in preferences
            KeyPairGenerator kGenerator;
            try {
                kGenerator = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
            kGenerator.initialize(2048);
            KeyPair kPair = kGenerator.generateKeyPair();

            preferenceEditor.putString(PUBLIC_KEY,
                                        Base64.encodeToString(kPair.getPublic().getEncoded(),
                                                                Base64.DEFAULT)).apply();
            preferenceEditor.putString(PRIVATE_KEY,
                                        Base64.encodeToString(kPair.getPrivate().getEncoded(),
                                                                Base64.DEFAULT)).apply();
            try {
                // Replacing the keys
                EncryptionUtil.updateKeys(getContext());
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
                return false;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                return false;
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        private boolean onInterlocutorKeyChanged(Preference preference, Object newValue) {
            SharedPreferences.Editor preferenceEditor = PreferenceManager
                    .getDefaultSharedPreferences(getContext())
                    .edit();
            // Updating key in preferences
            preferenceEditor.putString(preference.getKey(), (String)newValue);
            preferenceEditor.apply();

            try {
                // Replacing the key
                EncryptionUtil.updateKeys(getContext());
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
                return false;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                return false;
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
                return false;
            }

            return false;
        }
    }
}