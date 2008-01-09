/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.clustering.tribes;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.RequestBlockingHandler;
import org.apache.axis2.clustering.configuration.ConfigurationManager;
import org.apache.axis2.clustering.configuration.DefaultConfigurationManager;
import org.apache.axis2.clustering.context.ClusteringContextListener;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.clustering.context.DefaultContextManager;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.clustering.control.GetConfigurationCommand;
import org.apache.axis2.clustering.control.GetStateCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DispatchPhase;
import org.apache.axis2.engine.Phase;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.interceptors.DomainFilterInterceptor;
import org.apache.catalina.tribes.group.interceptors.TcpFailureDetector;
import org.apache.catalina.tribes.transport.ReceiverBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TribesClusterManager implements ClusterManager {
    private static final Log log = LogFactory.getLog(TribesClusterManager.class);

    private DefaultConfigurationManager configurationManager;
    private DefaultContextManager contextManager;

    private HashMap parameters;
    private ManagedChannel channel;
    private ConfigurationContext configurationContext;
    private TribesControlCommandProcessor controlCmdProcessor;
    private ChannelListener channelListener;
    private ChannelSender channelSender;

    public TribesClusterManager() {
        parameters = new HashMap();
        controlCmdProcessor = new TribesControlCommandProcessor(configurationContext);
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void init() throws ClusteringFault {

        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        for (Iterator iterator = axisConfig.getInFlowPhases().iterator();
             iterator.hasNext();) {
            Phase phase = (Phase) iterator.next();
            if (phase instanceof DispatchPhase) {
                RequestBlockingHandler requestBlockingHandler = new RequestBlockingHandler();
                if (!phase.getHandlers().contains(requestBlockingHandler)) {
                    PhaseRule rule = new PhaseRule("Dispatch");
                    rule.setAfter("SOAPMessageBodyBasedDispatcher");
                    rule.setBefore("InstanceDispatcher");
                    HandlerDescription handlerDesc = requestBlockingHandler.getHandlerDesc();
                    handlerDesc.setHandler(requestBlockingHandler);
                    handlerDesc.setName(ClusteringConstants.REQUEST_BLOCKING_HANDLER);
                    handlerDesc.setRules(rule);
                    phase.addHandler(requestBlockingHandler);
                }
                break;
            }
        }
        for (Iterator iterator = axisConfig.getInFaultFlowPhases().iterator();
             iterator.hasNext();) {
            Phase phase = (Phase) iterator.next();
            if (phase instanceof DispatchPhase) {
                RequestBlockingHandler requestBlockingHandler = new RequestBlockingHandler();
                if (!phase.getHandlers().contains(requestBlockingHandler)) {
                    PhaseRule rule = new PhaseRule("Dispatch");
                    rule.setAfter("SOAPMessageBodyBasedDispatcher");
                    rule.setBefore("InstanceDispatcher");
                    HandlerDescription handlerDesc = requestBlockingHandler.getHandlerDesc();
                    handlerDesc.setHandler(requestBlockingHandler);
                    handlerDesc.setName(ClusteringConstants.REQUEST_BLOCKING_HANDLER);
                    handlerDesc.setRules(rule);
                    phase.addHandler(requestBlockingHandler);
                    break;
                }
            }
        }

        channelSender = new ChannelSender();
        channelListener = new ChannelListener(configurationContext,
                                              configurationManager,
                                              contextManager,
                                              controlCmdProcessor,
                                              channelSender,
                                              synchronizeAllMembers());

        controlCmdProcessor.setChannelSender(channelSender);
        channel = new GroupChannel();



        // Set the IP address that will be advertised by this node
        String localIP = System.getProperty(ClusteringConstants.LOCAL_IP_ADDRESS);
        if (localIP != null) {
            ReceiverBase receiver = (ReceiverBase) channel.getChannelReceiver();
            receiver.setAddress(localIP);
        }

        // Set the domain for this Node
        Parameter domainParam = getParameter(ClusteringConstants.DOMAIN);
        byte[] domain;
        if (domainParam != null) {
            domain = ((String) domainParam.getValue()).getBytes();
        } else {
            domain = "apache.axis2.domain".getBytes();
        }
        channel.getMembershipService().setDomain(domain);
        DomainFilterInterceptor dfi = new DomainFilterInterceptor();
        dfi.setDomain(domain);
        channel.addInterceptor(dfi);

        // Add the NonBlockingCoordinator. This is used for leader election
        /*nbc = new NonBlockingCoordinator() {
            public void fireInterceptorEvent(InterceptorEvent event) {
                String status = event.getEventTypeDesc();
                System.err.println("$$$$$$$$$$$$ NBC status=" + status);
                int type = event.getEventType();
            }
        };
        nbc.setPrevious(dfi);
        channel.addInterceptor(nbc);*/

        /*Properties mcastProps = channel.getMembershipService().getProperties();
       mcastProps.setProperty("mcastPort", "5555");
       mcastProps.setProperty("mcastAddress", "224.10.10.10");
       mcastProps.setProperty("mcastClusterDomain", "catalina");
       mcastProps.setProperty("bindAddress", "localhost");
       mcastProps.setProperty("memberDropTime", "20000");
       mcastProps.setProperty("mcastFrequency", "500");
       mcastProps.setProperty("tcpListenPort", "4000");
       mcastProps.setProperty("tcpListenHost", "127.0.0.1");*/

        TcpFailureDetector tcpFailureDetector = new TcpFailureDetector();
        tcpFailureDetector.setPrevious(dfi);
        channel.addInterceptor(tcpFailureDetector);

        channel.addChannelListener(channelListener);
        TribesMembershipListener membershipListener = new TribesMembershipListener();
        channel.addMembershipListener(membershipListener);
        try {
            channel.start(Channel.DEFAULT);
            String localHost = TribesUtil.getLocalHost(channel);
            if (localHost.startsWith("127.0.")) {
                channel.stop(Channel.DEFAULT);
                throw new ClusteringFault("Cannot join cluster using IP " + localHost +
                                          ". Please set an IP address other than " +
                                          localHost + " in your /etc/hosts file or set the " +
                                          ClusteringConstants.LOCAL_IP_ADDRESS +
                                          " System property and retry.");
            }
        } catch (ChannelException e) {
            throw new ClusteringFault("Error starting Tribes channel", e);
        }
        channelSender.setChannel(channel);

//        Member[] members = channel.getMembers();
        log.info("Local Tribes Member " + TribesUtil.getLocalHost(channel));
        TribesUtil.printMembers();

        // If configuration management is enabled, get the latest config from a neighbour
        if (configurationManager != null) {
            configurationManager.setSender(channelSender);
            getInitializationMessage(channelSender, new GetConfigurationCommand());
        }

        // If context replication is enabled, get the latest state from a neighbour  
        if (contextManager != null) {
            contextManager.setSender(channelSender);
            channelListener.setContextManager(contextManager);
            getInitializationMessage(channelSender, new GetStateCommand());
            ClusteringContextListener contextListener = new ClusteringContextListener(channelSender);
            configurationContext.addContextListener(contextListener);
        }
        configurationContext.
                setNonReplicableProperty(ClusteringConstants.CLUSTER_INITIALIZED, "true");
    }

    /**
     * Get some information from a neighbour. This information will be used by this node to
     * initialize itself
     *
     * @param sender  The utility for sending messages to the channel
     * @param command The control command to send
     */
    private void getInitializationMessage(ChannelSender sender, ControlCommand command) {
        // If there is at least one member in the cluster,
        //  get the current initialization info from a member
        int numberOfTries = 0; // Don't keep on trying indefinitely

        // Keep track of members to whom we already sent an initialization command
        // Do not send another request to these members
        List sentMembersList = new ArrayList();
        sentMembersList.add(TribesUtil.getLocalHost(channel));
        Member[] members = MembershipManager.getMembers();
        while (members.length > 0 &&
               configurationContext.
                       getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null
               && numberOfTries < 50) {

            // While there are members and GetStateResponseCommand is not received do the following
            try {
                Member member = (numberOfTries == 0) ?
                                MembershipManager.getLongestLivingMember() : // First try to get from the longest alive member
                                MembershipManager.getRandomMember(); // Else get from a random member
                if (!sentMembersList.contains(TribesUtil.getHost(member))) {
                    long tts = sender.sendToMember(command, member);
                    configurationContext.
                            setNonReplicableProperty(ClusteringConstants.TIME_TO_SEND,
                                                     new Long(tts));
                    sentMembersList.add(TribesUtil.getHost(member));
                    log.debug("WAITING FOR STATE INITIALIZATION MESSAGE...");
                    Thread.sleep(tts + 5 * (numberOfTries + 1));
                }
            } catch (Exception e) {
                log.error("Cannot get initialization information", e);
                break;
            }
            members = MembershipManager.getMembers();
            numberOfTries++;
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = (DefaultConfigurationManager) configurationManager;
        this.configurationManager.setSender(channelSender);
    }

    public void setContextManager(ContextManager contextManager) {
        this.contextManager = (DefaultContextManager) contextManager;
        this.contextManager.setSender(channelSender);
    }

    public void addParameter(Parameter param) throws AxisFault {
        parameters.put(param.getName(), param);
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        throw new UnsupportedOperationException();
    }

    public Parameter getParameter(String name) {
        return (Parameter) parameters.get(name);
    }

    public ArrayList getParameters() {
        ArrayList list = new ArrayList();
        for (Iterator it = parameters.keySet().iterator(); it.hasNext();) {
            list.add(parameters.get(it.next()));
        }
        return list;
    }

    public boolean isParameterLocked(String parameterName) {
        Parameter parameter = (Parameter) parameters.get(parameterName);
        return parameter != null && parameter.isLocked();
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    public void shutdown() throws ClusteringFault {
        log.debug("Enter: TribesClusterManager::shutdown");
        if (channel != null) {
            try {
                channel.stop(Channel.DEFAULT);
            } catch (ChannelException e) {

                if (log.isDebugEnabled()) {
                    log.debug("Exit: TribesClusterManager::shutdown");
                }

                throw new ClusteringFault(e);
            }
        }
        log.debug("Exit: TribesClusterManager::shutdown");
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        controlCmdProcessor.setConfigurationContext(configurationContext);
        if (channelListener != null) {
            channelListener.setConfigurationContext(configurationContext);
        }
        if (configurationManager != null) {
            configurationManager.setConfigurationContext(configurationContext);
        }
        if (contextManager != null) {
            contextManager.setConfigurationContext(configurationContext);
        }
    }

    public boolean synchronizeAllMembers() {
        Parameter syncAllParam = getParameter(ClusteringConstants.SYNCHRONIZE_ALL_MEMBERS);
        return syncAllParam == null || Boolean.parseBoolean((String) syncAllParam.getValue());
    }
}
