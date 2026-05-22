package com.zippt.l3l4.client.ui;

import com.zippt.l3l4.common.command.Commands.BidProposalInput;

public class AgentInterface {
    public void requestOpenAuctionList(String agentId) {
        showMessage("Agent requests open auctions: " + agentId);
    }

    public void selectAuction(String auctionId) {
        showMessage("Agent selected auction: " + auctionId);
    }

    public void enterBidProposal(BidProposalInput proposalInput) {
        showMessage("Agent entered bid proposal: " + proposalInput);
    }

    public void confirmBidSubmission() {
        showMessage("Agent confirmed bid submission.");
    }

    public void cancelBidSubmission() {
        showMessage("Agent cancelled bid submission.");
    }

    public void showMessage(String message) {
        System.out.println("[AgentInterface] " + message);
    }
}

