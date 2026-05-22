package com.zippt.l3l4sa.client.domain;

import com.zippt.l3l4sa.common.command.Commands.BidProposalInput;
import com.zippt.l3l4sa.common.command.Commands.SubmitBidCommand;

public class BidSession {
    private String agentId;
    private String selectedAuctionId;
    private BidProposalInput proposalInput;

    public void storeAgent(String agentId) {
        this.agentId = agentId;
    }

    public void storeSelectedAuction(String auctionId) {
        this.selectedAuctionId = auctionId;
    }

    public void storeBidProposal(BidProposalInput input) {
        this.proposalInput = input;
    }

    public SubmitBidCommand toSubmitBidCommand() {
        return new SubmitBidCommand(agentId, selectedAuctionId, proposalInput);
    }

    public void discard() {
        agentId = null;
        selectedAuctionId = null;
        proposalInput = null;
    }
}


