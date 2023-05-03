package hu.lacztam.keepassservice.service;

import de.slackspace.openkeepass.crypto.*;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassHeader;
import de.slackspace.openkeepass.domain.zipper.GroupZipper;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnwriteableException;
import de.slackspace.openkeepass.parser.KeePassDatabaseXmlParser;
import de.slackspace.openkeepass.parser.SimpleXmlParser;
import de.slackspace.openkeepass.processor.EncryptionStrategy;
import de.slackspace.openkeepass.processor.ProtectedValueProcessor;
import de.slackspace.openkeepass.stream.HashedBlockOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

@Service
public class MakeKdbxByteService {

    public byte[] makeKdbx(KeePassFile keePassFile, String password) {
        try {
            if (!validateKeePassFile(keePassFile)) {
                throw new KeePassDatabaseUnwriteableException(
                        "The provided keePassFile is not valid. A valid keePassFile must contain of " +
                                "meta and root group and the root group must at least contain one group.");
            }

            KeePassHeader header = new KeePassHeader(new RandomGenerator());
            byte[] hashedPassword = this.hashPassword(password);
            byte[] keePassFilePayload = this.marshallXml(keePassFile, header);
            ByteArrayOutputStream streamToZip = this.compressStream(keePassFilePayload);
            ByteArrayOutputStream streamToHashBlock = this.hashBlockStream(streamToZip);
            ByteArrayOutputStream streamToEncrypt = this.combineHeaderAndContent(header, streamToHashBlock);
            byte[] encryptedDatabase = this.encryptStream(header, hashedPassword, streamToEncrypt);

            return encryptedDatabase;
        } catch (IOException e) {
            throw new KeePassDatabaseUnwriteableException("Could not write database file", e);
        }
    }

    // TO-DO: SHA256 ?
    private byte[] hashPassword(String password) throws UnsupportedEncodingException {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        return Sha256.hash(passwordBytes);
    }

    private byte[] encryptStream(KeePassHeader header, byte[] hashedPassword, ByteArrayOutputStream streamToEncrypt) throws IOException {
        CryptoInformation cryptoInformation = new CryptoInformation(12, header.getMasterSeed(), header.getTransformSeed(), header.getEncryptionIV(), header.getTransformRounds(), header.getHeaderSize());
        return (new Decrypter()).encryptDatabase(hashedPassword, cryptoInformation, streamToEncrypt.toByteArray());
    }

    private ByteArrayOutputStream combineHeaderAndContent(KeePassHeader header, ByteArrayOutputStream content) throws IOException {
        ByteArrayOutputStream streamToEncrypt = new ByteArrayOutputStream();
        streamToEncrypt.write(header.getBytes());
        streamToEncrypt.write(header.getStreamStartBytes());
        streamToEncrypt.write(content.toByteArray());
        return streamToEncrypt;
    }

    private ByteArrayOutputStream hashBlockStream(ByteArrayOutputStream streamToUnzip) throws IOException {
        ByteArrayOutputStream streamToHashBlock = new ByteArrayOutputStream();
        HashedBlockOutputStream hashBlockOutputStream = new HashedBlockOutputStream(streamToHashBlock);
        hashBlockOutputStream.write(streamToUnzip.toByteArray());
        hashBlockOutputStream.close();
        return streamToHashBlock;
    }

    private byte[] marshallXml(KeePassFile keePassFile, KeePassHeader header) {
        KeePassFile clonedKeePassFile = (new GroupZipper(keePassFile)).cloneKeePassFile();
        ProtectedStringCrypto protectedStringCrypto = Salsa20.createInstance(header.getProtectedStreamKey());
        (new ProtectedValueProcessor()).processProtectedValues(new EncryptionStrategy(protectedStringCrypto), clonedKeePassFile);
        return (new KeePassDatabaseXmlParser(new SimpleXmlParser())).toXml(keePassFile).toByteArray();
    }

    private ByteArrayOutputStream compressStream(byte[] keePassFilePayload) throws IOException {
        ByteArrayOutputStream streamToZip = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(streamToZip);
        gzipOutputStream.write(keePassFilePayload);
        gzipOutputStream.close();
        return streamToZip;
    }

    private static boolean validateKeePassFile(KeePassFile keePassFile) {
        if (keePassFile != null && keePassFile.getMeta() != null) {
            return keePassFile.getRoot() != null && !keePassFile.getRoot().getGroups().isEmpty();
        } else {
            return false;
        }
    }

}
