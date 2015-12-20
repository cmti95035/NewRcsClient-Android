package mobisocial.crypto;

import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;

import java.security.SecureRandom;

public class CloudedKeyChain implements KeyChain {

    private byte[] mKey;

    public CloudedKeyChain(boolean isMock) {

        if (isMock) {
            final byte[] mock = new byte[16];
            new SecureRandom().nextBytes(mock);
            mKey = mock;
        } else {
            // Need request from server, set null for now
            mKey= null;
        }
    }

    @Override
    public byte[] getCipherKey() throws KeyChainException {
        return mKey;
    }

    @Override
    public byte[] getMacKey() throws KeyChainException {
        return mKey;
    }

    @Override
    public byte[] getNewIV() throws KeyChainException {
        return mKey;
    }

    @Override
    public void destroyKeys() {
        mKey = null;
    }
}
