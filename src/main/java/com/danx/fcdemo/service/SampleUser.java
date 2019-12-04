/*
 *  Copyright 2016 DTCC, Fujitsu Australia Software Technology - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.danx.fcdemo.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

import io.netty.util.internal.StringUtil;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;


public class SampleUser implements User, Serializable {
    private static final long serialVersionUID = 8077132186383604355L;

    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private String organization;
    private String enrollmentSecret;
    Enrollment enrollment = null; //need access in test env.

    private transient SampleStore keyValStore;
    private final String keyValStoreName;

    private transient CryptoSuite cryptoSuite;

    public SampleUser(final String name, final String org, final SampleStore fs, final CryptoSuite cryptoSuite) {
        this.name = name;
        this.cryptoSuite = cryptoSuite;

        this.keyValStore = fs;
        this.organization = org;
        this.keyValStoreName = toKeyValStoreName(this.name, org);
        final String memberStr = keyValStore.getValue(keyValStoreName);
        if (null == memberStr) {
            saveState();
        } else {
            restoreState();
        }

    }

    static boolean isStored(final String name, final String org, final SampleStore fs) {

        return fs.hasValue(toKeyValStoreName(name, org));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    public void setRoles(final Set<String> roles) {

        this.roles = roles;
        saveState();
    }

    @Override
    public String getAccount() {
        return this.account;
    }

    /**
     * Set the account.
     *
     * @param account The account.
     */
    public void setAccount(final String account) {

        this.account = account;
        saveState();
    }

    @Override
    public String getAffiliation() {
        return this.affiliation;
    }

    /**
     * Set the affiliation.
     *
     * @param affiliation the affiliation.
     */
    public void setAffiliation(final String affiliation) {
        this.affiliation = affiliation;
        saveState();
    }

    @Override
    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    /**
     * Determine if this name has been registered.
     *
     * @return {@code true} if registered; otherwise {@code false}.
     */
    public boolean isRegistered() {
        return !StringUtil.isNullOrEmpty(enrollmentSecret);
    }

    /**
     * Determine if this name has been enrolled.
     *
     * @return {@code true} if enrolled; otherwise {@code false}.
     */
    public boolean isEnrolled() {
        return this.enrollment != null;
    }

    /**
     * Save the state of this user to the key value store.
     */
    void saveState() {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            keyValStore.setValue(keyValStoreName, Hex.toHexString(bos.toByteArray()));
            bos.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restore the state of this user from the key value store (if found).  If not found, do nothing.
     */
    SampleUser restoreState() {
        final String memberStr = keyValStore.getValue(keyValStoreName);
        if (null != memberStr) {
            // The user was found in the key value store, so restore the
            // state.
            final byte[] serialized = Hex.decode(memberStr);
            final ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
            try {
                final ObjectInputStream ois = new ObjectInputStream(bis);
                final SampleUser state = (SampleUser) ois.readObject();
                if (state != null) {
                    this.name = state.name;
                    this.roles = state.roles;
                    this.account = state.account;
                    this.affiliation = state.affiliation;
                    this.organization = state.organization;
                    this.enrollmentSecret = state.enrollmentSecret;
                    this.enrollment = state.enrollment;
                    this.mspId = state.mspId;
                    return this;
                }
            } catch (final Exception e) {
                throw new RuntimeException(String.format("Could not restore state of member %s", this.name), e);
            }
        }
        return null;
    }

    public String getEnrollmentSecret() {
        return enrollmentSecret;
    }

    public void setEnrollmentSecret(final String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;
        saveState();
    }

    public void setEnrollment(final Enrollment enrollment) {

        this.enrollment = enrollment;
        saveState();

    }

    public void setIdemixEnrollment(final Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public static String toKeyValStoreName(final String name, final String org) {
        return "user." + name + org;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    String mspId;

    public void setMspId(final String mspID) {
        this.mspId = mspID;
        saveState();

    }
}
