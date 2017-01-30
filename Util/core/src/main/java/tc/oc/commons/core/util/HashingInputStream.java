package tc.oc.commons.core.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: This is copypast from Guava - use the real one when we upgrade
 */
public class HashingInputStream extends FilterInputStream {
    private final Hasher hasher;

    /**
     * Creates an input stream that hashes using the given {@link com.google.common.hash.HashFunction} and delegates all data
     * read from it to the underlying {@link java.io.InputStream}.
     *
     * <p>The {@link java.io.InputStream} should not be read from before or after the hand-off.
     */
    public HashingInputStream(HashFunction hashFunction, InputStream in) {
      super(checkNotNull(in));
      this.hasher = checkNotNull(hashFunction.newHasher());
    }

    /**
     * Reads the next byte of data from the underlying input stream and updates the hasher with
     * the byte read.
     */
    @Override
    public int read() throws IOException {
      int b = in.read();
      if (b != -1) {
        hasher.putByte((byte) b);
      }
      return b;
    }

    /**
     * Reads the specified bytes of data from the underlying input stream and updates the hasher with
     * the bytes read.
     */
    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
      int numOfBytesRead = in.read(bytes, off, len);
      if (numOfBytesRead != -1) {
        hasher.putBytes(bytes, off, numOfBytesRead);
      }
      return numOfBytesRead;
    }

    /**
     * mark() is not supported for HashingInputStream
     * @return {@code false} always
     */
    @Override
    public boolean markSupported() {
      return false;
    }

    /**
     * mark() is not supported for HashingInputStream
     */
    @Override
    public void mark(int readlimit) {}

    /**
     * reset() is not supported for HashingInputStream.
     * @throws IOException this operation is not supported
     */
    @Override
    public void reset() throws IOException {
      throw new IOException("reset not supported");
    }

    /**
     * Returns the {@link com.google.common.hash.HashCode} based on the data read from this stream. The result is
     * unspecified if this method is called more than once on the same instance.
     */
    public HashCode hash() {
      return hasher.hash();
    }
}
