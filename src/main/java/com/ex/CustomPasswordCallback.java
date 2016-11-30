package com.ex;

import eu.europa.esig.dss.token.PasswordInputCallback;

/**
 * Created by b.balukiewicz on 30.11.2016.
 */
public class CustomPasswordCallback implements PasswordInputCallback {
    @Override
    public char[] getPassword() {
        return "test".toCharArray();
    }
}
