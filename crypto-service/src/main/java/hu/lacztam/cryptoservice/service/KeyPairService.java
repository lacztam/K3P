package hu.lacztam.cryptoservice.service;

import hu.lacztam.cryptoservice.model.PublicPrivateKeyPair;
import hu.lacztam.cryptoservice.repository.KeyPairRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Service
@AllArgsConstructor
public class KeyPairService {

    private final KeyPairRepository keyPairRepository;

    public PublicPrivateKeyPair save(PublicPrivateKeyPair keyPair){
        if(keyPair != null)
            return keyPairRepository.save(keyPair);
        else
            throw new NullPointerException();
    }

    public void delete(PublicPrivateKeyPair keyPair){
        keyPairRepository.delete(keyPair);
    }

    public PublicPrivateKeyPair findByUserEmail(String email){
        Optional<PublicPrivateKeyPair> keyPairOptional = keyPairRepository.findByUserEmail(email);
        if(keyPairOptional.isPresent()){
            return keyPairOptional.get();
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public boolean isKeyPairExist(String email){
        Optional<PublicPrivateKeyPair> keyPairOptional = keyPairRepository.findByUserEmail(email);

        return keyPairOptional.isPresent() ? true : false;
    }

    public PublicPrivateKeyPair generateKeyPairAndSave() {

        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        String pubKey = new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
        String priKey = new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()));

        PublicPrivateKeyPair publicPrivateKeyPair = new PublicPrivateKeyPair();
        publicPrivateKeyPair.setPublicKey(pubKey);
        publicPrivateKeyPair.setPrivateKey(priKey);

        return publicPrivateKeyPair;
    }

    public String getEncrypted(String data, String Key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {
        if(Key == null)
            throw new NullPointerException("Key can not be null.");

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(Key.getBytes())));
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedbytes = cipher.doFinal(data.getBytes());
        return new String(Base64.getEncoder().encode(encryptedbytes));
    }

    public String getDecrypted(String data, String Key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if(Key == null)
            throw new NullPointerException("Key can not be null.");

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PrivateKey pk = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(Key.getBytes())));
        cipher.init(Cipher.DECRYPT_MODE, pk);
        byte[] encryptedbytes = cipher.doFinal(Base64.getDecoder().decode(data.getBytes()));
        return new String(encryptedbytes);
    }

}
