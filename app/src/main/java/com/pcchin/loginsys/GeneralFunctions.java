package com.pcchin.loginsys;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;

class GeneralFunctions {
    // Static variables: Creation date, User ID, Device ID (GUID)
    // Encryption (Salt) : AES, Blowfish, RSA
    // Hashing (Password, restore code) : SHA, PBKDF2

    // Password: Salt, GUID
    // Code: Salt, creation date
    // Admin code: GUID * 2
    @Contract("_, _, _ -> new")
    @NotNull
    static String passwordHash(@NotNull String original, @NotNull String salt, @NotNull String guid) {
        byte[] responseByte;

        // 1) PBKDF2
        PBEParametersGenerator pbkdfGen = new PKCS5S2ParametersGenerator();
        pbkdfGen.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(original.toCharArray()),
                salt.getBytes(), 10000);
        KeyParameter params = (KeyParameter)pbkdfGen.generateDerivedParameters(128);
        responseByte = params.getKey();

        // 2) Blowfish with GUID
        try {
            Cipher blowfishCipher = Cipher.getInstance("BLOWFISH/CBC/NoPadding");
            SecretKeySpec blowfishKeySpec = new SecretKeySpec(guid.getBytes(), "BLOWFISH");
            blowfishCipher.init(Cipher.ENCRYPT_MODE, blowfishKeySpec);
            responseByte = blowfishCipher.doFinal(responseByte);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        // 3) SHA
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest shaDigest = new SHA3.Digest512();
        responseByte = shaDigest.digest(responseByte);

        return new String(responseByte, UTF_8);
    }

    static void storeBitmap(@NotNull Bitmap bitmap, String fullPath) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(fullPath), false);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Bitmap getBitmap(String fullPath) {
        Bitmap bitmap = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(fullPath));
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}