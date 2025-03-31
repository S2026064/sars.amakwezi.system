/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.common;

/**
 *
 * @author S2026987
 */
public enum NominationStatus {

    SAVED("Saved"),
    SUBMITTED("Submitted"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    DELETED("Deleted"),
    REROUTED("Re-routed"),
    FINANCE_REROUTED("Finance Re-routed");

    private final String name;

    NominationStatus(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
