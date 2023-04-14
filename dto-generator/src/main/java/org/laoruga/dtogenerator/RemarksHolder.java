package org.laoruga.dtogenerator;

import lombok.Getter;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */
@Getter
public class RemarksHolder {

    private final RemarksHolderBasic basicRemarks;
    private final CustomGeneratorConfigMapHolder customRemarks;

    public RemarksHolder() {
        this(new RemarksHolderBasic(), new CustomGeneratorConfigMapHolder());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    RemarksHolder(RemarksHolder toCopy) {
        this(new RemarksHolderBasic(toCopy.getBasicRemarks()),
                new CustomGeneratorConfigMapHolder(toCopy.getCustomRemarks()));
    }

    private RemarksHolder(RemarksHolderBasic basicRemarks, CustomGeneratorConfigMapHolder customRemarks) {
        this.basicRemarks = basicRemarks;
        this.customRemarks = customRemarks;
    }
}
