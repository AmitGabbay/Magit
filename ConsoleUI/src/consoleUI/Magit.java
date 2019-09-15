package consoleUI;

import engine.repo.Branch;
import engine.repo.Repository;
import engine.magitObjects.Commit;
import engine.magitObjects.MagitObject;
import engine.fileMangers.MagitFileUtils;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;

public class Magit {

    private Repository repo = null;

    private final String tryAgainNonEmptyOrSpacesInEdges = "Please enter a non-empty input which doesn't starts or " +
            "ends with a space.";
    private Predicate<String> nonEmptyAndNoSpacesInEdgesString = v -> !(v.isEmpty() || v.startsWith(" ") || v.endsWith(" "));

    private static void printNoDefinedRepoMsg() {
        System.out.println("Error! Magit cannot perform this operation without a defined " +
                "repository. \nPlease open an existing one or create a new one and then try again.");
        System.out.println();
    }

    private void handleGenericException(Exception e) {
        System.out.println("Oops... An error occurred! Please watch the details below and try using the function again\n");
        e.printStackTrace();
        System.out.println();
    }


    private String getValidUserString(String inputMsg, String tryAgainMsg, Predicate<String> validInput) {

        Scanner scanner = new Scanner(System.in);
        String userInput;
        boolean stopInputLoop = false;
        do {
            System.out.println(inputMsg);
            userInput = scanner.nextLine();
            if (validInput.test(userInput))
                stopInputLoop = true;
            else
                System.out.println(tryAgainMsg);

        } while (!stopInputLoop);

        return userInput;
    }


    public void createNewRepoFromScratch() {

        String nameInputMsg = "Please enter your new repo name: ";
        String repoPathInputMsg = "Please enter where to save your new repo: ";
        String tryAgainMsg = this.tryAgainNonEmptyOrSpacesInEdges;

        String newRepoName = getValidUserString(nameInputMsg, tryAgainMsg, this.nonEmptyAndNoSpacesInEdgesString);
        String requestedPath = getValidUserString(repoPathInputMsg, tryAgainMsg, this.nonEmptyAndNoSpacesInEdgesString);
        try {
            MagitFileUtils.newRepoOnDisk_PathValidation(requestedPath); //Exceptions will be catched if path invalid path given
            this.repo = Repository.newRepoFromScratchCreator(newRepoName, requestedPath);
            System.out.println("Your repo " + newRepoName + " has been created successfully at " + requestedPath + " !\n");
        } catch (InvalidPathException e) {
            System.out.println(e);
            System.out.println("The parent path you supplied doesn't exist. Please enter an existing one.");
        } catch (FileAlreadyExistsException e) {
            System.out.println(e);
            System.out.println("There is already a folder named " + e.getFile() + " in the parent path. " +
                    "Please supply a new folder name.");
        } catch (Exception e) {
            handleGenericException(e);
        }
    }


    public void openRepoFromDisk() {

        String inputMsg = "Please enter the location of your your existing repository: ";
        String tryAgainMsg = this.tryAgainNonEmptyOrSpacesInEdges;

        String repoPath = getValidUserString(inputMsg, tryAgainMsg, this.nonEmptyAndNoSpacesInEdgesString);

        try {
            if (MagitFileUtils.isExistingRepoPath(repoPath)) {
                this.repo = Repository.openRepoFromFolder(repoPath);
                System.out.println("The repository " + repo.getName() + " has opened successfully!\n");
            } else
                System.out.println("Invalid path. Please enter a valid existing path with \".magit\" folder inside it");

        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    public void changeUsername() {

        if (!isRepoDefined()) {
            printNoDefinedRepoMsg();
            return;
        }
        String inputMsg = "Please enter the username to change to: ";
        String tryAgainMsg = "Please enter a non-empty name.";

        try {
            String username = getValidUserString(inputMsg, tryAgainMsg, v -> !(v.isEmpty()));
            repo.setActiveUser(username);
            System.out.println("Changed successfully to work as " + username + "!\n");
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    public void showCurrentCommitObjects(){

        if (!isRepoDefined()) {
            printNoDefinedRepoMsg();
            return;
        }
        if (repo.isNoCommits()) {
            System.out.println("There are no commits on this Repository. Please commit and try again.");
            return;
        }

        try {
            Commit currentCommit = repo.getCurrentCommit();
            System.out.println("CurrentCommit: " + currentCommit.calcSha1());
            System.out.println(currentCommit.getInfoForUI());
            System.out.println(repo.getCurrentCommitObjectsData());

        } catch (Exception e) {
            handleGenericException(e);
        }
    }


    public void checkWcStatus() {

        if (!isRepoDefined()) {
            printNoDefinedRepoMsg();
            return;
        }

        printCurrentRepoDetails();
        try {
            boolean anyChanges = repo.checkForWcPendingChanges();

            if (anyChanges)
                System.out.println(repo.getWcPendingChanges());
            else
                System.out.println("There are no pending changes for commit");
        } catch (Exception e) {
            handleGenericException(e);
        }
    }


    public void commit() {

        String inputMsg = "Please enter a description for the new commit:";
        String tryAgainMsg = "Please enter a non-empty description.";

        if (!isRepoDefined()) {
            printNoDefinedRepoMsg();
            return;
        }

        checkWcStatus();
        if (!repo.isPendingChangesWaiting()) {
            System.out.println("Cannot commit if there isn't something new... Please try again later\n");
            return;
        }

        String commitDescription = getValidUserString(inputMsg, tryAgainMsg, v -> !(v.isEmpty()));
        try {
            repo.newCommit(commitDescription);
            System.out.println("New commit has been created!\n");
        } catch (Exception e) {
            handleGenericException(e);
        }
    }

    public void createNewBranch(){

        if (!isRepoDefined()) {
            printNoDefinedRepoMsg();
            return;
        }
        if (repo.isNoCommits()) {
            System.out.println("There are no commits on this Repository. Please commit and try again.");
            return;
        }

        String inputMsg = "Please enter the new branch name: ";
        String tryAgainMsg = "Please enter a non-empty name with no whitespaces.";
        try {
            String newBranchName = getValidUserString(inputMsg, tryAgainMsg, v -> v.matches(("\\S+"))); /*single word with
                                                                                                    no whitespaces*/
            if (repo.isBranchNameExists(newBranchName)) {
                System.out.println("Already got a branch with that name. Please try again with another name.");
                return;
            }

            String pointedCommitSha1 = repo.getActiveBranch().getPointedCommit();
            Branch newBranch = new Branch(newBranchName, pointedCommitSha1);
            repo.addNewBranchToRepo(newBranch);
            System.out.println("The branch " + newBranchName + " created successfully!");

            } catch (Exception e) {
            handleGenericException(e);
        }
    }

    public void showAllBranches()
    {
        if (!isRepoDefined()) {
            printNoDefinedRepoMsg();
            return;
        }

        try {
            System.out.println(repo.getName() + " branches info:\n");
            System.out.println(repo.getAllBranchesInfo());

        } catch (Exception e) {
            handleGenericException(e);
        }
    }


    public boolean isRepoDefined() {
        return this.repo != null;
    }

    public void printCurrentRepoDetails() {
        System.out.print("Working on the Repository: ");
        if (isRepoDefined()) {
            System.out.print(repo.getName() + "\t");
            System.out.println("Located in: " + repo.getStringPath());
            System.out.println("Current user: " + repo.getActiveUser());
            System.out.println();
        } else
            System.out.println("N\\A");
    }

    public void inDevProgress() {
        System.out.println("This operation will be added soon");

        System.out.println(repo.getActiveBranch());
    }

    /**
     * ******** Testing only section!!! ********************************************************************************
     */

    public void printObject_TEST() {
        Set<Map.Entry<String, MagitObject>> objects = repo.getObjectsAsEntrySet();
        for (Map.Entry<String, MagitObject> object : objects) {
            System.out.println(object.getKey() + ": " + object.getValue());
        }

        //System.out.println(repo.getObjectsAsMap());

//        Collection<MagitObject> objects  = repo.getObjectsTxtAsCollection();
//        for(MagitObject object : objects)
//            System.out.println(object);
    }

    public void my666() {
//        File toCheck = new File("C:\\Magit\\word.docx");
//        Blob blobi = new Blob(toCheck);
//        toCheck = new File("C:\\Magit\\aa.txt");
//        Blob blobi2 = new Blob(toCheck);
        //repo.test123();
        repo.createCurrentCommitDatabases();
    }

    public void my667() {
//        File toCheck = new File("C:\\Magit\\word.docx");
//        Blob blobi = new Blob(toCheck);
//        toCheck = new File("C:\\Magit\\aa.txt");
//        Blob blobi2 = new Blob(toCheck);
        //repo.test123();
        //repo.TEST_updateWcDatabases();
    }


} //class end


