package net.openhft.chronicle.decentred.remote.rpc;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.salt.Ed25519;

public class KeyPair {
    public final Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
    public final Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);

    public KeyPair(long id) {
        Bytes<Void> privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.zeroOut(0, 32);
        privateKey.writeLong(32 - Long.BYTES, id);
        privateKey.writeSkip(32);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);
        privateKey.release();
    }

}