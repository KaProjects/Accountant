package org.kaleta.accountant.frontend.component.procedure;

import org.kaleta.accountant.common.Constants;

/**
 * Says which procedure groups the editor may touch.
 * <p>
 * Most groups are the user's own: they are named, renamed and filled by hand. A few are written by
 * the app as a by-product of creating something else - the creation procedure of a long-term
 * financial asset is written when the asset is created - and those are shown locked: the group
 * keeps the name the app knows it by, nothing is added to or deleted from it by hand, and a procedure
 * in it keeps the name and the group the app gave it. What such a procedure books stays editable:
 * the amount usually put into a financial asset is a standing guess, and correcting it is the whole
 * point of opening the procedure.
 */
public final class ProcedureRules {

    private ProcedureRules() {
        // static members only
    }

    public static boolean isAppMaintained(String groupName) {
        return Constants.Procedure.DERIVED_GROUP_NAMES.contains(groupName);
    }

    /**
     * What the card's header says about a locked group, or null when there is nothing to say. The
     * wording is the schema editor's, where "derived" marks what the app maintains by following
     * something else - here, the financial assets the procedures are written for.
     */
    public static String modeHint(String groupName) {
        return isAppMaintained(groupName) ? "derived" : null;
    }
}
