package com.hiro.goat.core.postal;

import com.hiro.goat.api.signature.Signable;
import com.hiro.goat.api.signature.Signer;
import com.hiro.goat.api.signature.Verifier;

import lombok.NonNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Tool to build signature for signable objects
 * 1. Using HmacSHA256 to build Mac
 * 2. Unique secret is required in use
 * 3. Put Mac in thread local to prevent different threads using the same Mac
 */
public class HmacSha256Signer implements Signer, Verifier {

    /**
     * Mac holder for threads
     */
    private final ThreadLocal<Mac> macHolder;

    /**
     * Constructor
     *
     * @param secret recommend length 64 bytes. Minimum 32 bytes suggested.
     */
    public HmacSha256Signer(@NonNull final String secret) {
        final SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.macHolder = ThreadLocal.withInitial(() -> {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(keySpec);
                return mac;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Generate signature and set in signable
     *
     * @param signable Signable Implementation
     */
    @Override
    public void sign(@NonNull final Signable signable) {
        signable.setSignature(generateSignature(signable));
    }

    /**
     * @param signable Signable Implementation
     *
     * @return true if verify success
     */
    @Override
    public boolean verify(@NonNull final Signable signable) {
        return generateSignature(signable).equals(signable.getSignature());
    }

    /**
     * Generate Base64 signature
     *
     * @param signable Signable Implementation
     *
     * @return Base64 String
     */
    private String generateSignature(Signable signable) {
        try {
            Mac mac = macHolder.get();
            mac.reset();
            byte[] result = mac.doFinal(signable.signableData().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
