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

import javax.annotation.Nullable;

import com.google.api.client.util.Strings;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

import net.sf.json.JSONObject;


/**
 * Provides global configuration for this plgin.
 */
@Extension
public class GoogleContainerRegistryCredentialGlobalConfig
    extends GlobalConfiguration {
  private static final String GCR_SERVER = "gcr.io,*.gcr.io";

  public GoogleContainerRegistryCredentialGlobalConfig() {
    oldDescriptor = new GoogleContainerRegistryCredential.DescriptorImpl();
    if (Strings.isNullOrEmpty(oldDescriptor.getGcrServer())) {
      load();
    } else {
      gcrServer = oldDescriptor.getGcrServer();
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getDisplayName() {
    return Messages.
        GoogleContainerRegistryCredential_GlobalDisplayName();
  }

  /** {@inheritDoc} */
  @Override
  public boolean configure(StaplerRequest req, JSONObject json)
      throws FormException {
    json = json.getJSONObject(getDisplayName());
    gcrServer = json.has("gcrServer") ?
        json.getString("gcrServer") : null;
    save();
    oldDescriptor.deleteConfigFile();
    return true;
  }

  /**
   * Retrieve the Google Registry Container server URL.
   */
  @Nullable public String getGcrServer() {
    return Strings.isNullOrEmpty(gcrServer) ? GCR_SERVER : gcrServer;
  }

  private String gcrServer = null;
  GoogleContainerRegistryCredential.DescriptorImpl oldDescriptor;
}
