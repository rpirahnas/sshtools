/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */

package com.sshtools.common.configuration;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.automate.AutomationConfiguration;
import com.sshtools.j2ssh.configuration.ConfigurationContext;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.18 $
 */
public class XmlConfigurationContext
    implements ConfigurationContext {
  private static Log log = LogFactory.getLog(XmlConfigurationContext.class);
  HashMap configurations = new HashMap();
  String automationResource = "automation.xml";
  private boolean failOnError = false;

  /**
   * Creates a new XmlConfigurationContext object.
   */
  public XmlConfigurationContext() {
  }


  /**
   *
   *
   * @param automationResource
   */
  public void setAutomationConfigurationResource(String automationResource) {
    this.automationResource = automationResource;
  }

  /**
   *
   *
   * @param failOnError
   */
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  /**
   *
   *
   * @throws ConfigurationException
   */
  public void initialize() throws ConfigurationException {

    if (automationResource != null) {
      try {
        AutomationConfiguration y = new AutomationConfiguration(
            ConfigurationLoader
            .loadFile(automationResource));
        configurations.put(com.sshtools.common.automate.AutomationConfiguration.class,
                           y);
      }
      catch (Exception ex) {
        if (failOnError) {
          throw new ConfigurationException(ex.getMessage());
        }
        else {
          log.info(automationResource + " could not be found: "
                   + ex.getMessage());
        }
      }
    }
  }

  /**
   *
   *
   * @param cls
   *
   * @return
   */
  public boolean isConfigurationAvailable(Class cls) {
    return configurations.containsKey(cls);
  }

  /**
   *
   *
   * @param cls
   *
   * @return
   *
   * @throws ConfigurationException
   */
  public Object getConfiguration(Class cls) throws ConfigurationException {
    if (configurations.containsKey(cls)) {
      return configurations.get(cls);
    }
    else {
      throw new ConfigurationException(cls.getName()
                                       + " configuration not available");
    }
  }
}
