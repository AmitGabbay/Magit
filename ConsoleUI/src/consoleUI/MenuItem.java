package consoleUI;

import java.util.HashMap;
import java.util.Map;

public enum MenuItem {
    NEW_REPO_SCRATCH(1,"Create a new Magit repository from scratch"),
    OPEN_FROM_XML(2,"Open Magit repository from XML file"),
    OPEN_FROM_DISK(3,"Open Magit repository from disk"),
    CHANGE_USERNAME(4,"Change active username"),
    SHOW_CURRENT_COMMIT(5,"Show current commit files and folders"),
    SHOW_STATUS(6,"Show Status (working copy files pending changes)"),
    COMMIT(7, "Commit"),
    SHOW_BRANCHES(8, "Show all branches"),
    NEW_BRANCH(9, "Create a new branch"),
    DELETE_BRANCH(10, "Delete an existing branch"),
    CHECKOUT(11, "Checkout (select branch)"),
    SHOW_COMMIT_HISTORY(12, "Show active branch commits history");
//    TEST_GENERAL(666, "TEST - GENERAL"),
//    TEST_GENERAL2(667, "TEST - GENERAL2"),
//    PRINT_OBJECTS_TEST(777,"Test: print objects");


    public static final int EXIT_OPER_CODE = values().length + 1;
    private static final Map<Integer, MenuItem> ITEM_BY_NUM = new HashMap<>();

    static {
        for (MenuItem item : values())
            ITEM_BY_NUM.put(item.UI_OpNumber, item);
    }

    public final int UI_OpNumber;
    public final String UI_Text;

    MenuItem(int UI_OpNumber, String UI_Text) {
        this.UI_Text = UI_Text;
        this.UI_OpNumber = UI_OpNumber;
    }

    public static MenuItem getItemByInt(int number) {
        return ITEM_BY_NUM.get(number);
    }

    public static boolean isOperNumValid(int operNum){
        return ITEM_BY_NUM.containsKey(operNum);
    }

    public static boolean isExitCode(int operNum){
        return (operNum == EXIT_OPER_CODE);
    }

    @Override
    public String toString() {
        return UI_OpNumber + ". " + UI_Text;
    }

//    public void eval(){
//            switch (this){
//                case NEW_REPO_SCRATCH: Magit.createNewRepoFromScratchWrapper(); break;
//                case TRAVERSE_WC: engine.fileMangers.FolderTraverser2.traverseWC_Wrapper(); break;
//            }
//    }

}
