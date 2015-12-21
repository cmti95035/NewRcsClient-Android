package mobisocial.crypto;

import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.crypto.mac.NativeMac;
import com.facebook.crypto.cipher.NativeGCMCipher;

import java.util.Arrays;

public class CloudedKeyChain implements KeyChain {

    private byte[] mCipher; // must be 16 bytes
    private byte[] mMac;    // must be 64 bytes
    private byte[] mIV;     // must be 12 bytes

    private static final byte[] mock = {0x32, 0x43, 0x4e, 0x2a, 0x72, 0x19,
            0x0c,
            0x55, 0x6f, 0x33, 0x48, 0x7e, 0x66, 0x51, 0x28, 0x11};

    public CloudedKeyChain(boolean isMock) {

        if (isMock) {
            mCipher = mock;
        } else {
            // Need request from server, set null for now
            mCipher= null;
        }
        mMac = getMac(mock);
        mIV = getIV(mock);

    }

    @Override
    public byte[] getCipherKey() throws KeyChainException {
        return mCipher;
    }

    @Override
    public byte[] getMacKey() throws KeyChainException {
        return mMac;
    }

    @Override
    public byte[] getNewIV() throws KeyChainException {
        return mIV;
    }

    @Override
    public void destroyKeys() {
        if (mCipher != null) {
            Arrays.fill(mCipher, (byte) 0);
        }
        if (mMac != null) {
            Arrays.fill(mMac, (byte) 0);
        }
        mMac = null;
        mCipher = null;
    }

    private byte[] getMac(byte[] input) {
        byte[] result = new byte[NativeMac.KEY_LENGTH];
        for (int i = 0; i< 4; i++) {
            System.arraycopy(input, 0, result, input.length*i, input.length);
        }
        return result;
    }

    private byte[] getIV(byte[] input) {
        return Arrays.copyOf(input, NativeGCMCipher.IV_LENGTH);
    }
}
