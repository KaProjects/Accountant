package org.kaleta.accountant.frontend.common;

public interface Validable {

    /**
     * Returns error message if component is NOT in valid form, null otherwise.
     */
    String validator();
}
