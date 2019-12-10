package com.example.passwordmanager;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {
    static public String encrypt(String message, String password, byte[] iv){
        try {
            byte[] srcBuff = message.getBytes("UTF8");
            SecretKeySpec skeySpec = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            ecipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

            byte[] dstBuff = ecipher.doFinal(srcBuff);

            String base64 = Base64.encodeToString(dstBuff, Base64.DEFAULT);

            return base64;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    static public String decrypt(String message, String password, String iv){
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(password.getBytes("UTF8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes("UTF8"));

            Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            ecipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);

            byte[] raw = Base64.decode(message, Base64.DEFAULT);

            byte[] originalBytes = ecipher.doFinal(raw);

            String original = new String(originalBytes, "UTF8");

            return original;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    static public byte[] hashPassword(String password, byte[] salt2){
        int iterations = 100000;
        int keyLength = 512;
        char[] passwordChars = password.toCharArray();
        byte[] res2;
        try
        {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec2 = new PBEKeySpec( passwordChars, salt2, iterations, keyLength );
            SecretKey key2 = keyFactory.generateSecret( spec2 );
            res2 = key2.getEncoded( );
            String saltString2 = new String(salt2);
            return res2;
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    static class DeCryptor {

        private static final String TRANSFORMATION = "AES/GCM/NoPadding";
        private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

        private KeyStore keyStore;

        DeCryptor() {
            try {
                initKeyStore();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        private void initKeyStore() throws KeyStoreException, CertificateException,
                NoSuchAlgorithmException, IOException {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        String decryptData(final String alias, final byte[] encryptedData, final byte[] encryptionIv)
                throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
                NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
                BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
            try {
                Log.i("TESTDEcrypt", "00");
                final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                Log.i("TESTDEcrypt", "0");
                final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv);
                Log.i("TESTDEcrypt", "1");
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(alias), spec);
                Log.i("TESTDEcrypt", "2");
                String res = new String(cipher.doFinal(encryptedData), "UTF-8");
                Log.i("TESTDEcrypt", res);
                return res;
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        private SecretKey getSecretKey(final String alias) throws NoSuchAlgorithmException,
                UnrecoverableEntryException, KeyStoreException {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null)).getSecretKey();
        }
    }

    static class EnCryptor {

        private static final String TRANSFORMATION = "AES/GCM/NoPadding";
        private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

        private byte[] encryption;
        private byte[] iv;

        EnCryptor() {
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        byte[] encryptText(final String alias, final String textToEncrypt)
                throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
                NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException,
                InvalidAlgorithmParameterException, SignatureException, BadPaddingException,
                IllegalBlockSizeException {

            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(alias));

            iv = cipher.getIV();

            return (encryption = cipher.doFinal(textToEncrypt.getBytes("UTF-8")));
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @NonNull
        private SecretKey getSecretKey(final String alias) throws NoSuchAlgorithmException,
                NoSuchProviderException, InvalidAlgorithmParameterException {

            final KeyGenerator keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

            keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());

            return keyGenerator.generateKey();
        }

        byte[] getEncryption() {
            return encryption;
        }

        byte[] getIv() {
            return iv;
        }
    }
}
