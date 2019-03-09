package com.pcchin.loginsys;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;

class SecurityFunctions {
    // Static variables: Creation date, User ID, Device ID
    // Encryption (Salt) : AES, Blowfish, RSA, Elliptic-curve cryptography
    // Hashing (Password, restore code) : SHA, PBKDF2

    @NotNull
    @Contract("_, _ -> new")
    static String hash(@NotNull String original, @NotNull String salt) {
        byte[] responseByte;

        // 1) PBKDF2
        PBEParametersGenerator pbkdfGen = new PKCS5S2ParametersGenerator();
        pbkdfGen.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(original.toCharArray()), salt.getBytes(), 1000);
        KeyParameter params = (KeyParameter)pbkdfGen.generateDerivedParameters(128);
        responseByte = params.getKey();

        // 2) Blowfish with salt
        try {
            Cipher blowfishCipher = Cipher.getInstance("BLOWFISH/CBC/NoPadding");
            SecretKeySpec blowfishKeySpec = new SecretKeySpec(salt.getBytes(), "BLOWFISH");
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

        // 4) RSA with Device ID
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding");
            SecretKeySpec rsaKeySpec = new SecretKeySpec(UUID.randomUUID().toString().getBytes(),
                    "RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaKeySpec);
            responseByte = rsaCipher.doFinal(responseByte);
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

        return new String(responseByte, UTF_8);
    }
}
