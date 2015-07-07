/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jenkins.plugins.googlecontainerregistryauth;

import java.util.Collections;
import java.util.List;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.allOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.firstOrNull;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import com.google.jenkins.plugins.credentials.oauth.GoogleOAuth2ScopeRequirement;
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotCredentials;
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotCredentialsModule;

import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.plugins.docker.commons.credentials.DockerRegistryToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import hudson.model.Item;
import hudson.util.Secret;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import jenkins.model.Jenkins;

/**
 * Test for {@link GoogleContainerRegistryTokenSource}
 */
public class GoogleContainerRegistryTokenSourceTest {
  private static final String EMAIL = "not@val.id";
  private static final String USERNAME = "gcr_user";
  private static final String GCR_USERNAME = "_token";
  private static final Secret SECRET = Secret.fromString("gcr_password");
  private static final String NAME = "gcr Account";
  private static final String CREDENTIALS_ID = "gcr-cred-id";

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  @NameWith(value = Namer.class, priority = 50)
  private abstract static class FakeGoogleRobotCredentials
      extends GoogleRobotCredentials {
    public FakeGoogleRobotCredentials(String a) {
      super(a, new GoogleRobotCredentialsModule());
    }
  }

  private static class FakeModule
      extends GoogleContainerRegistryCredentialModule {
    public FakeModule() {
    }

    @Override
    public String getIdentity(GoogleRobotCredentials credentials) {
      return USERNAME;
    }

    @Override
    public GoogleContainerRegistryCredentialModule forRemote(
        GoogleRobotCredentials credentials) {
      return new OtherFakeModule();
    }
  }

  private static class OtherFakeModule
      extends GoogleContainerRegistryCredentialModule {

    public OtherFakeModule() {
    }

    public String getIdentity(GoogleRobotCredentials credentials) {
      return USERNAME;
    }

    @Override
    public GoogleContainerRegistryCredentialModule forRemote(
        GoogleRobotCredentials credentials) {
      return this;
    }
  }

  /**
   */
  public static class Namer
      extends CredentialsNameProvider<FakeGoogleRobotCredentials> {
    public String getName(FakeGoogleRobotCredentials c) {
      return NAME;
    }
  }

  
  @Mock
  private FakeGoogleRobotCredentials credentials;

  @Mock
  private GoogleContainerRegistryCredential gcrCredential;
  private GoogleContainerRegistryTokenSource gcrTokenSource;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(credentials.getId()).thenReturn(CREDENTIALS_ID);
    when(credentials.getAccessToken(isA(GoogleOAuth2ScopeRequirement.class)))
        .thenReturn(SECRET);

    if (jenkins.jenkins != null) {
      SystemCredentialsProvider.getInstance().getCredentials().add(
          credentials);
    }
  }

  @Test
  public void testConvert() throws Exception {
    List<DomainRequirement> requirements = Collections.emptyList();
    requirements = Collections.<DomainRequirement>singletonList(
        new HostnameRequirement("gcr.io"));

    DockerRegistryToken token = AuthenticationTokens.convert(
        DockerRegistryToken.class, firstOrNull(
            CredentialsProvider.lookupCredentials(
                IdCredentials.class, (Item) null,
                Jenkins.getAuthentication(), requirements),
            allOf(AuthenticationTokens.matcher(DockerRegistryToken.class),
                withId("gcr:" + CREDENTIALS_ID))));
    assertEquals(EMAIL, token.getEmail());
    String encoded = Base64.encodeBase64String(
        (GCR_USERNAME + ":" + SECRET.getPlainText()).getBytes(Charsets.UTF_8));
    assertEquals(encoded, token.getToken());
  }
}

