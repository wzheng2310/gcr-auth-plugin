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

import java.util.List;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotCredentials;

import org.acegisecurity.Authentication;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.security.ACL;

/**
 * This class automatically wraps existing GoogleRobotCredentials instances
 * into a username password credential type that is compatible with Docker
 * remote API client plugin like
 * {@link org.jenkinsci.plugins.dockerbuildstep.cmd.DockerCommand}.
 * In this way, a 'projecthosting'-scoped Oauth credential can be reused for
 * Google Container Registry access.
 */
@Extension
public class GoogleContainerRegistryCredentialProvider
    extends CredentialsProvider {

  /** {@inheritDoc} */
  @Override
  @NonNull
  public <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type,
      ItemGroup itemGroup, Authentication authentication,
      @NonNull List<DomainRequirement> domainRequirements) {
    if (!ACL.SYSTEM.equals(authentication)) {
      return ImmutableList.of();
    }

    // This provider only provides
    // {@link GoogleContainerRegistryCredential}
    // instances (and not, for example, the GoogleRobotCredentials we depend on,
    // which endangers an infinite loop).  We can stop fast if this Credential
    // type is not useful to the query.
    if (!type.isAssignableFrom(GoogleContainerRegistryCredential.class)) {
      return ImmutableList.of();
    }

    List<C> derived = Lists.newLinkedList();

    // GoogleContainerRegistryCredential can only be used from contexts
    // consistent with is access needs.  This is the first line of Domain access
    // protection; we won't even suggest these credentials for contexts where
    // it is not plausibly appropriate to.
    if (!GoogleContainerRegistryCredentialModule.matches(
        domainRequirements)) {
      return derived;
    }
    // The second line of Domain protection propagates the requirements when
    // looking up existing GoogleRobotCredentials to ensure we do not suggest
    // elevating an inappropriate GoogleRobotCredentials instance.
    Iterable<GoogleRobotCredentials> availableGoogleCredentials =
        lookupCredentials(GoogleRobotCredentials.class, itemGroup,
            ACL.SYSTEM, ImmutableList.<DomainRequirement>of(
                GoogleContainerRegistryCredentialModule.getScope())
        );

    for (GoogleRobotCredentials credentials : availableGoogleCredentials) {
      // Second line of Domain protection; we will not suggest a
      // the credential we create must have
      // its implementation constraints satisfied by the DomainRequirements
      // input (if any; empty lists create no tests).
      derived.add((C) new GoogleContainerRegistryCredential(
          credentials.getId(),
          new GoogleContainerRegistryCredentialModule()));
    }
    return derived;
  }

  /** {@inheritDoc} */
  @Override
  @NonNull
  public <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type,
      ItemGroup itemGroup, Authentication authentication) {
    return getCredentials(type, itemGroup, authentication,
        ImmutableList.<DomainRequirement>of());
  }
}
