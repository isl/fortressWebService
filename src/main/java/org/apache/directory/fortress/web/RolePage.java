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


import io.prometheus.client.Summary;
import org.apache.directory.fortress.web.common.GlobalIds;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.directory.fortress.web.panel.Displayable;
import org.apache.directory.fortress.web.panel.InfoPanel;
import org.apache.directory.fortress.web.panel.RoleDetailPanel;
import org.apache.directory.fortress.web.panel.RoleListPanel;
import org.apache.wicket.util.string.StringValue;


/**
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class RolePage extends FortressWebBasePage
{
    /** Default serialVersionUID */
    private static final long serialVersionUID = 1L;
    private boolean isAdmin = false;
    private static final Logger LOG = Logger.getLogger( RolePage.class.getName() );


    public RolePage( PageParameters parameters )
    {

        String label = "RBAC Role Administration";
        add( new Label( GlobalIds.PAGE_HEADER, label ) );
        WebMarkupContainer container = new WebMarkupContainer( GlobalIds.LAYOUT );
        FourWaySplitter splitter = new FourWaySplitter();
        splitter.addBorderLayout( container );

        // Add the four necessary panels of Fortress Web Page: 1. Nav, 2. List, 3. Info, 4. Detail.
        // Nav and Info are generic and work across all entities, the others are specific to this entity type.
        Summary.Timer requestDelayTimer = roleDelayTimeLatency.startTimer();

        // 1. Nav Panel:
        NavPanel navPanel = new NavPanel( GlobalIds.NAVPANEL );

        // 2. List Panel:
        container.add( new AjaxLazyLoadPanel( GlobalIds.ROLELISTPANEL )
        {
            /** Default serialVersionUID */
            private static final long serialVersionUID = 1L;


            @Override
            public Component getLazyLoadComponent( String id )
            {
                return new RoleListPanel( id, isAdmin );
            }
        } );
        requestDelayTimer.observeDuration();

        Summary.Timer requestProcessTimer = roleProcessTimeLatency.startTimer();
        // 3. Info Panel:
        InfoPanel infoPanel = new InfoPanel( GlobalIds.INFOPANEL );
        container.add( infoPanel );

        // 4. Detail Panel:
        Displayable display = infoPanel.getDisplay();
        RoleDetailPanel roleDetail = new RoleDetailPanel( GlobalIds.ROLEDETAILPANEL, display, isAdmin );
        container.add( roleDetail );

        container.add( navPanel );
        this.add( container );
        requestProcessTimer.observeDuration();

        Summary.Timer requestAnswerDelayTimer = roleAnswerDelayTimeLatency.startTimer();
        NavPanel navPanel1 = new NavPanel( GlobalIds.NAVPANEL );
        InfoPanel infoPanel1 = new InfoPanel( GlobalIds.INFOPANEL );
        Displayable display1 = infoPanel1.getDisplay();
        requestAnswerDelayTimer.observeDuration();

        this.add(new AjaxEventBehavior("load") {

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
            {
                super.updateAjaxAttributes(attributes);
                String uuidParamConenctEnd = "return {'"+ PARAM_CONNECT_END_NAME +"': window.performance.timing.connectEnd}";
                attributes.getDynamicExtraParameters().add(uuidParamConenctEnd);

                String uuidParamFetchStart = "return {'"+ PARAM_FETCH_START_NAME +"': window.performance.timing.fetchStart}";
                attributes.getDynamicExtraParameters().add(uuidParamFetchStart);

                String uuidParamResponseEnd = "return {'"+ PARAM_RESPONSE_END_NAME +"': window.performance.timing.responseEnd}";
                attributes.getDynamicExtraParameters().add(uuidParamResponseEnd);

                String uuidParamResponseStart = "return {'"+ PARAM_RESPONSE_START_NAME +"': window.performance.timing.responseStart}";
                attributes.getDynamicExtraParameters().add(uuidParamResponseStart);

                String uuidParamLoadEventEnd = "return {'"+ PARAM_LOAD_EVENT_END_NAME +"': window.performance.timing.loadEventEnd}";
                attributes.getDynamicExtraParameters().add(uuidParamLoadEventEnd);

                String uuidParamDomLoading = "return {'"+ PARAM_DOM_LOADING_NAME +"': window.performance.timing.domLoading}";
                attributes.getDynamicExtraParameters().add(uuidParamDomLoading);

            }
            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                StringValue connectEnd = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("window.performance.timing.connectEnd");
                System.out.println("ConnectEnd equals to : " + connectEnd.toString());

                StringValue fetchStart = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("window.performance.timing.fetchStart");
                System.out.println("FetchStart equals to : " + fetchStart.toString());

                StringValue responseEnd = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("window.performance.timing.responseEnd");
                System.out.println("ResponseEnd equals to : " + responseEnd.toString());

                StringValue responseStart = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("window.performance.timing.responseStart");
                System.out.println("ResponseStart equals to : " + responseStart.toString());

                StringValue loadEventEnd = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("window.performance.timing.loadEventEnd");
                System.out.println("LoadEventEnd equals to : " + loadEventEnd.toString());

                StringValue domLoading = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("window.performance.timing.domLoading");
                System.out.println("DomLoading equals to : " + domLoading.toString());

                double dconnectEnd = Double.parseDouble(connectEnd.toString());
                double dfetchStart = Double.parseDouble(fetchStart.toString());
                double dresponseEnd = Double.parseDouble(responseEnd.toString());
                double dresponseStart = Double.parseDouble(responseStart.toString());
                double dloadEventEnd = Double.parseDouble(loadEventEnd.toString());
                double ddomLoading = Double.parseDouble(domLoading.toString());
                System.out.println("The double values would be : connectEnd -> "+ dconnectEnd+ " fetchStart ->" + dfetchStart + " responseEnd ->" + dresponseEnd +
                        " responseStart ->" +dresponseEnd + " dresponseStart ->"+ dresponseStart + " loadEventEnd->"+ dloadEventEnd +" domLoading-> "+ ddomLoading);

                double networkLatency = (dconnectEnd - dfetchStart) + (dresponseEnd - dresponseStart);
                double completionTime = dloadEventEnd - ddomLoading;

                System.out.println("NetworkLatency equals to : " + networkLatency);
                System.out.println("Request Completion Time equals to :" + completionTime);

                roleAcknowledgeNumber.inc();
                roleNetworkLatency.observe(networkLatency);
                roleRequestCompletionTime.observe(completionTime);
            }
        });
    }
    @Override
    protected void onConfigure(){
        roleRequestNumber.inc();
        super.onConfigure();
    }
    @Override
    protected void onAfterRender(){
        roleResponseNumber.inc();
        super.onAfterRender();
    }
}