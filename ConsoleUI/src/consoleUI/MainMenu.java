package consoleUI;

import java.util.Scanner;

public class MainMenu {

    private static final Magit magit = new Magit();

    public static void main(String[] args) {

        System.out.println("Welcome to Magit!\n");
        boolean toEnd = false;
        do {

            magit.printCurrentRepoDetails();
            printMenu();
            System.out.println();
            int userSelectionNum = getValidUserOperSelection();
            if (MenuItem.isExitCode(userSelectionNum)) {
                System.out.println("Goodbye!");
                toEnd = true;
            } else {
                MenuItem userSelection = MenuItem.getItemByInt(userSelectionNum);
                doRequestedOperation(userSelection);
                continueOnNextKeyPress();
            }
        } while (!toEnd);
    }

    private static void printMenu() {
        System.out.println("Please type the number of the operation you wish to do:");

        if (!magit.isRepoDefined())
            printNoDefinedRepoMenu();
        else {
            for (MenuItem item : MenuItem.values())
                System.out.println(item);
        }

        System.out.println(MenuItem.EXIT_OPER_CODE + ". Quit program");
    }

    private static void printNoDefinedRepoMenu() {
        System.out.println(MenuItem.NEW_REPO_SCRATCH);
        System.out.println(MenuItem.OPEN_FROM_XML);
        System.out.println(MenuItem.OPEN_FROM_DISK);
    }

    private static int getValidUserOperSelection() {
        Scanner scanner = new Scanner(System.in);
        int selection;
        boolean validInput = false;

        do {
            while (!scanner.hasNextInt()) {
                String input = scanner.nextLine();
                System.out.println(input + " is not a valid number. Please type a one...");
            }
            selection = scanner.nextInt();
            //scanner.skip(".*");
            scanner.nextLine();

            if (MenuItem.isOperNumValid(selection) || MenuItem.isExitCode(selection))
                validInput = true;
            else
                System.out.println(selection + " is not a valid operation code. Please type a one...");

        } while (!validInput);

        return selection;
    }

    private static void doRequestedOperation(MenuItem requestedOper) {
        switch (requestedOper) {

            case NEW_REPO_SCRATCH:
                magit.createNewRepoFromScratch();
                break;

            case OPEN_FROM_XML:
                magit.loadRepoFromXml();
                break;

            case OPEN_FROM_DISK:
                magit.openRepoFromDisk();
                break;

            case CHANGE_USERNAME:
                magit.changeUsername();
                break;

            case SHOW_CURRENT_COMMIT:
                magit.showCurrentCommitObjects();
                break;

            case SHOW_STATUS:
                magit.checkWcStatus();
                break;

            case COMMIT:
                magit.commit();
                break;

            case SHOW_BRANCHES:
                magit.showAllBranches();
                break;

            case NEW_BRANCH:
                magit.createNewBranch();
                break;

            case DELETE_BRANCH:
                magit.deleteBranch();
                break;

            case SHOW_COMMIT_HISTORY:
                magit.showActiveBranchCommitHistory();
                break;

            case CHECKOUT:
                magit.checkout();
                break;

//            case PRINT_OBJECTS_TEST:
//                magit.printObject_TEST();
//                break;
        }
    }

    private static void continueOnNextKeyPress() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press Enter to continue...\n");
        scanner.nextLine();
    }


}
