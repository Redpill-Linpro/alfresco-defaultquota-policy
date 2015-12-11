/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.extension.quota;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * @author Jared Ottley (jared.ottley@alfresco.com)
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class DefaultQuotaPolicy implements OnCreateNodePolicy {

  private Logger LOG = Logger.getLogger(DefaultQuotaPolicy.class);

  // default quota for newly created users
  private String _defaultQuota;

  private Behaviour _onCreateNode;

  private PolicyComponent _policyComponent;
  private ContentUsageService _contentUsageService;
  private NodeService _nodeService;

  public void setPolicyComponent(PolicyComponent policyComponent) {
    _policyComponent = policyComponent;
  }

  public void setContentUsageService(ContentUsageService contentUsageService) {
    _contentUsageService = contentUsageService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setDefaultQuota(String defaultQuota) {
    _defaultQuota = defaultQuota;
  }

  public void init() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Default Quota will be set to: " + _defaultQuota + " bytes.");
    }

    _onCreateNode = new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT);

    _policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"), ContentModel.TYPE_PERSON, _onCreateNode);
  }

  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {

    // The person node
    final NodeRef user = childAssocRef.getChildRef();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting user " + _nodeService.getProperty(user, ContentModel.PROP_USERNAME) + "'s quota to " + _defaultQuota);
    }

    // We only want to set a default for users when a quota has not been set
    long currentQuota = _contentUsageService.getUserQuota((String) _nodeService.getProperty(user, ContentModel.PROP_USERNAME));

    // No quota is -1
    if (currentQuota < 0) {
      // Quota can only be set by an admin
      AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
        public Object doWork() throws Exception {

          _contentUsageService.setUserQuota((String) _nodeService.getProperty(user, ContentModel.PROP_USERNAME), Long.parseLong(_defaultQuota));

          return user;
        }
      }, "admin");
    }
  }
}