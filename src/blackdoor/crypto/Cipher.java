package blackdoor.crypto;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.spec.IvParameterSpec;

public interface Cipher {
	 /**
     * Returns the algorithm name of this <code>Cipher</code> object.
     *
     * @return the algorithm name of this <code>Cipher</code> object.
     */
	public String getAlgorithm();
	
    /**
     * Returns the block size (in bytes).
     *
     * @return the block size (in bytes), or 0 if the underlying algorithm is
     * not a block cipher
     */
	public int getBlockSize();
	
    /**
     * Returns the initialization vector (IV) in a new buffer.
     *
     * <p>This is useful in the case where a random IV was created,
     * or in the context of password-based encryption or
     * decryption, where the IV is derived from a user-supplied password.
     *
     * @return the initialization vector in a new buffer, or null if the
     * underlying algorithm does not use an IV, or if the IV has not yet
     * been set.
     */
	public byte[] getIV();
	
	/**
     * Initializes this cipher with a key and IV.
     *
     * <p>If this cipher requires any algorithm parameters that cannot be
     * derived from the given <code>key</code>, the underlying cipher
     * implementation is supposed to generate the required parameters itself
     * (using provider-specific default or random values) if it is being
     * initialized for encryption or key wrapping, and raise an
     * <code>InvalidKeyException</code> if it is being
     * initialized for decryption or key unwrapping.
     * The generated parameters can be retrieved using
     * {@link #getParameters() getParameters} or
     * {@link #getIV() getIV} (if the parameter is an IV).
     *
     * <p>If this cipher requires algorithm parameters that cannot be
     * derived from the input parameters, and there are no reasonable
     * provider-specific default values, initialization will
     * necessarily fail.
     *
     * <p>If this cipher (including its underlying feedback or padding scheme)
     * requires any random bytes (e.g., for parameter generation), it will get
     * them using the {@link SecureRandom <code>SecureRandom</code>}
     * implementation of the highest-priority
     * installed provider as the source of randomness.
     * (If none of the installed providers supply an implementation of
     * SecureRandom, a system-provided source of randomness will be used.)
     *
     * <p>Note that when a Cipher object is initialized, it loses all
     * previously-acquired state. In other words, initializing a Cipher is
     * equivalent to creating a new instance of that Cipher and initializing
     * it.
     *
     * @param key the key
     * @param iv the iv
     *
     * @exception InvalidKeyException if the given key is inappropriate for
     * initializing this cipher, or requires
     * algorithm parameters that cannot be
     * determined from the given key, or if the given key has a keysize that
     * exceeds the maximum allowable keysize (as determined from the
     * configured jurisdiction policy files).
     */
	public void init(Key key, IvParameterSpec iv) throws InvalidKeyException;
	
	byte[] update(byte[] input);
	
	
}
