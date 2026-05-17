package com.example.backend.policy;

import java.util.ArrayList;
import java.util.List;

public class PolicyConfig {

    private static final int DEFAULT_MIN_APPROVALS = 1;
    private static final int DEFAULT_MIN_SCORE = 70;

    private int minApprovals = DEFAULT_MIN_APPROVALS;
    private int minScore = DEFAULT_MIN_SCORE;
    private List<FreezeWindow> freezeWindows = new ArrayList<>();
    private List<String> approvalRoles = new ArrayList<>();

    public int getMinApprovals() {
        return minApprovals;
    }

    public void setMinApprovals(final int minApprovals) {
        this.minApprovals = minApprovals;
    }

    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(final int minScore) {
        this.minScore = minScore;
    }

    public List<FreezeWindow> getFreezeWindows() {
        return freezeWindows;
    }

    public void setFreezeWindows(final List<FreezeWindow> freezeWindows) {
        this.freezeWindows = freezeWindows;
    }

    public List<String> getApprovalRoles() {
        return approvalRoles;
    }

    public void setApprovalRoles(final List<String> approvalRoles) {
        this.approvalRoles = approvalRoles;
    }
}
