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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.jenkinsci.plugins.docker.commons.credentials.DockerRegistryToken;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;

/**
 * Token source class that converts Google Container Registry credentials
 * to {@code DockerRegistryToken}
 */
@Extension
public class GoogleContainerRegistryTokenSource extends
    AuthenticationTokenSource<DockerRegistryToken,
    GoogleContainerRegistryCredential> {
  public GoogleContainerRegistryTokenSource() {
    super(DockerRegistryToken.class, GoogleContainerRegistryCredential.class);
}

  /** {@inheritDoc} */
  @NonNull
  @Override
  public DockerRegistryToken convert(
      GoogleContainerRegistryCredential credential)
      throws AuthenticationTokenException {
    return new DockerRegistryToken(credential.getEmail(),
        Base64.encodeBase64String((credential.getUsername() + ":" +
            credential.getPassword().getPlainText()).getBytes(Charsets.UTF_8)));
  }
}
