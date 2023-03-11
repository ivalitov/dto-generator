package org.laoruga.dtogenerator;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.UtilsRoot.resetStaticConfig;

/**
 * @author Il'dar Valitov
 * Created on 06.03.2023
 */
public class Extensions {

    static public class RestoreStaticConfig implements AfterEachCallback {

        @Override
        public void afterEach(ExtensionContext context) {
            if (context.getTags().contains(RESTORE_STATIC_CONFIG)) {
                resetStaticConfig();
            }
        }
    }

}
