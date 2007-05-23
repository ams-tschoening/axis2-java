/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.MessageSender;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChannelSender implements MessageSender {

    private Channel channel;

    private static final Log log = LogFactory.getLog(ChannelSender.class);

    public void sendToGroup(ClusteringCommand msg) throws ClusteringFault {
        if(channel == null) return;
        Member[] group = channel.getMembers();
        log.debug("Group size " + group.length);
        // send the message

//        for (int i = 0; i < group.length; i++) {
//            printMember(group[i]);
//        }

        if (group.length > 0) {
            try {
                channel.send(group, msg, 0);
            } catch (ChannelException e) {
                log.error("" + msg, e);
                String message = "Error sending command message : " + msg;
                throw new ClusteringFault(message, e);
            }
        }
    }

    public void sendToSelf(ClusteringCommand msg) throws ClusteringFault {
        if(channel == null) return;
        try {
            channel.send(new Member[]{channel.getLocalMember(true)},
                         msg,
                         Channel.SEND_OPTIONS_USE_ACK);
        } catch (ChannelException e) {
            throw new ClusteringFault(e);
        }
    }

    public void sendToGroup(Throwable throwable) throws ClusteringFault {
        if(channel == null) return;
        Member[] group = channel.getMembers();
        log.debug("Group size " + group.length);
        // send the message

        for (int i = 0; i < group.length; i++) {
            printMember(group[i]);
        }

        if (group.length > 0) {
            try {
                channel.send(group, throwable, 0);
            } catch (ChannelException e) {
                log.error("" + throwable, e);
                String message = "Error sending exception message : " + throwable;
                throw new ClusteringFault(message, e);
            }
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    private void printMember(Member member) {
        member.getUniqueId();
        log.debug("\n===============================");
        log.debug("Member Name " + member.getName());
        log.debug("Member Host" + member.getHost());
        log.debug("Member Payload" + member.getPayload());
        log.debug("===============================\n");
    }
}
