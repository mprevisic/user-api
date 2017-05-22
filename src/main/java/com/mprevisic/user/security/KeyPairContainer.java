package com.mprevisic.user.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mprevisic.user.domain.KeyEntity;
import com.mprevisic.user.repository.KeyRepository;

/**
 * Container for RSA encryption keys used for signing the JWT token contents.
 * Generates an initial key pair and persists it in the database for further
 * usage.
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Component
public class KeyPairContainer {
	
	private final Logger LOG = LoggerFactory.getLogger(this.getClass()); 

	private RSAPublicKey publicKey;

	private RSAPrivateKey privateKey;

	private static final String PRIVATE_KEY_NAME = "private";

	private static final String PUBLIC_KEY_NAME = "public";

	private final KeyRepository keyRepo;

	/**
	 * Constructs the container and initializes it. If no keys are already in
	 * DB, generates new key pair and stores it.
	 */
	@Autowired
	public KeyPairContainer(KeyRepository keyRepo) {
		LOG.info("Initializing RSA key-pair container");
		
		this.keyRepo = keyRepo;

		try {
			KeyPair kp = getKeyPairFromDb();

			if (kp == null) {
				KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
				keyGenerator.initialize(1024);
				kp = keyGenerator.genKeyPair();

				KeyEntity privKey = new KeyEntity();
				privKey.setName(PRIVATE_KEY_NAME);
				privKey.setValue(kp.getPrivate().getEncoded());

				KeyEntity pubKey = new KeyEntity();
				pubKey.setName(PUBLIC_KEY_NAME);
				pubKey.setValue(kp.getPublic().getEncoded());

				keyRepo.save(privKey);
				keyRepo.save(pubKey);
			}

			publicKey = (RSAPublicKey) kp.getPublic();
			privateKey = (RSAPrivateKey) kp.getPrivate();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns key pair from DB (if exists)
	 */
	private KeyPair getKeyPairFromDb() throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyEntity privKey = keyRepo.findByName(PRIVATE_KEY_NAME);
		KeyEntity pubKey = keyRepo.findByName(PUBLIC_KEY_NAME);

		if (privKey == null || pubKey == null) {
			return null;
		}

		KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privKey.getValue());
		RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeyFac.generatePrivate(encodedKeySpec);

		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKey.getValue());
		RSAPublicKey publicKey = (RSAPublicKey) rsaKeyFac.generatePublic(keySpec);

		KeyPair keyPair = new KeyPair(publicKey, privateKey);

		return keyPair;
	}

	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(RSAPublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(RSAPrivateKey privateKey) {
		this.privateKey = privateKey;
	}

}