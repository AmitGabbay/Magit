package consoleUI;

import java.util.HashMap;
import java.util.Map;

public enum MenuItem {
    NEW_REPO_SCRATCH(11,"Create new Magit repository"),
    SHOW_STATUS(12,"Show Status (WC Pending changes)"),
    CHANGE_REPO(13,"Change to existing Magit repository"),
    Commit(14, "Commit"),
    TEST_FIRST_COMMIT(99, "First commit test"),
    TEST_GENERAL(666, "TEST - GENERAL"),
    TEST_GENERAL2(667, "TEST - GENERAL2"),
    PRINT_OBJECTS_TEST(777,"Test: print objects");


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

//    public static int getExitOperCode() {
//        return EXIT_OPER_CODE;
//    }

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
