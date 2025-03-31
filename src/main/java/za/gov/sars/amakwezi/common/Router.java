/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.common;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author S2026987
 */
@Getter
@Setter
public class Router implements Serializable {
    private boolean administrator;
    private boolean nomination;
    private boolean reports;

    public Router reset() {
        administrator = Boolean.FALSE;
        nomination = Boolean.FALSE;
        reports = Boolean.FALSE;
        return this;
    }
}
