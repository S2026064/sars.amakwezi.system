/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package za.gov.sars.amakhwezi.common;

/**
 *
 * @author S2026080
 */
public enum SarsValue {
    UNCOMPROMISING_REGARD_FOR_TAXPAYER_CONFIDENTIALITY("Uncompromising regard for Taxpayer confidentiality;"),
    UNQUESTIONABLE_INTEGRITY_PROFESSIONALISM_AND_FAIRNESS("Unquestionable integrity, professionalism, and fairness;"),
    EXEMPLARY_PUBLIC_SERVICE("Exemplary public service;"),
    INCONTESTABLE_INSIGHT_FROM_DATA_AND_EVIDENCE("Incontestable insight from data and evidence.");

    private final String name;

    SarsValue(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
