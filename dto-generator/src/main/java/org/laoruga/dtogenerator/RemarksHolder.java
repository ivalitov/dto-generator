package org.laoruga.dtogenerator;

import lombok.Getter;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */
@Getter
public class RemarksHolder {

    private final BasicRemarksHolder basicRemarks;
    private final CustomRemarksHolder customRemarks;

    public RemarksHolder() {
        this(new BasicRemarksHolder(), new CustomRemarksHolder());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    RemarksHolder(RemarksHolder toCopy) {
        this(new BasicRemarksHolder(toCopy.getBasicRemarks()),
                new CustomRemarksHolder(toCopy.getCustomRemarks()));
    }

    private RemarksHolder(BasicRemarksHolder basicRemarks, CustomRemarksHolder customRemarks) {
        this.basicRemarks = basicRemarks;
        this.customRemarks = customRemarks;
    }
}
