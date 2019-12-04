package com.danx.fcdemo.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Sample Organization Representation
 *
 * Keeps track which resources are defined for the Organization it represents.
 *
 */
public class SampleOrg {
    final String name;
    final String mspid;
    HFCAClient caClient;

    Map<String, User> userMap = new HashMap<>();
    Map<String, String> peerLocations = new HashMap<>();
    Map<String, String> ordererLocations = new HashMap<>();

    private SampleUser admin;
    private String caLocation;
    private Properties caProperties = null;

    private SampleUser peerAdmin;


    private String domainName;

    public String getCAName() {
        return caName;
    }

    private String caName;

    public SampleOrg(final String name, final String mspid) {
        this.name = name;
        this.mspid = mspid;
    }

    public SampleUser getAdmin() {
        return admin;
    }

    public void setAdmin(final SampleUser admin) {
        this.admin = admin;
    }

    public String getMSPID() {
        return mspid;
    }

    public String getCALocation() {
        return this.caLocation;
    }

    public void setCALocation(final String caLocation) {
        this.caLocation = caLocation;
    }

    public void addPeerLocation(final String name, final String location) {

        peerLocations.put(name, location);
    }

    public void addOrdererLocation(final String name, final String location) {

        ordererLocations.put(name, location);
    }


    public String getPeerLocation(final String name) {
        return peerLocations.get(name);

    }

    public String getOrdererLocation(final String name) {
        return ordererLocations.get(name);

    }


    public Set<String> getPeerNames() {

        return Collections.unmodifiableSet(peerLocations.keySet());
    }


    public Set<String> getOrdererNames() {

        return Collections.unmodifiableSet(ordererLocations.keySet());
    }

    public HFCAClient getCAClient() {

        return caClient;
    }

    public void setCAClient(final HFCAClient caClient) {

        this.caClient = caClient;
    }

    public String getName() {
        return name;
    }

    public void addUser(final SampleUser user) {
        userMap.put(user.getName(), user);
    }

    public User getUser(final String name) {
        return userMap.get(name);
    }

    public Collection<String> getOrdererLocations() {
        return Collections.unmodifiableCollection(ordererLocations.values());
    }


    public void setCAProperties(final Properties caProperties) {
        this.caProperties = caProperties;
    }

    public Properties getCAProperties() {
        return caProperties;
    }


    public SampleUser getPeerAdmin() {
        return peerAdmin;
    }

    public void setPeerAdmin(final SampleUser peerAdmin) {
        this.peerAdmin = peerAdmin;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setCAName(final String caName) {
        this.caName = caName;
    }
}
