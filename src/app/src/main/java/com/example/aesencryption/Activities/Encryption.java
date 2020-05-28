package com.example.aesencryption.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aesencryption.Functions.Encrypt;
import com.example.aesencryption.R;

import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Encryption extends Activity {

    @BindView(R.id.etOrjText)
    EditText etOrjText;
    @BindView(R.id.secretkey)
    TextView tvSecretKey;
    @BindView(R.id.sonuc)
    TextView tvSonuc;
    @BindView(R.id.anahtar)
    TextView tvAnahtar;
    @BindView(R.id.encBtn)
    Button encBtn;

    KeyGenerator keyGenerator;
    SecretKey secretKey;
    byte[] secretKeyen;
    String strSecretKey;
    String aeskey;
    byte[] IV = new byte[16];
    // Create a fixed IV here:
    byte[] myIV = "\u00e0\u004f\u00d0\u0020\u00ea\u003a\u0069\u0010\u00a2\u00d8\u0008\u0000\u002b\u0030\u0030\u009d".getBytes();
    byte[] cipherText;
    SecureRandom random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encryption);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.encBtn)
    public void btnEncodeClick() {
        if (TextUtils.isEmpty(etOrjText.getText())) {
            Toast t = Toast.makeText(this, "Fill empty field.", Toast.LENGTH_SHORT);
            t.show();
        } else {
            try {
                /*Keygenerator simetrik şifreleme anahtarı üretmek için kullanılan bir kütüphanedir.Öncelikle KeyGenerator kurulumu yapılır
                 * getInstance ile algoritma isminin bir parametre olarak alınması engellenir.*/
                keyGenerator = KeyGenerator.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            // The hardcodekey String we are going to use
            String hardcodekey = new String("okjkT06mZKLdJKdGRVpJPpbQTcgv2X0NCDqwavYCEbQ=");
            // Key generation
            keyGenerator.init(256);// init yöntemiyle oluşturulan KeyGenerator örneği başlatılır.Burada 256bit değer kullanıldı.
            secretKey = keyGenerator.generateKey();//Kurulum tamamlandıktan sonra generateKey() ile anahtar üretilir.
            secretKeyen=secretKey.getEncoded();
            strSecretKey = encoderfun(secretKeyen);

            System.out.println("Java key generator> "+strSecretKey);

            // Use password to load key in the android key store with name "key2"
            String password = "someString";
            char[] charArray = password.toCharArray();

            SecretKey keyStoreKey=null;
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                keyStoreKey = (SecretKey) keyStore.getKey("key2", charArray);
                // when the app run on the phone 1st time generate the key2 in the keystore
                if(keyStoreKey==null) {
                    System.out.println("============> Create a new key");
                    keyStore.setEntry(
                            "key2",
                            new KeyStore.SecretKeyEntry(secretKey),
                            new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                    .build());
                    keyStoreKey = (SecretKey) keyStore.getKey("key2", charArray);
                }

                // Key imported, obtain a reference to it.
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, keyStoreKey);

                IV = cipher.getIV();
                System.out.println(" Android keystore random IV: >>>>" + encoderfun(IV));
                byte[] encodedBytes = cipher.doFinal(new String(hardcodekey).getBytes("UTF-8"));
                System.out.println(" Android cipher encrypted text: >>>>" + encoderfun(encodedBytes));

                // decrypted
                Cipher dcipher = Cipher.getInstance("AES/GCM/NoPadding");
                final GCMParameterSpec spec = new GCMParameterSpec(128, cipher.getIV());
                dcipher.init(Cipher.DECRYPT_MODE, ((KeyStore.SecretKeyEntry) keyStore.getEntry("key2", null)).getSecretKey(), spec);
                byte[] encodedBytes2 = dcipher.doFinal(encodedBytes);
                // Set the java aes encrypt key:
                aeskey = new  String(encodedBytes2, "UTF-8");
                System.out.println(" Android cipher decrypted text: >>>>" +aeskey);

            } catch (Exception e){
                System.out.println("==");
                e.printStackTrace();
            }

            //2

            /*IV, Başlatma Vektörü anlamına gelir, şifreleme sırasında SecretKey ile birlikte kullanılacak isteğe bağlı bir sayıdır. IV, şifreleme işleminin başlangıcına rastgelelik ekler.
             * */
            //random = new SecureRandom();
            //random.nextBytes(IV);
            try {
                // add convert the string to secretkey

                // load the AES key in the java ciper
                byte[] encodedKey = Base64.decode(aeskey, Base64.DEFAULT);
                SecretKey originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
                cipherText = Encrypt.encrypt(etOrjText.getText().toString().trim().getBytes(), originalKey, myIV);

                //cipherText = Encrypt.encrypt(etOrjText.getText().toString().trim().getBytes(), secretKey, IV);

                tvSecretKey.setText(aeskey);

                String sonuc = encoderfun(cipherText);
                tvSonuc.setText(sonuc);

                //String tvIV = encoderfun(IV);
                String tvIV = encoderfun(myIV);

                tvAnahtar.setText(tvIV);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static String encoderfun(byte[] decval) {
        String conVal= Base64.encodeToString(decval,Base64.DEFAULT);
        return conVal;
    }
    public static byte[] decoderfun(String enval) {
        byte[] conVal = Base64.decode(enval,Base64.DEFAULT);
        return conVal;

    }

}