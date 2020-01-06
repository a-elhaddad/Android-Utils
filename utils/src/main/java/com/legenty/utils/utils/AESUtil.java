package com.legenty.utils.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public abstract class AESUtil {
    private final static String HEX = "0123456789ABCDEF";
    private static final int keyLength = 16;
    private static final String defaultV = "0";

    public static String encrypt(String key, String src) throws Exception {
        // /src = Base64.encodeToString(src.getBytes(), Base64.DEFAULT);
        byte[] rawKey = toMakekey(key, keyLength, defaultV).getBytes();// key.getBytes();
        byte[] result = encrypt(rawKey, src.getBytes("utf-8"));
        // result = Base64.encode(result, Base64.DEFAULT);
        return toHex(result);
    }


    public static String encrypt2Java(String key, String src) throws Exception {
        // /src = Base64.encodeToString(src.getBytes(), Base64.DEFAULT);
        byte[] rawKey = toMakekey(key, keyLength, defaultV).getBytes();// key.getBytes();
        byte[] result = encrypt2Java(rawKey, src.getBytes("utf-8"));
        // result = Base64.encode(result, Base64.DEFAULT);
        return toHex(result);
    }

    public static String decrypt(String key, String encrypted) throws Exception {
        byte[] rawKey = toMakekey(key, keyLength, defaultV).getBytes();// key.getBytes();
        byte[] enc = toByte(encrypted);
        // enc = Base64.decode(enc, Base64.DEFAULT);
        byte[] result = decrypt(rawKey, enc);
        // /result = Base64.decode(result, Base64.DEFAULT);
        return new String(result, "utf-8");
    }


    private static String toMakekey(String str, int strLength, String val) {

        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(str).append(val);
                str = buffer.toString();
                strLen = str.length();
            }
        }
        return str;
    }


    private static byte[] encrypt(byte[] key, byte[] src) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        byte[] encrypted = cipher.doFinal(src);
        return encrypted;
    }

    private static byte[] encrypt2Java(byte[] key, byte[] src) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        return cipher.doFinal(src);
    }


    private static byte[] decrypt(byte[] key, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }



    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }



    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }


    public static Cipher initAESCipher(String sKey, int cipherMode) {
        // 创建Key gen
        // KeyGenerator keyGenerator = null;
        Cipher cipher = null;
        try {
            /*
             * keyGenerator = KeyGenerator.getInstance("AES");
             * keyGenerator.init(128, new SecureRandom(sKey.getBytes()));
             * SecretKey secretKey = keyGenerator.generateKey(); byte[]
             * codeFormat = secretKey.getEncoded(); SecretKeySpec key = new
             * SecretKeySpec(codeFormat, "AES"); cipher =
             * Cipher.getInstance("AES"); //初始化 cipher.init(cipherMode, key);
             */
            byte[] rawKey = toMakekey(sKey, keyLength, defaultV).getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (InvalidKeyException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cipher;
    }


    public static File encryptFile(File sourceFile, String toFile, String dir, String sKey) {
        // 新建临时加密文件
        File encrypfile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(sourceFile);
            encrypfile = new File(dir + toFile);
            outputStream = new FileOutputStream(encrypfile);
            Cipher cipher = initAESCipher(sKey, Cipher.ENCRYPT_MODE);
            // 以加密流写入文件
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
            byte[] cache = new byte[1024];
            int nRead = 0;
            while ((nRead = cipherInputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, nRead);
                outputStream.flush();
            }
            cipherInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();  // To change body of catch statement use
                // File | Settings | File Templates.
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();  // To change body of catch statement use
                // File | Settings | File Templates.
            }
        }
        return encrypfile;
    }


    public static File decryptFile(File sourceFile, String toFile, String dir, String sKey) {
        File decryptFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            decryptFile = new File(dir + toFile);
            Cipher cipher = initAESCipher(sKey, Cipher.DECRYPT_MODE);
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(decryptFile);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, r);
            }
            cipherOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();  // To change body of catch statement use
                // File | Settings | File Templates.
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();  // To change body of catch statement use
                // File | Settings | File Templates.
            }
        }
        return decryptFile;
    }
}
