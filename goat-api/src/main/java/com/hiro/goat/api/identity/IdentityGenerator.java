package com.hiro.goat.api.identity;

/**
 * Generator of ID with long type
 */
public interface IdentityGenerator {

    /**
     * Get next ID
     *
     * @return int 64
     */
    long nextId();

}
