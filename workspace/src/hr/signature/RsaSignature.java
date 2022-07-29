package hr.signature;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import hr.cache.Cache;
import hr.cache.CacheManager;
import hr.constant.CacheName;
import hr.util.FileUtil;

public class RsaSignature {
	private static final Logger log = Logger.getLogger(RsaSignature.class);
	
	public static PublicKey getPublicKey(String partnerKey) {
		String key = partnerKey + "PublicRequest";
		Cache signatureCache = (Cache) CacheManager.getInstance().getCache(CacheName.RSA_SIGNATURE_CACHE_NAME);
		if (signatureCache == null) {
			signatureCache = (Cache) CacheManager.getInstance().createCache(CacheName.RSA_SIGNATURE_CACHE_NAME);
		}
		PublicKey publicKey = (PublicKey) signatureCache.get(key);
		if (publicKey == null) {
			byte[] keyBytes = FileUtil.readFileByteArray(RsaSignature.class.getResource(key + ".key").getFile());
			try {
				X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64(keyBytes));
				KeyFactory kf = KeyFactory.getInstance("RSA");
				publicKey = kf.generatePublic(spec);
				signatureCache.put(key, publicKey);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				log.error(e);
			}
		}
		
		return publicKey;
	}
	
	public static PrivateKey getPrivateKey(String partnerKey) {
		String key = partnerKey + "PrivateResponse";
		Cache signatureCache = (Cache) CacheManager.getInstance().getCache(CacheName.RSA_SIGNATURE_CACHE_NAME);
		if (signatureCache == null) {
			signatureCache = (Cache) CacheManager.getInstance().createCache(CacheName.RSA_SIGNATURE_CACHE_NAME);
		}
		PrivateKey privateKey = (PrivateKey) signatureCache.get(key);
		if (privateKey == null) {
			byte[] keyBytes = FileUtil.readFileByteArray(RsaSignature.class.getResource(key + ".key").getFile());
			try {
				PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decodeBase64(keyBytes));			
				KeyFactory kf = KeyFactory.getInstance("RSA");
				privateKey = kf.generatePrivate(spec);
				signatureCache.put(key, privateKey);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				log.error(e);
			}
		}
		
		return privateKey;
	}
	
	public static boolean verify(String clearData, String cipherData, PublicKey publicKey) {
		boolean isSuccess = false;
		Signature signature;
		try {
			signature = Signature.getInstance("SHA512withRSA");
			signature.initVerify(publicKey);
			signature.update(clearData.getBytes("UTF-8"));
			byte[] cipherBytes = Base64.decodeBase64(cipherData.getBytes("UTF-8"));
			isSuccess = signature.verify(cipherBytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | UnsupportedEncodingException e) {
			log.error(e);
		}
		
		return isSuccess;
	}
	
	public static String sign(String clearData, PrivateKey privateKey) {
		String cipherText = "";
		try {
			Signature signature = Signature.getInstance("SHA512withRSA");
			signature.initSign(privateKey);
			signature.update(clearData.getBytes("UTF-8"));
			byte[] cipherBytes = signature.sign();
			cipherText = Base64.encodeBase64URLSafeString(cipherBytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | UnsupportedEncodingException e) {
			log.error(e);
		}
		
		return cipherText;
	}
}
