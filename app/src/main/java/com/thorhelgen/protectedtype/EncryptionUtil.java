package com.thorhelgen.protectedtype;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;


import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

class EncryptionUtil {
    /* Singleton class */

    private static final String CIPHER_INSTANCE = "RSA";
    private static final String INTERLOCUTOR_KEY = "public_interlocutor_key";
    private static final String PRIVATE_KEY = "private_key";

    private static Cipher encrypter;
    private static Cipher decrypter;

    private static EncryptionUtil singleInstance;


    private EncryptionUtil(Context preferencesContext)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    InvalidKeySpecException {

        SharedPreferences preferences = getDefaultSharedPreferences(preferencesContext);

        KeyFactory keyFactory = KeyFactory.getInstance(CIPHER_INSTANCE);
        // Initializing of the encoder with the public key of the interlocutor
        encrypter = Cipher.getInstance(CIPHER_INSTANCE);
        String base64EncodedInterlocutorKey = preferences.getString(INTERLOCUTOR_KEY, null);
        if (base64EncodedInterlocutorKey == null) {
            throw new InvalidKeyException(preferencesContext.getString(R.string.no_interlocutor_ex));
        }
        byte[] encryptionKeyBytes = Base64.decode(base64EncodedInterlocutorKey, Base64.DEFAULT);
        // Importing RSA public key
        X509EncodedKeySpec encryptionKey = new X509EncodedKeySpec(encryptionKeyBytes);
        encrypter.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(encryptionKey));
        // Initializing of the decoder with the private key of the phone owner
        decrypter = Cipher.getInstance(CIPHER_INSTANCE);
        String base64EncodedPrivateKey = preferences.getString(PRIVATE_KEY, null);
        byte[] decryptionKeyBytes = Base64.decode(base64EncodedPrivateKey, Base64.DEFAULT);
        // Importing RSA private key
        PKCS8EncodedKeySpec decryptionKey = new PKCS8EncodedKeySpec(decryptionKeyBytes);
        decrypter.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(decryptionKey));
    }

    public static EncryptionUtil getInstance(Context preferencesContext)
            throws InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException,
                    NoSuchPaddingException {
        if (singleInstance == null) {
            singleInstance = new EncryptionUtil(preferencesContext);
        }
        return singleInstance;
    }

    public String encryptChar(int symbol) throws BadPaddingException, IllegalBlockSizeException {
        byte[] encryptedBytes = encrypter.doFinal(new String(Character.toChars(symbol)).getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    public char decryptChar(String encryptedSymbol) throws BadPaddingException, IllegalBlockSizeException {
        byte[] decodedSymbol = Base64.decode(encryptedSymbol, Base64.DEFAULT);
        return new String(decrypter.doFinal(decodedSymbol)).charAt(0);
    }

    public static void updateKeys(Context preferencesContext)
            throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
                    NoSuchPaddingException {
        singleInstance = new EncryptionUtil(preferencesContext);
    }
}
