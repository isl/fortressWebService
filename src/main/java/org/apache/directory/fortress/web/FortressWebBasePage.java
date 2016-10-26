/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.fortress.web;


import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import net.sf.ehcache.search.aggregator.Sum;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.fortress.core.SecurityException;
import org.apache.directory.fortress.realm.J2eePolicyMgr;
import org.apache.directory.fortress.web.common.*;
import org.apache.directory.fortress.web.control.SecUtils;
import org.apache.directory.fortress.web.control.SecureBookmarkablePageLink;
import org.apache.directory.fortress.web.control.WicketSession;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.directory.fortress.core.*;
import org.apache.directory.fortress.core.model.Session;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;


/**
 * Base class for Commander Web.  This class initializes Fortress RBAC context and so contains a synchronized block.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public abstract class FortressWebBasePage extends WebPage
{
    /** Default serialVersionUID */
    private static final long serialVersionUID = 1L;
    @SpringBean
    private AccessMgr accessMgr;
    @SpringBean
    private DelAccessMgr delAccessMgr;
    @SpringBean
    private J2eePolicyMgr j2eePolicyMgr;
    private static final String CLS_NM = FortressWebBasePage.class.getName();
    private static final Logger LOG = Logger.getLogger( CLS_NM );

    // Navigation Timing variables in favor of getting the Response Times by using timing.js client library on each of the Pages
    static final String PARAM_CONNECT_END_NAME = "window.performance.timing.connectEnd";
    static final String PARAM_FETCH_START_NAME = "window.performance.timing.fetchStart";
    static final String PARAM_RESPONSE_END_NAME = "window.performance.timing.responseEnd";
    static final String PARAM_RESPONSE_START_NAME = "window.performance.timing.responseStart";
    static final String PARAM_LOAD_EVENT_END_NAME = "window.performance.timing.loadEventEnd";
    static final String PARAM_DOM_LOADING_NAME = "window.performance.timing.domLoading";


    // metric for getting the total number of requests
    static final Counter requestTotalCount = Counter.build()
            .name("request_messages_total")
            .help("Request Messages Total").register();



    static final Summary userNetworkLatency = Summary.build()
            .name("userNetworkLatency_latency_milliseconds")
            .help("User Network Latency milliseconds")
            .register();
    static final Summary userRequestCompletionTime = Summary.build()
            .name("userRequestCompletionTime_latency_milliseconds")
            .help("User Request Completion Time latency milliseconds")
            .register();

    static final Summary roleNetworkLatency = Summary.build()
            .name("roleNetworkLatency_latency_milliseconds")
            .help("Role Network Latency milliseconds")
            .register();
    static final Summary roleRequestCompletionTime = Summary.build()
            .name("roleRequestCompletionTime_latency_milliseconds")
            .help("Role Request Completion Time latency milliseconds")
            .register();

    static final Summary sdStaticNetworkLatency = Summary.build()
            .name("sdStaticNetworkLatency_latency_milliseconds")
            .help("SdStatic Network Latency milliseconds")
            .register();
    static final Summary sdStaticRequestCompletionTime = Summary.build()
            .name("sdStaticRequestCompletionTime_latency_milliseconds")
            .help("SdStatic Request Completion Time latency milliseconds")
            .register();

    static final Summary sdDynamicNetworkLatency = Summary.build()
            .name("sdDynamicNetworkLatency_latency_milliseconds")
            .help("SdDynamic Network Latency milliseconds")
            .register();
    static final Summary sdDynamicRequestCompletionTime = Summary.build()
            .name("sdDynamicRequestCompletionTime_latency_milliseconds")
            .help("SdDynamic Request Completion Time latency milliseconds")
            .register();

    static final Summary roleAdminNetworkLatency = Summary.build()
            .name("roleAdminNetworkLatency_latency_milliseconds")
            .help("RoleAdmin Network Latency milliseconds")
            .register();
    static final Summary roleAdminRequestCompletionTime = Summary.build()
            .name("roleAdminRequestCompletionTime_latency_milliseconds")
            .help("RoleAdmin Request Completion Time latency milliseconds")
            .register();

    static final Summary permNetworkLatency = Summary.build()
            .name("permNetworkLatency_latency_milliseconds")
            .help("Perm Network Latency milliseconds")
            .register();
    static final Summary permRequestCompletionTime = Summary.build()
            .name("permRequestCompletionTime_latency_milliseconds")
            .help("Perm Request Completion Time latency milliseconds")
            .register();

    static final Summary permAdminNetworkLatency = Summary.build()
            .name("permAdminNetworkLatency_latency_milliseconds")
            .help("Perm Admin Network Latency milliseconds")
            .register();
    static final Summary permAdminRequestCompletionTime = Summary.build()
            .name("permAdminRequestCompletionTime_latency_milliseconds")
            .help("Perm Admin Request Completion Time latency milliseconds")
            .register();

    static final Summary ouUserNetworkLatency = Summary.build()
            .name("ouUserNetworkLatency_latency_milliseconds")
            .help("OuUser Network Latency milliseconds")
            .register();
    static final Summary ouUserRequestCompletionTime = Summary.build()
            .name("ouUserRequestCompletionTime_latency_milliseconds")
            .help("OuUser Request Completion Time latency milliseconds")
            .register();

    static final Summary ouPermNetworkLatency = Summary.build()
            .name("ouPermNetworkLatency_latency_milliseconds")
            .help("OuPerm Network Latency milliseconds")
            .register();
    static final Summary ouPermRequestCompletionTime = Summary.build()
            .name("ouPermRequestCompletionTime_latency_milliseconds")
            .help("OuPerm Request Completion Time latency milliseconds")
            .register();

    static final Summary objectNetworkLatency = Summary.build()
            .name("objectNetworkLatency_latency_milliseconds")
            .help("Object Network Latency milliseconds")
            .register();
    static final Summary objectRequestCompletionTime = Summary.build()
            .name("objectRequestCompletionTime_latency_milliseconds")
            .help("Object Request Completion Time latency milliseconds")
            .register();

    static final Summary objectAdminNetworkLatency = Summary.build()
            .name("objectAdminNetworkLatency_latency_milliseconds")
            .help("Object Admin Network Latency milliseconds")
            .register();
    static final Summary objectAdminRequestCompletionTime = Summary.build()
            .name("objectAdminRequestCompletionTime_latency_milliseconds")
            .help("Object Admin Request Completion Time latency milliseconds")
            .register();




    // Process and Delay Time in favor of Execution Time calculation of Service Pages
    static final Summary userProcessTimeLatency = Summary.build()
            .name("userProcess_latency_milliseconds")
            .help("User Process Time milliseconds")
            .register();
    static final Summary userDelayTimeLatency = Summary.build()
            .name("userDelay_latency_milliseconds")
            .help("User Delay Time milliseconds")
            .register();
    static final Summary userAnswerDelayTimeLatency = Summary.build()
            .name("userAnswerDelay_latency_milliseconds")
            .help("User Answer Delay Time milliseconds")
            .register();


    static final Summary sdStaticProcessTimeLatency = Summary.build()
            .name("sdStaticProcess_latency_milliseconds")
            .help("sdStatic Process Time milliseconds")
            .register();
    static final Summary sdStaticDelayTimeLatency = Summary.build()
            .name("sdStaticDelay_latency_milliseconds")
            .help("sdStatic Delay Time milliseconds")
            .register();
    static final Summary sdStaticAnswerDelayTimeLatency = Summary.build()
            .name("sdStaticAnswerDelay_latency_milliseconds")
            .help("sdStatic Answer Delay Time milliseconds")
            .register();


    static final Summary sdDynamicProcessTimeLatency = Summary.build()
            .name("sdDynamicProcess_latency_milliseconds")
            .help("sdDynamic Process Time milliseconds")
            .register();
    static final Summary sdDynamicDelayTimeLatency = Summary.build()
            .name("sdDynamicDelay_latency_milliseconds")
            .help("sdDynamic Delay Time milliseconds")
            .register();
    static final Summary sdDynamicAnswerDelayTimeLatency = Summary.build()
            .name("sdDynamicAnswerDelay_latency_milliseconds")
            .help("sdDynamic Answer Delay Time milliseconds")
            .register();


    static final Summary roleProcessTimeLatency = Summary.build()
            .name("roleProcess_latency_milliseconds")
            .help("Role Process Time milliseconds")
            .register();
    static final Summary roleDelayTimeLatency = Summary.build()
            .name("roleDelay_latency_milliseconds")
            .help("Role Delay Time milliseconds")
            .register();
    static final Summary roleAnswerDelayTimeLatency = Summary.build()
            .name("roleAnswerDelay_latency_milliseconds")
            .help("Role Answer Delay Time milliseconds")
            .register();



    static final Summary roleAdminProcessTimeLatency = Summary.build()
            .name("roleAdminProcess_latency_milliseconds")
            .help("RoleAdmin Process Time milliseconds")
            .register();
    static final Summary roleAdminDelayTimeLatency = Summary.build()
            .name("roleAdminDelay_latency_milliseconds")
            .help("RoleAdmin Delay Time milliseconds")
            .register();
    static final Summary roleAdminAnswerDelayTimeLatency = Summary.build()
            .name("roleAdminAnswerDelay_latency_milliseconds")
            .help("Role Admin Answer Delay Time milliseconds")
            .register();



    static final Summary permProcessTimeLatency = Summary.build()
            .name("permProcess_latency_milliseconds")
            .help("Perm Process Time milliseconds")
            .register();
    static final Summary permDelayTimeLatency = Summary.build()
            .name("permDelay_latency_milliseconds")
            .help("Perm Delay Time milliseconds")
            .register();
    static final Summary permAnswerDelayTimeLatency = Summary.build()
            .name("permAnswerDelay_latency_milliseconds")
            .help("perm Answer Delay Time milliseconds")
            .register();



    static final Summary permAdminProcessTimeLatency = Summary.build()
            .name("permAdminProcess_latency_milliseconds")
            .help("PermAdmin Process Time milliseconds")
            .register();
    static final Summary permAdminDelayTimeLatency = Summary.build()
            .name("permAdminDelay_latency_milliseconds")
            .help("PermAdmin Delay Time milliseconds")
            .register();
    static final Summary permAdminAnswerDelayTimeLatency = Summary.build()
            .name("permAdminAnswerDelay_latency_milliseconds")
            .help("perm Admin Answer Delay Time milliseconds")
            .register();


    static final Summary ouUserProcessTimeLatency = Summary.build()
            .name("ouUserProcess_latency_milliseconds")
            .help("OuUser Process Time milliseconds")
            .register();
    static final Summary ouUserDelayTimeLatency = Summary.build()
            .name("ouUserDelay_latency_milliseconds")
            .help("OuUser Delay Time milliseconds")
            .register();
    static final Summary ouUserAnswerDelayTimeLatency = Summary.build()
            .name("ouUserAnswerDelay_latency_milliseconds")
            .help("ouUser Answer Delay Time milliseconds")
            .register();



    static final Summary ouPermProcessTimeLatency = Summary.build()
            .name("ouPermProcess_latency_milliseconds")
            .help("OuPerm Process Time milliseconds")
            .register();
    static final Summary ouPermDelayTimeLatency = Summary.build()
            .name("ouPermDelay_latency_milliseconds")
            .help("OuPerm Delay Time milliseconds")
            .register();
    static final Summary ouPermAnswerDelayTimeLatency = Summary.build()
            .name("ouPermAnswerDelay_latency_milliseconds")
            .help("ouPerm Answer Delay Time milliseconds")
            .register();




    static final Summary objectProcessTimeLatency = Summary.build()
            .name("objectProcess_latency_milliseconds")
            .help("Object Process Time milliseconds")
            .register();
    static final Summary objectDelayTimeLatency = Summary.build()
            .name("objectDelay_latency_milliseconds")
            .help("Object Delay Time milliseconds")
            .register();
    static final Summary objectAnswerDelayTimeLatency = Summary.build()
            .name("objectAnswerDelay_latency_milliseconds")
            .help("object Answer Delay Time milliseconds")
            .register();




    static final Summary objectAdminProcessTimeLatency = Summary.build()
            .name("objectAdminProcess_latency_milliseconds")
            .help("Object Admin Process Time milliseconds")
            .register();
    static final Summary objectAdminDelayTimeLatency = Summary.build()
            .name("objectAdminDelay_latency_milliseconds")
            .help("Object Admin Delay Time milliseconds")
            .register();
    static final Summary objectAdminAnswerDelayTimeLatency = Summary.build()
            .name("objectAdminAnswerDelay_latency_milliseconds")
            .help("object Admin Answer Delay Time milliseconds")
            .register();

    //Successability and accessability metrics
    static final Counter userResponseNumber = Counter.build()
            .name("user_response_total")
            .help("User Response Total").register();
    static final Counter roleResponseNumber = Counter.build()
            .name("role_response_total")
            .help("Role Response Total").register();
    static final Counter objectResponseNumber = Counter.build()
            .name("object_response_total")
            .help("Object Response Total").register();
    static final Counter permResponseNumber = Counter.build()
            .name("perm_response_total")
            .help("Perm Response Total").register();
    static final Counter sdStaticResponseNumber = Counter.build()
            .name("sdStatic_response_total")
            .help("SdStatic Response Total").register();
    static final Counter sdDynamicResponseNumber = Counter.build()
            .name("sdDynamic_response_total")
            .help("SdDynamic Response Total").register();
    static final Counter ouUserResponseNumber = Counter.build()
            .name("ouUser_response_total")
            .help("OuUser Response Total").register();
    static final Counter ouPermResponseNumber = Counter.build()
            .name("ouPerm_response_total")
            .help("OuPerm Response Total").register();
    static final Counter roleAdminResponseNumber = Counter.build()
            .name("roleAdmin_response_total")
            .help("RoleAdmin Response Total").register();
    static final Counter objectAdminResponseNumber = Counter.build()
            .name("objectAdmin_response_total")
            .help("ObjectAdmin Response Total").register();
    static final Counter permAdminResponseNumber = Counter.build()
            .name("permAdmin_response_total")
            .help("PermAdmin Response Total").register();
    static final Counter logOutResponseNumber = Counter.build()
            .name("logOut_response_total")
            .help("LogOut Response Total").register();
    static final Counter logInResponseNumber = Counter.build()
            .name("logIn_response_total")
            .help("LogIn Response Total").register();
    static final Counter launchResponseNumber = Counter.build()
            .name("launch_response_total")
            .help("Launch Response Total").register();
    static final Counter errorPageResponseNumber = Counter.build()
            .name("error_response_total")
            .help("Error Response Total").register();


    static final Counter userAcknowledgeNumber = Counter.build()
            .name("user_acknowledge_total")
            .help("User Acknowledge Total").register();
    static final Counter roleAcknowledgeNumber = Counter.build()
            .name("role_acknowledge_total")
            .help("Role Acknowledge Total").register();
    static final Counter objectAcknowledgeNumber = Counter.build()
            .name("object_acknowledge_total")
            .help("Object Acknowledge Total").register();
    static final Counter permAcknowledgeNumber = Counter.build()
            .name("perm_acknowledge_total")
            .help("Perm Acknowledge Total").register();
    static final Counter sdStaticAcknowledgeNumber = Counter.build()
            .name("sdStatic_acknowledge_total")
            .help("SdStatic Acknowledge Total").register();
    static final Counter sdDynamicAcknowledgeNumber = Counter.build()
            .name("sdDynamic_acknowledge_total")
            .help("SdDynamic Acknowledge Total").register();
    static final Counter ouUserAcknowledgeNumber = Counter.build()
            .name("ouUser_acknowledge_total")
            .help("OuUser Acknowledge Total").register();
    static final Counter ouPermAcknowledgeNumber = Counter.build()
            .name("ouPerm_acknowledge_total")
            .help("OuPerm Acknowledge Total").register();
    static final Counter roleAdminAcknowledgeNumber = Counter.build()
            .name("roleAdmin_acknowledge_total")
            .help("RoleAdmin Acknowledge Total").register();
    static final Counter objectAdminAcknowledgeNumber = Counter.build()
            .name("objectAdmin_acknowledge_total")
            .help("ObjectAdmin Acknowledge Total").register();
    static final Counter permAdminAcknowledgeNumber = Counter.build()
            .name("permAdmin_acknowledge_total")
            .help("PermAdmin Acknowledge Total").register();


    //Throughput metrics
    static final Counter userRequestNumber = Counter.build()
            .name("user_request_total")
            .help("User Request Total").register();
    static final Counter roleRequestNumber = Counter.build()
            .name("role_request_total")
            .help("Role Request Total").register();
    static final Counter objectRequestNumber = Counter.build()
            .name("object_request_total")
            .help("Object Request Total").register();
    static final Counter permRequestNumber = Counter.build()
            .name("perm_request_total")
            .help("Perm Request Total").register();
    static final Counter sdStaticRequestNumber = Counter.build()
            .name("sdStatic_request_total")
            .help("SdStatic Request Total").register();
    static final Counter sdDynamicRequestNumber = Counter.build()
            .name("sdDynamic_request_total")
            .help("SdDynamic Request Total").register();
    static final Counter ouUserRequestNumber = Counter.build()
            .name("ouUser_request_total")
            .help("OuUser Request Total").register();
    static final Counter ouPermRequestNumber = Counter.build()
            .name("ouPerm_request_total")
            .help("OuPerm Request Total").register();
    static final Counter roleAdminRequestNumber = Counter.build()
            .name("roleAdmin_request_total")
            .help("RoleAdmin Request Total").register();
    static final Counter objectAdminRequestNumber = Counter.build()
            .name("objectAdmin_request_total")
            .help("ObjectAdmin Request Total").register();
    static final Counter permAdminRequestNumber = Counter.build()
            .name("permAdmin_request_total")
            .help("PermAdmin Request Total").register();
    static final Counter logOutRequestNumber = Counter.build()
            .name("logOut_request_total")
            .help("LogOut Request Total").register();
    static final Counter logInRequestNumber = Counter.build()
            .name("logIn_request_total")
            .help("LogIn Request Total").register();
    static final Counter launchRequestNumber = Counter.build()
            .name("launch_request_total")
            .help("Launch Request Total").register();
    static final Counter errorPageRequestNumber = Counter.build()
            .name("error_request_total")
            .help("Error Request Total").register();

    public FortressWebBasePage()
    {
        requestTotalCount.inc();
        SecureBookmarkablePageLink usersLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web
            .common.GlobalIds.USERS_PAGE, UserPage.class,
            org.apache.directory.fortress.web.common.GlobalIds.ROLE_USERS );
        add( usersLink );
        PageParameters parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.RBAC_TYPE );
        SecureBookmarkablePageLink rolesLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.ROLES_PAGE, RolePage.class,
            parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_ROLES );
        add( rolesLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.ADMIN_TYPE );
        SecureBookmarkablePageLink admrolesLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.ADMROLES_PAGE,
            RoleAdminPage.class, parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_ADMINROLES );
        add( admrolesLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.RBAC_TYPE );
        SecureBookmarkablePageLink objectsLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.POBJS_PAGE,
            ObjectPage.class, parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_PERMOBJS );
        add( objectsLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.ADMIN_TYPE );
        SecureBookmarkablePageLink admobjsLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.ADMPOBJS_PAGE,
            ObjectAdminPage.class, parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_ADMINOBJS );
        add( admobjsLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.RBAC_TYPE );
        SecureBookmarkablePageLink permsLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.PERMS_PAGE, PermPage.class,
            parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_PERMS );
        add( permsLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.ADMIN_TYPE );
        SecureBookmarkablePageLink admpermsLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.ADMPERMS_PAGE,
            PermAdminPage.class, parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_ADMINPERMS );
        add( admpermsLink );
        SecureBookmarkablePageLink policiesLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.PWPOLICIES_PAGE,
            PwPolicyPage.class, org.apache.directory.fortress.web.common.GlobalIds.ROLE_POLICIES );
        add( policiesLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.SSD );
        SecureBookmarkablePageLink ssdsLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.SSDS_PAGE,
            SdStaticPage.class, parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_SSDS );
        add( ssdsLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.DSD );
        SecureBookmarkablePageLink dsdsLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.DSDS_PAGE,
            SdDynamicPage.class, parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_DSDS );
        add( dsdsLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, GlobalIds.USEROUS );
        SecureBookmarkablePageLink userouLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.USEROUS_PAGE,
            OuUserPage.class, parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_USEROUS );
        add( userouLink );
        parameters = new PageParameters();
        //parameters.set( GlobalIds.PAGE_TYPE, "PERMOUS" );
        SecureBookmarkablePageLink permouLink = new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.PERMOUS_PAGE,
            OuPermPage.class, parameters, org.apache.directory.fortress.web.common.GlobalIds.ROLE_PERMOUS );
        add( permouLink );

        /* TODO: Add groups back:
        add( new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.GROUP_PAGE, GroupPage.class,
            org.apache.directory.fortress.web.common.GlobalIds.ROLE_GROUPS ) );
        */

        add( new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.AUDIT_BINDS_PAGE, AuditBindPage.class,
            org.apache.directory.fortress.web.common.GlobalIds.ROLE_AUDIT_BINDS ) );

        add( new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.AUDIT_AUTHZS_PAGE, AuditAuthzPage.class,
            org.apache.directory.fortress.web.common.GlobalIds.ROLE_AUDIT_AUTHZS ) );

        add( new SecureBookmarkablePageLink( org.apache.directory.fortress.web.common.GlobalIds.AUDIT_MODS_PAGE, AuditModPage.class,
            org.apache.directory.fortress.web.common.GlobalIds.ROLE_AUDIT_MODS ) );

        add( new Label( "footer", "Copyright (c) 2003-2016, The Apache Software Foundation. All Rights Reserved." ) );

        final Link actionLink = new Link( "logout" )
        {
            /** Default serialVersionUID */
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick()
            {
                setResponsePage( LogoutPage.class );
            }
        };
        add( actionLink );
        HttpServletRequest servletReq = ( HttpServletRequest ) getRequest().getContainerRequest();

        // RBAC Security Processing:
        Principal principal = servletReq.getUserPrincipal();
        // Is this a Java EE secured page && has the User successfully authenticated already?
        boolean isSecured = principal != null;
        if ( isSecured && !isLoggedIn() )
        {
            // Here the principal was created by fortress realm and is a serialized instance of {@link Session}.
            String szPrincipal = principal.toString();
            Session session = null;

            String szIsJetty = System.getProperty( org.apache.directory.fortress.web.common.GlobalIds.IS_JETTY_SERVER );
            boolean isJetty = false;
            if( StringUtils.isNotEmpty( szIsJetty ))
            {
                if ( szIsJetty.equalsIgnoreCase( "true" ) )
                {
                    isJetty = true;
                }
            }
            if( !isJetty )
            {
                try
                {
                    // Deserialize the principal string into a fortress session:
                    session = j2eePolicyMgr.deserialize( szPrincipal );
                }
                catch(SecurityException se)
                {
                    // Can't recover....
                    throw new RuntimeException( se );
                }
            }

            // If this is null, it means this app cannot share an rbac session with container and must now (re)create session here:
            if ( session == null )
            {
                session = SecUtils.createSession( accessMgr, principal.getName() );
            }

            // Now load the fortress session into the Wicket session and let wicket hold onto that for us.  Also retreive the arbac perms from server and cache those too.
            synchronized ( ( WicketSession ) WicketSession.get() )
            {
                SecUtils.loadPermissionsIntoSession( delAccessMgr, session );
            }
        }
    }


    private boolean isLoggedIn()
    {
        boolean isLoggedIn = false;
        if ( SecUtils.getSession( this ) != null )
        {
            isLoggedIn = true;
        }
        return isLoggedIn;
    }
}