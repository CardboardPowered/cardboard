/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cardboardpowered.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Operations to simplify common {@link java.security.MessageDigest} tasks.
 * This class is immutable and thread-safe.
 * However the MessageDigest instances it creates generally won't be.
 * <p>
 * The {@link MessageDigestAlgorithms} class provides constants for standard
 * digest algorithms that can be used with the {@link #getDigest(String)} method
 * and other methods that require the Digest algorithm name.
 * <p>
 * Note: the class has short-hand methods for all the algorithms present as standard in Java 6.
 * This approach requires lots of methods for each algorithm, and quickly becomes unwieldy.
 * The following code works with all algorithms:
 * <pre>
 * import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_224;
 * ...
 * byte [] digest = new DigestUtils(SHA_224).digest(dataToDigest);
 * String hdigest = new DigestUtils(SHA_224).digestAsHex(new File("pom.xml"));
 * </pre>
 * @see MessageDigestAlgorithms
 */
public class DigestUtil {

    private static final int STREAM_BUFFER_LENGTH = 1024;

    /**
     * Reads through a byte array and returns the digest for the data. Provided for symmetry with other methods.
     *
     * @param messageDigest
     *            The MessageDigest to use (e.g. MD5)
     * @param data
     *            Data to digest
     * @return the digest
     * @since 1.11
     */
    public static byte[] digest(final MessageDigest messageDigest, final byte[] data) {
        return messageDigest.digest(data);
    }

    /**
     * Reads through a ByteBuffer and returns the digest for the data
     *
     * @param messageDigest
     *            The MessageDigest to use (e.g. MD5)
     * @param data
     *            Data to digest
     * @return the digest
     *
     * @since 1.11
     */
    public static byte[] digest(final MessageDigest messageDigest, final ByteBuffer data) {
        messageDigest.update(data);
        return messageDigest.digest();
    }

    /**
     * Reads through a File and returns the digest for the data
     *
     * @param messageDigest
     *            The MessageDigest to use (e.g. MD5)
     * @param data
     *            Data to digest
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public static byte[] digest(final MessageDigest messageDigest, final File data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * Reads through an InputStream and returns the digest for the data
     *
     * @param messageDigest
     *            The MessageDigest to use (e.g. MD5)
     * @param data
     *            Data to digest
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11 (was private)
     */
    public static byte[] digest(final MessageDigest messageDigest, final InputStream data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * Reads through a File and returns the digest for the data
     *
     * @param messageDigest
     *            The MessageDigest to use (e.g. MD5)
     * @param data
     *            Data to digest
     * @param options
     *            options How to open the file
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.14
     */
    public static byte[] digest(final MessageDigest messageDigest, final Path data, final OpenOption... options)
        throws IOException {
        return updateDigest(messageDigest, data, options).digest();
    }

    /**
     * Reads through a RandomAccessFile using non-blocking-io (NIO) and returns the digest for the data
     *
     * @param messageDigest The MessageDigest to use (e.g. MD5)
     * @param data Data to digest
     * @return the digest
     * @throws IOException On error reading from the stream
     * @since 1.14
     */
    public static byte[] digest(final MessageDigest messageDigest, final RandomAccessFile data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * Returns a {@code MessageDigest} for the given {@code algorithm}.
     *
     * @param algorithm
     *            the name of the algorithm requested. See <a
     *            href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA"
     *            >Appendix A in the Java Cryptography Architecture Reference Guide</a> for information about standard
     *            algorithm names.
     * @return A digest instance.
     * @see MessageDigest#getInstance(String)
     * @throws IllegalArgumentException
     *             when a {@link NoSuchAlgorithmException} is caught.
     */
    public static MessageDigest getDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns a {@code MessageDigest} for the given {@code algorithm} or a default if there is a problem
     * getting the algorithm.
     *
     * @param algorithm
     *            the name of the algorithm requested. See
     *            <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA" >
     *            Appendix A in the Java Cryptography Architecture Reference Guide</a> for information about standard
     *            algorithm names.
     * @param defaultMessageDigest
     *            The default MessageDigest.
     * @return A digest instance.
     * @see MessageDigest#getInstance(String)
     * @throws IllegalArgumentException
     *             when a {@link NoSuchAlgorithmException} is caught.
     * @since 1.11
     */
    public static MessageDigest getDigest(final String algorithm, final MessageDigest defaultMessageDigest) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final Exception e) {
            return defaultMessageDigest;
        }
    }

    /**
     * Returns an MD5 MessageDigest.
     *
     * @return An MD5 digest instance.
     * @throws IllegalArgumentException
     *             when a {@link NoSuchAlgorithmException} is caught, which should never happen because MD5 is a
     *             built-in algorithm
     * @see MessageDigestAlgorithms#MD5
     */
    public static MessageDigest getMd5Digest() {
        return getDigest("MD5");
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element {@code byte[]}.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest
     */
    public static byte[] md5(final byte[] data) {
        return getMd5Digest().digest(data);
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element {@code byte[]}.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.4
     */
    public static byte[] md5(final InputStream data) throws IOException {
        return digest(getMd5Digest(), data);
    }


    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(final byte[] data) {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest as a hex string
     * @throws IOException
     *             On error reading from the stream
     * @since 1.4
     */
    public static String md5Hex(final InputStream data) throws IOException {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * Updates the given {@link MessageDigest}.
     *
     * @param messageDigest
     *            the {@link MessageDigest} to update
     * @param valueToDigest
     *            the value to update the {@link MessageDigest} with
     * @return the updated {@link MessageDigest}
     * @since 1.7
     */
    public static MessageDigest updateDigest(final MessageDigest messageDigest, final byte[] valueToDigest) {
        messageDigest.update(valueToDigest);
        return messageDigest;
    }

    /**
     * Updates the given {@link MessageDigest}.
     *
     * @param messageDigest
     *            the {@link MessageDigest} to update
     * @param valueToDigest
     *            the value to update the {@link MessageDigest} with
     * @return the updated {@link MessageDigest}
     * @since 1.11
     */
    public static MessageDigest updateDigest(final MessageDigest messageDigest, final ByteBuffer valueToDigest) {
        messageDigest.update(valueToDigest);
        return messageDigest;
    }

    /**
     * Reads through a File and updates the digest for the data
     *
     * @param digest
     *            The MessageDigest to use (e.g. MD5)
     * @param data
     *            Data to digest
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public static MessageDigest updateDigest(final MessageDigest digest, final File data) throws IOException {
        try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(data))) {
            return updateDigest(digest, inputStream);
        }
    }

    /**
     * Reads through a RandomAccessFile and updates the digest for the data using non-blocking-io (NIO).
     *
     * TODO Decide if this should be public.
     *
     * @param digest The MessageDigest to use (e.g. MD5)
     * @param data Data to digest
     * @return the digest
     * @throws IOException On error reading from the stream
     * @since 1.14
     */
    private static MessageDigest updateDigest(final MessageDigest digest, final FileChannel data) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(STREAM_BUFFER_LENGTH);
        while (data.read(buffer) > 0) {
            buffer.flip();
            digest.update(buffer);
            buffer.clear();
        }
        return digest;
    }

    /**
     * Reads through an InputStream and updates the digest for the data
     *
     * @param digest
     *            The MessageDigest to use (e.g. MD5)
     * @param inputStream
     *            Data to digest
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.8
     */
    public static MessageDigest updateDigest(final MessageDigest digest, final InputStream inputStream)
        throws IOException {
        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        int read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);

        while (read > -1) {
            digest.update(buffer, 0, read);
            read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        return digest;
    }

    /**
     * Reads through a Path and updates the digest for the data
     *
     * @param digest
     *            The MessageDigest to use (e.g. MD5)
     * @param path
     *            Data to digest
     * @param options
     *            options How to open the file
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.14
     */
    public static MessageDigest updateDigest(final MessageDigest digest, final Path path, final OpenOption... options)
        throws IOException {
        try (final BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(path, options))) {
            return updateDigest(digest, inputStream);
        }
    }

    /**
     * Reads through a RandomAccessFile and updates the digest for the data using non-blocking-io (NIO)
     *
     * @param digest The MessageDigest to use (e.g. MD5)
     * @param data Data to digest
     * @return the digest
     * @throws IOException On error reading from the stream
     * @since 1.14
     */
    public static MessageDigest updateDigest(final MessageDigest digest, final RandomAccessFile data)
            throws IOException {
        return updateDigest(digest, data.getChannel());
    }


    private final MessageDigest messageDigest;

   /**
    * Preserves binary compatibility only.
    * As for previous versions does not provide useful behavior
    * @deprecated since 1.11; only useful to preserve binary compatibility
    */
   @Deprecated
    public DigestUtil() {
        this.messageDigest = null;
    }

    /**
     * Creates an instance using the provided {@link MessageDigest} parameter.
     *
     * This can then be used to create digests using methods such as
     * {@link #digest(byte[])} and {@link #digestAsHex(File)}.
     *
     * @param digest the {@link MessageDigest} to use
     * @since 1.11
     */
    public DigestUtil(final MessageDigest digest) {
        this.messageDigest = digest;
    }

    /**
     * Creates an instance using the provided {@link MessageDigest} parameter.
     *
     * This can then be used to create digests using methods such as
     * {@link #digest(byte[])} and {@link #digestAsHex(File)}.
     *
     * @param name the name of the {@link MessageDigest} to use
     * @see #getDigest(String)
     * @throws IllegalArgumentException
     *             when a {@link NoSuchAlgorithmException} is caught.
     * @since 1.11
     */
    public DigestUtil(final String name) {
        this(getDigest(name));
    }

    /**
     * Reads through a byte array and returns the digest for the data.
     *
     * @param data
     *            Data to digest
     * @return the digest
     * @since 1.11
     */
    public byte[] digest(final byte[] data) {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * Reads through a ByteBuffer and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest
     *
     * @since 1.11
     */
    public byte[] digest(final ByteBuffer data) {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * Reads through a File and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public byte[] digest(final File data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * Reads through an InputStream and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public byte[] digest(final InputStream data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * Reads through a File and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @param options
     *            options How to open the file
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.14
     */
    public byte[] digest(final Path data, final OpenOption... options) throws IOException {
        return updateDigest(messageDigest, data, options).digest();
    }

    /**
     * Reads through a byte array and returns the digest for the data.
     *
     * @param data
     *            Data to digest
     * @return the digest as a hex string
     * @since 1.11
     */
    public String digestAsHex(final byte[] data) {
        return Hex.encodeHexString(digest(data));
    }

    /**
     * Reads through a ByteBuffer and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest as a hex string
     *
     * @since 1.11
     */
    public String digestAsHex(final ByteBuffer data) {
        return Hex.encodeHexString(digest(data));
    }

    /**
     * Reads through a File and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest as a hex string
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public String digestAsHex(final File data) throws IOException {
        return Hex.encodeHexString(digest(data));
    }

    /**
     * Reads through an InputStream and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest as a hex string
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public String digestAsHex(final InputStream data) throws IOException {
        return Hex.encodeHexString(digest(data));
    }

    /**
     * Reads through a File and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @param options
     *            options How to open the file
     * @return the digest as a hex string
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public String digestAsHex(final Path data, final OpenOption... options) throws IOException {
        return Hex.encodeHexString(digest(data, options));
    }

    /**
     * Returns the message digest instance.
     * @return the message digest instance
     * @since 1.11
     */
    public MessageDigest getMessageDigest() {
        return messageDigest;
    }

}
