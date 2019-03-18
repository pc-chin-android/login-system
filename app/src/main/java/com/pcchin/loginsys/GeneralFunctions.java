package com.pcchin.loginsys;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.engines.BlowfishEngine;
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
import java.security.MessageDigest;
import java.security.Security;

class GeneralFunctions {
    // Static variables: Creation date, User ID, Device ID (GUID)
    // Encryption (Salt) : AES, Blowfish, RSA
    // Hashing (Password, restore code) : SHA, PBKDF2
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    // Password: Salt, GUID
    // Code: Salt, creation date
    // Admin code: GUID * 2
    @Contract("_, _, _ -> new")
    @NotNull
    static String passwordHash(@NotNull String original, @NotNull String salt, @NotNull String guid) {
        byte[] originalByte;

        // 1) PBKDF2 with salt
        PBEParametersGenerator pbkdfSaltGen = new PKCS5S2ParametersGenerator();
        pbkdfSaltGen.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(original.toCharArray()),
                salt.getBytes(), 20000);
        KeyParameter saltParams = (KeyParameter)pbkdfSaltGen.generateDerivedParameters(128);
        originalByte = saltParams.getKey();

        // 2) SHA
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest shaDigest = new SHA3.Digest512();
        originalByte = shaDigest.digest(originalByte);

        // 3) Blowfish with GUID
        BlowfishEngine blowfishEngine = new BlowfishEngine();
        blowfishEngine.init(true,  new KeyParameter(guid.getBytes()));
        byte[] responseByte = new byte[originalByte.length];
        blowfishEngine.processBlock(originalByte, original.length(), responseByte, 0);

        return bytesToHex(responseByte);
    }

    // Only used in passhash(), separated for clarity
    @NotNull
    @Contract("_ -> new")
    private static String bytesToHex(@NotNull byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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