package com.example.passwordmanager;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {
    static public String encrypt(String message, String password){
        try {
            byte[] srcBuff = message.getBytes("UTF8");
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
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
}
