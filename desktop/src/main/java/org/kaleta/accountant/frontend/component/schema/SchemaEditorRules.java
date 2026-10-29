package org.kaleta.accountant.frontend.component.schema;

import org.kaleta.accountant.common.Constants;

import java.util.List;

/**
 * Says which parts of the schema the editor may touch.
 * <p>
 * The schema is shared by every year and its three digit ids are referenced by every account and
 * every transaction ever recorded, so an id must never change its meaning: nothing is deleted or
 * renumbered here, a group or an account is only created, renamed, or left alone.
 * <p>
 * Beyond that, a group's name and its accounts are locked separately, because they are relied upon
 * for different reasons. A name is what the app and its users recognise a structural group by -
 * the finance groups, the two groups holding the financial asset accounts - while those groups
 * still take new accounts freely. Other groups are structural all the way through: the code
 * addresses their accounts by a hard-coded id, or the app generates the accounts itself by
 * mirroring another class, and then nothing in the group may be touched.
 */
public final class SchemaEditorRules {

    /** Schema classes, in the order the editor shows them as tabs. */
    public static final List<Integer> CLASS_IDS = List.of(0, 1, 2, 3, 4, 5, 6, 7);

    /** Highest group or account id that fits in a three digit schema id. */
    public static final int LAST_ID = 9;

    public enum GroupMode {
        /** The user's own group: rename it, rename its accounts, add accounts. */
        EDITABLE,
        /** The name is structural, the contents are not: accounts stay fully editable. */
        FIXED_NAME,
        /** Structural throughout: the code addresses these accounts by id, so nothing changes. */
        FIXED,
        /** Mirrors another class and is maintained by the app: read only. */
        DERIVED
    }

    private SchemaEditorRules() {
        // static members only
    }

    public static GroupMode modeOf(int classId, int groupId) {
        if (classId == 0 && groupId == Integer.parseInt(Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) {
            return GroupMode.DERIVED; // 09 a. d. of <asset group>
        }
        if (classId == 5 && (groupId == 0 || groupId == 1)) {
            return GroupMode.DERIVED; // 50 d. of <asset group>, 51 c. of <resource group>
        }
        if (classId == 3 && groupId <= 1) {
            return GroupMode.FIXED; // 30 receivables, 31 liabilities
        }
        if (classId == 4 && groupId == 0) {
            return GroupMode.FIXED; // 40 capital
        }
        if (classId == 7) {
            return GroupMode.FIXED; // off balance: opening, closing and profit statement accounts
        }
        if (classId == 2 && groupId <= 3) {
            return GroupMode.FIXED_NAME; // the finance groups: cash, debit, credit, financial assets
        }
        if ((classId == 5 && groupId == 4) || (classId == 6 && groupId == 2)) {
            return GroupMode.FIXED_NAME; // hold the financial asset accounts the app generates into
        }
        return GroupMode.EDITABLE;
    }

    /**
     * Mode of one account, which is its group's mode unless the account itself is one the app
     * maintains - the financial asset creation and revaluation accounts. An account of a group
     * whose name alone is fixed is as editable as any other.
     */
    public static GroupMode modeOf(int classId, int groupId, int accountId) {
        String schemaId = "" + classId + groupId + accountId;
        if (schemaId.equals(Constants.Schema.FIN_CREATION_FULL_ID)
                || schemaId.equals(Constants.Schema.FIN_EXP_REVALUATION_FULL_ID)
                || schemaId.equals(Constants.Schema.FIN_REV_REVALUATION_FULL_ID)) {
            return GroupMode.DERIVED;
        }
        GroupMode groupMode = modeOf(classId, groupId);
        return groupMode == GroupMode.FIXED_NAME ? GroupMode.EDITABLE : groupMode;
    }

    /** Returns true if the group's own name may be changed. */
    public static boolean canRenameGroup(int classId, int groupId) {
        return modeOf(classId, groupId) == GroupMode.EDITABLE;
    }

    /** Returns true if this account may be renamed. */
    public static boolean canRenameAccount(int classId, int groupId, int accountId) {
        return modeOf(classId, groupId, accountId) == GroupMode.EDITABLE;
    }

    /** Returns true if accounts may still be added to this group's free slots. */
    public static boolean canCreateAccount(int classId, int groupId) {
        GroupMode mode = modeOf(classId, groupId);
        return mode == GroupMode.EDITABLE || mode == GroupMode.FIXED_NAME;
    }

    /** Returns true if a brand new group may be created in this empty slot. */
    public static boolean canCreateGroup(int classId, int groupId) {
        return modeOf(classId, groupId) == GroupMode.EDITABLE;
    }

    /** Short word for the card header telling why a group is not fully editable, null when it is. */
    public static String modeHint(GroupMode mode) {
        switch (mode) {
            case FIXED_NAME: return "fixed name";
            case FIXED: return "fixed";
            case DERIVED: return "derived";
            default: return null;
        }
    }
}
