package consoleUI;

import engine.magitObjects.Blob;
import engine.repo.Repository;
import engine.magitObjects.Commit;
import engine.magitObjects.MagitObject;
import engine.fileMangers.MagitFileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Magit {

    private Repository repo = null;

//    public static void createNewRepoFromScratchWrapper() {
//        repo = createNewRepoFromScratch();
//    }

    public static void printNoDefinedRepoMsg() {
        System.out.println("Error! Magit cannot perform this operation without a defined " +
                "repository. \nPlease open an existing one or create a new one and then try again.");
        System.out.println();
    }

    //todo add support for separate folder and repo names
    public void createNewRepoFromScratch() {
        Repository newRepo = null;
        String newRepoName = null;
        String requestedParentPath = null;
        Scanner scanner = new Scanner(System.in);
        boolean finishInputLoop = false;
//        if (isRepoDefined())      --> is this needed?
//            System.out.println("add q for changing repo");
        do {
            System.out.print("Please enter your new repo name: ");
            try {
                newRepoName = scanner.nextLine();
                //verify name basic requirements
                if (newRepoName.isEmpty() || newRepoName.startsWith(" ") || newRepoName.endsWith(" ")) {
                    System.out.println("Please enter a non-empty name which doesn't starts or ends with a space.");
                    continue; //skip to end of the loop iteration (ask the user for a name again...)
                }

                System.out.print("Please enter where to save your new repo (the parent directory " +
                        "e.g. C:\\magit): ");
                requestedParentPath = scanner.nextLine();

                Repository.checkNewRepoPath(requestedParentPath, newRepoName);
                String newRepoPath = requestedParentPath.concat("\\" + newRepoName);
                newRepo = Repository.newRepoFromScratchCreator(newRepoName, newRepoPath);

                System.out.println("Your repo " + newRepoName + " has been created successfully at " + newRepoPath + " !");
                finishInputLoop = true;

            } catch (InvalidPathException e) {
                System.out.println(e);
                System.out.println("The parent path you supplied doesn't exist. Please enter an existing one.");
            } catch (FileAlreadyExistsException e) {
                System.out.println(e);
                System.out.println("There is already a folder named " + newRepoName + " in the path " + requestedParentPath
                        + " . Please supply another valid path or another name for your repo.");
            } catch (IOException e) {
                System.out.println(e);
                System.out.println("General I/O Error! Please try to create the repo again.");
                finishInputLoop = true;
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Unknown Error! Please try to create the repo again.");
                finishInputLoop = true;
            }
        } while (!finishInputLoop);

        this.repo = newRepo;
    }

    public void traverseWC() {
        if (!isRepoDefined()) {
            printNoDefinedRepoMsg();
            return;
        }

        this.repo.traverseWC("FirstCommit");
    }

    //todo verify error checking
    public void changeRepo() {

        Scanner scanner = new Scanner(System.in);
        String repoName, repoPath;
        System.out.println("Please enter the location of your your existing repository:");
        repoPath = scanner.nextLine();
        //todo add check for .magit folder

        //todo ask user for setting a name
        int lastSlash = repoPath.lastIndexOf('\\');
        repoName = repoPath.substring(lastSlash + 1);
        this.repo = new Repository(repoName, repoPath);
        //todo to load data (remember to load the head branch!!!)
    }

    public void firstCommit() {

        String commitDescription;
        Scanner scanner = new Scanner(System.in);
        boolean finishInputLoop = false;
        do {
            System.out.println("Please enter a description for the new commit");
            commitDescription = scanner.nextLine();
            if (commitDescription.isEmpty())
                System.out.println("Please enter a non-empty description.");
            else {
                finishInputLoop = true;
                Commit newCommit = MagitFileUtils.getFirstCommitFromWC(this.repo, commitDescription);
                try {
                    MagitFileUtils.writeFirstCommitToMagitFolder(this.repo, newCommit);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } while (!finishInputLoop);
    }

    public void testNewCommitAndWCcalc() {
        repo.updateCurrentCommitDatabases();
        repo.TEST_updateWcDatabase();
    }

    /**
     * testing only!!!
     */
    public void printObject() {
        Set<Map.Entry<String, MagitObject>> objects = repo.getObjectsAsEntrySet();
        for (Map.Entry<String, MagitObject> object : objects) {
            System.out.println(object.getKey() + ": " + object.getValue());
        }

        //System.out.println(repo.getObjectsAsMap());

//        Collection<MagitObject> objects  = repo.getObjectsTxtAsCollection();
//        for(MagitObject object : objects)
//            System.out.println(object);
    }

    /**
     * testing only!!!
     */
    public void myAmazingTesting() {
//        File toCheck = new File("C:\\Magit\\word.docx");
//        Blob blobi = new Blob(toCheck);
//        toCheck = new File("C:\\Magit\\aa.txt");
//        Blob blobi2 = new Blob(toCheck);
        repo.test123();
    }




    public boolean isRepoDefined() {
        return this.repo != null;
    }

    public void printCurrentRepoDetails() {
        System.out.print("Working on the Repository: ");
        if (isRepoDefined()) {
            System.out.print(repo.getName() + "\t");
            System.out.println("Located in: " + repo.getStringPath());
        } else
            System.out.println("N\\A");
    }
}


