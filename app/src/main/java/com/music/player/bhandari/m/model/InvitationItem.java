package com.music.player.bhandari.m.model;

/**
 * Created by abami on 27-Feb-18.
 */

public class InvitationItem {
    public String invitationId = "";
    public Boolean invitationAccepted = false;

    public InvitationItem(){}

    public InvitationItem(String invitationId, boolean invitationAccepted){
        this.invitationId = invitationId;
        this.invitationAccepted = invitationAccepted;
    }
}
