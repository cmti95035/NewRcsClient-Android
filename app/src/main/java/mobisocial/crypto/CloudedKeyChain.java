package mobisocial.crypto;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.chinamobile.cloudStorageProxy.server.EncryptionKey;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.crypto.mac.NativeMac;
import com.facebook.crypto.cipher.NativeGCMCipher;

import java.util.Arrays;

import mobisocial.musubi.cloudstorage.Utils;

public class CloudedKeyChain implements KeyChain {

    private byte[] raw;
    private long timestamp;


    public CloudedKeyChain(Context context) {
        EncryptionKey key = Utils.requestKey(context);
        raw = key.getHash().getBytes();
        timestamp = key.getTimestamp();
    }

    public CloudedKeyChain(String hash, long timestamp) {
        raw = hash.getBytes();
        this.timestamp = timestamp;
    }

    @Override
    public byte[] getCipherKey() throws KeyChainException {
        return Arrays.copyOf(raw, NativeGCMCipher.KEY_LENGTH);
    }

    @Override
    public byte[] getMacKey() throws KeyChainException {
        // must be 64 bytes
        return Arrays.copyOf(raw, NativeMac.KEY_LENGTH);
    }

    @Override
    public byte[] getNewIV() throws KeyChainException {
        // must be 12 bytes
        return Arrays.copyOf(raw, NativeGCMCipher.IV_LENGTH);
    }

    @Override
    public void destroyKeys() {
        if (raw != null) {
            Arrays.fill(raw, (byte) 0);
        }
        raw = null;
     }

    public long getTimestamp() {
        return timestamp;
    }
}
