package com.qunar.qchat.utils;

import org.apache.http.util.TextUtils;

public class QtalkStringUtils {

    public static String parseId(String jid) {
        if (TextUtils.isEmpty(jid)) {
            return "";
        }
        int slashIndex = jid.indexOf("@");
        if (slashIndex != -1) {
            return jid.substring(0, slashIndex);
        } else {
            return jid;
        }
    }

    /**
     * Returns the domain of an XMPP address (JID). For example, for the address "user@xmpp.org/Resource", "xmpp.org"
     * would be returned. If <code>jid</code> is <code>null</code>, then this method returns also <code>null</code>. If
     * the input String is no valid JID or has no domainpart, then this method will return the empty String.
     *
     * @param jid the XMPP address to parse.
     * @return the domainpart of the XMPP address, the empty String or <code>null</code>.
     */
    public static String parseDomain(String jid) {
        if (jid == null) return "";
        int atIndex = jid.indexOf('@');
        if (jid.contains("@conference.")) atIndex += 11;
        int slashIndex = jid.indexOf('/');
        if (slashIndex > 0) {
            // 'local@domain.foo/resource' and 'local@domain.foo/res@otherres' case
            if (slashIndex > atIndex) {
                return jid.substring(atIndex + 1, slashIndex);
                // 'domain.foo/res@otherres' case
            } else {
                return jid.substring(0, slashIndex);
            }
        } else {
            if (atIndex == -1) return "";
            return jid.substring(atIndex + 1);
        }
    }

    public static String userId2Jid(String userId, String domain) {
        if (TextUtils.isEmpty(userId)) return "";
        if (userId.contains("@")) return userId;
        return userId + "@" + domain;
    }

    public static String roomId2Jid(String roomId, String domain) {
        if (TextUtils.isEmpty(roomId)) return "";
        if (roomId.contains("@")) return roomId;
        return roomId + "@conference." + domain;
    }

    public static int getSignalType(String type){
        int signalType = 0;
        switch (type){
            case "chat":
                signalType = ProtoMessageOuterClass.SignalType.SignalTypeChat_VALUE;
                break;
            case "groupchat":
                signalType = ProtoMessageOuterClass.SignalType.SignalTypeGroupChat_VALUE;
                break;
            case "headline":
                signalType = ProtoMessageOuterClass.SignalType.SignalTypeHeadline_VALUE;
                break;
            case "subscription":
                signalType = ProtoMessageOuterClass.SignalType.SignalTypeSubscription_VALUE;
                break;
            case "collection":
                signalType = ProtoMessageOuterClass.SignalType.SignalTypeCollection_VALUE;
                break;
            case "consult":
                signalType = ProtoMessageOuterClass.SignalType.SignalTypeConsult_VALUE;
                break;
            default:
                signalType = -1;
                break;
        }
        return signalType;
    }


}
