package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import static gitlet.Utils.*;
import java.io.IOException;
import java.util.*;


/** Represents a gitlet repository.
 * This class handles the core operations of the Gitlet version-control system.
 * It includes methods to initialize a repository, stage changes, commit changes,
 * restore files from commits, and print commit history.
 *
 *  @author enmanuel hernandez
 */
public class Repository {

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    /**
     * Allows for persistence to happen
     */
    public static boolean gitlet(String command) {
        if (command.equals("init")) {
            return true;
        } else {
            return checkIfGitletExists();
        }
    }

    /**
     * Check if the .gitlet directory exists in the current working directory.
     *
     * @return true if the .gitlet directory exists, false otherwise.
     */
    public static boolean checkIfGitletExists() {
        File allFiles = Utils.join(GITLET_DIR);
        List<String> s = plainFilenamesIn(allFiles);
        return s != null;
    }

    /**
     * Set up the .gitlet directory structure to allow for version control persistence.
     */
    public static void Persistence() {
        // Create the .gitlet directory
        File gitlet = new File(".gitlet");
        gitlet.mkdir();

        // Create subdirectories for staging, staged file removals, commits, and branches
        File stages = Utils.join(gitlet, "stages");
        stages.mkdir();
        File removal = Utils.join(gitlet, "stageRemoval");
        removal.mkdir();
        File commits = Utils.join(gitlet, "commits");
        commits.mkdir();

        // Create files to store information about commits and the current branch
        File head = Utils.join(gitlet, "head");
        createNewFile(head);

        LinkedList<Commit> list = new LinkedList<>();
        File all = Utils.join(gitlet, "allCommits");
        createNewFile(all);
        Utils.writeObject(all, list);

        File branches = Utils.join(GITLET_DIR, "branches");
        branches.mkdir();
        File nameOfHeadBranch = Utils.join(gitlet, "currentBranch");
        createNewFile(nameOfHeadBranch);
        Utils.writeObject(nameOfHeadBranch, "main");

        File namesEver = Utils.join(GITLET_DIR, "allFileNamesEver");
        createNewFile(namesEver);
        Utils.writeObject(namesEver, new LinkedList<String>());
    }

    /**
     * Create a new file.
     *
     * @param file The file to be created.
     */
    private static void createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException error) {
            System.out.println("Error creating file: " + error.getMessage());
        }
    }


    /**
     * Initialize the Gitlet repository by creating the initial commit.
     */
    public static void Init() {
        Commit initialCommit = createInitialCommit();
        saveCommitToDirectory(initialCommit);
        updateAllCommits(initialCommit);
        updateHead(initialCommit);
        updateCurrentBranch(initialCommit);
    }

    /**
     * Create the initial commit when initializing the Gitlet repository.
     *
     * @return The initial commit object representing the starting state of the repository.
     */
    private static Commit createInitialCommit() {
        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> cwdContents = new ArrayList<>();
        for (String fileName : filesInCWD) {
            cwdContents.add(readContentsAsString(Utils.join(CWD, fileName)));
        }
        Commit initialCommit = new Commit("initial commit", "*main",
                new ArrayList<>(), new ArrayList<>(), filesInCWD, cwdContents);
        initialCommit.setDate(0);
        initialCommit.setTime(initialCommit.getDate());
        initialCommit.setHash(sha1(Utils.serialize(initialCommit)));
        return initialCommit;
    }

    /**
     * Save the commit to the commits directory using its hash code as the filename.
     *
     * @param commit The commit object to be saved.
     */
    private static void saveCommitToDirectory(Commit commit) {
        File commitsDir = Utils.join(GITLET_DIR, "commits");
        File commitFile = Utils.join(commitsDir, commit.getHash());
        Utils.writeObject(commitFile, commit);
    }

    /**
     * Update the list of all commits with the new commit.
     *
     * @param commit The commit object to be added to the list of all commits.
     */
    private static void updateAllCommits(Commit commit) {
        File allCommitsFile = Utils.join(GITLET_DIR, "allCommits");
        LinkedList<Commit> allCommits = Utils.readObject(allCommitsFile, LinkedList.class);
        allCommits.addFirst(commit);
        Utils.writeObject(allCommitsFile, allCommits);
    }

    /**
     * Update the head pointer to point to the new commit.
     *
     * @param commit The commit object to be set as the head.
     */
    private static void updateHead(Commit commit) {
        File head = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> hCommits = new LinkedList<>();
        hCommits.addFirst(commit);
        Utils.writeObject(head, hCommits);
    }

    /**
     * Update the current branch to point to the new commit.
     *
     * @param commit The commit object to be set as the latest commit in the current branch.
     */
    private static void updateCurrentBranch(Commit commit) {
        File currentBranchName = Utils.join(GITLET_DIR, "currentBranch");
        String branchName = Utils.readObject(currentBranchName, String.class);
        File branch = Utils.join(GITLET_DIR, "branches", branchName);
        LinkedList<Commit> branchCommits = new LinkedList<>();
        branchCommits.addFirst(commit);
        Utils.writeObject(branch, branchCommits);
    }

    /**
     * Stage changes for the specified file in the current working directory.
     *
     * @param fileName The name of the file to be staged.
     */
    public static void stage(String fileName) {
        LinkedList<String> allFileNamesEver = readObject(join(GITLET_DIR, "allFileNamesEver"), LinkedList.class);
        List<String> filenames = plainFilenamesIn(join(CWD));

        if (!filenames.contains(fileName)) {
            handleStageRemoval(fileName);
            System.exit(0);
        }

        File heads = join(GITLET_DIR, "branches", readObject(join(GITLET_DIR, "currentBranch"), String.class));
        LinkedList<Commit> headCommits = readObject(heads, LinkedList.class);
        ArrayList<File> curr = headCommits.getFirst().getFileList();
        File cwdFiles = join(CWD, fileName);
        String x = readContentsAsString(cwdFiles);
        File stages = join(GITLET_DIR, "stages");
        File n = join(stages, fileName);
        writeContents(n, x);

        List<String> stagedFiles = plainFilenamesIn(stages);
        List<String> removalStageFiles = plainFilenamesIn(join(GITLET_DIR, "stageRemoval"));

        for (int i = 0; i < curr.size(); i ++) {
            if (stagedFiles.contains(curr.get(i).getName())) {
                String s = headCommits.getFirst().getBlob().get(i);
                File fStage = join(stages, curr.get(i).getName());
                if (s.equals(readContentsAsString(fStage))) {
                    fStage.delete();
                }
                if (removalStageFiles.contains(curr.get(i).getName())) {
                    fStage = join(GITLET_DIR, "stageRemoval", curr.get(i).getName());
                    if (s.equals(readContentsAsString(fStage))) {
                        fStage.delete();
                    }
                }
            }
        }

        if (!allFileNamesEver.contains(fileName)) {
            allFileNamesEver.add(fileName);
        }

        writeObject(join(GITLET_DIR, "allFileNamesEver"), allFileNamesEver);
    }

    private static void handleStageRemoval(String fileName) {
        List<String> removals = plainFilenamesIn(join(GITLET_DIR, "stageRemoval"));
        if (removals.contains(fileName)) {
            File removed = join(GITLET_DIR, "stageRemoval", fileName);
            String contents = readContentsAsString(removed);
            removed.delete();
            File newFile = join(CWD, fileName);
            try {
                newFile.createNewFile();
            } catch (IOException error) {
                System.out.println("Error");
            }
            writeContents(newFile, contents);
            stage(fileName);
        } else {
            System.out.println("File does not exist.");
        }
    }

    /**
     * Stage file removal for the specified file in the current working directory.
     *
     * @param fileName The name of the file to be marked for removal.
     */
    public static void stageRemoval(String fileName) {
        File cwdFile = Utils.join(CWD, fileName);
        if (!cwdFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        File stagesDir = Utils.join(GITLET_DIR, "stageRemoval");
        Utils.writeContents(Utils.join(stagesDir, fileName), readContentsAsString(cwdFile));
    }
    /**
     * Stage file removal for the specified file in a commit.
     *
     * @param fileName The name of the file to be marked for removal in the next commit.
     */
    public static void stageRemovalCommit(String fileName) {
        File cwdFile = Utils.join(CWD, fileName);
        if (!cwdFile.exists()) {
            return;
        }

        File stagesDir = Utils.join(GITLET_DIR, "stageRemoval");
        File stagedFile = Utils.join(stagesDir, fileName);
        Utils.writeContents(stagedFile, readContentsAsString(cwdFile));
    }

    /**
     * Commit the staged changes and create a new commit object.
     *
     * @param message The commit message provided by the user.
     */
    public static void commit(String message) {
        File stagedDir = Utils.join(GITLET_DIR, "stages");
        File stageRemovalDir = Utils.join(GITLET_DIR, "stageRemoval");
        File head = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> headCommits = Utils.readObject(head, LinkedList.class);
        Commit headCommit = headCommits.getFirst();

        List<String> stagedFiles = Utils.plainFilenamesIn(stagedDir);
        List<String> removalFiles = Utils.plainFilenamesIn(stageRemovalDir);

        if (stagedFiles.isEmpty() && removalFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        ArrayList<File> filesToCommit = new ArrayList<>();
        ArrayList<String> blobsToCommit = new ArrayList<>();

        for (String fileName : stagedFiles) {
            File stagedFile = Utils.join(stagedDir, fileName);
            filesToCommit.add(stagedFile);
            blobsToCommit.add(readContentsAsString(stagedFile));
            stagedFile.delete();
        }

        for (String fileName : removalFiles) {
            File removalFile = Utils.join(stageRemovalDir, fileName);
            File cwdFile = Utils.join(CWD, fileName);
            if (cwdFile.exists()) {
                cwdFile.delete();
            }
            removalFile.delete();
        }

        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> cwdContents = new ArrayList<>();
        for (String fileName : filesInCWD) {
            File cwdFile = Utils.join(CWD, fileName);
            cwdContents.add(readContentsAsString(cwdFile));
        }

        Commit newCommit = new Commit(message, "*main", filesToCommit,
                blobsToCommit, filesInCWD, cwdContents);
        newCommit.setHash(Utils.sha1(Utils.serialize(newCommit)));

        saveCommitToDirectory(newCommit);
        updateAllCommits(newCommit);
        headCommits.addFirst(newCommit);
        Utils.writeObject(head, headCommits);

        String branchName = Utils.readObject(Utils.join(GITLET_DIR, "currentBranch"), String.class);
        File branch = Utils.join(GITLET_DIR, "branches", branchName);
        LinkedList<Commit> branchCommits = Utils.readObject(branch, LinkedList.class);
        branchCommits.addFirst(newCommit);
        Utils.writeObject(branch, branchCommits);
    }

    /**
     * Restore the specified file from the latest commit to the current working directory.
     *
     * @param fileName The name of the file to be restored.
     */
    public static void restoreFile(String fileName) {
        File file = join(CWD, fileName);
        File head = join(GITLET_DIR, "head");
        LinkedList<Commit> commits = readObject(head, LinkedList.class);
        Commit latestCommit = commits.getFirst();

        for (int i = 0; i < latestCommit.getFileList().size(); i++) {
            File commitFile = latestCommit.getFileList().get(i);
            if (commitFile.getName().equals(fileName)) {
                writeContents(file, latestCommit.getBlob().get(i));
                return;
            }
        }

        System.out.println("File does not exist in the latest commit.");
        System.exit(0);
    }


    /**
     * Restore the specified file from a specific commit to the current working directory.
     *
     * @param commitID The ID of the commit containing the file to be restored.
     * @param fileName The name of the file to be restored.
     */
    public static void restoreCommit(String commitID, String fileName) {
        File commitDir = Utils.join(GITLET_DIR, "commits");
        List<String> commitIDs = plainFilenamesIn(commitDir);

        for (int i = 0; i < commitIDs.size(); i++) {
            if (commitID.length() >= 7) {
                String shortID = readObject(join(commitDir, commitIDs.get(i)), Commit.class)
                        .getHash().substring(0, 6);
                if (commitIDs.get(i).equals(commitID) || commitID.substring(0, 6).equals(shortID)) {
                    commitID = readObject(join(commitDir, commitIDs.get(i)), Commit.class).getHash();
                    break;
                }
            }
            if (i == commitIDs.size() - 1) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
        }

        File file1 = Utils.join(commitDir, commitID);
        Commit commit = Utils.readObject(file1, Commit.class);
        ArrayList<File> commitFileList = commit.getFileList();
        boolean fileFound = false;

        for (File file : commitFileList) {
            if (file.getName().compareTo(fileName) == 0) {
                File curr = Utils.join(CWD, fileName);
                Utils.writeContents(curr, commit.getBlob().get(commitFileList.indexOf(file)));
                fileFound = true;
                break;
            }
        }

        if (!fileFound) {
            List<String> cwdFiles = plainFilenamesIn(CWD);
            for (int i = 0; i < commitFileList.size(); i++) {
                File currentFile = commitFileList.get(i);
                if (!cwdFiles.contains(currentFile.getName())) {
                    File retrieve = Utils.join(CWD, fileName);
                    try {
                        retrieve.createNewFile();
                        Utils.writeContents(retrieve, commit.getBlob().get(i));
                    } catch (IOException error) {
                        System.out.println("Error creating the file.");
                    }
                }
            }
        }

        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (!fileFound && !cwdFiles.contains(fileName)) {
            System.out.println("File does not exist in that commit.");
        }
    }

    /**
     * removes the file from the staging area if it was marked for addition.
     * If the file is tracked in the current commit, it marks it to be removed in the next commit (staged removal).
     */
    public static void rm(String fileName) {
        File currentCommitFile = join(GITLET_DIR, "branches", readObject(
                join(GITLET_DIR, "currentBranch"), String.class));
        LinkedList<Commit> commitList = readObject(currentCommitFile, LinkedList.class);
        Commit headCommit = commitList.getFirst();
        ArrayList<File> currentFiles = headCommit.getFileList();
        List<String> removalFiles = plainFilenamesIn(join(GITLET_DIR, "stageRemoval"));
        List<String> cwdFiles = plainFilenamesIn(CWD);
        boolean inHead = currentFiles.stream().anyMatch(file -> file.getName().equals(fileName));

        if (inHead) {
            if (!removalFiles.contains(fileName) && !cwdFiles.contains(fileName)) {
                File toAdd = join(join(GITLET_DIR, "stageRemoval"), fileName);
                writeObject(toAdd, currentFiles.stream()
                        .filter(file -> file.getName().equals(fileName))
                        .findFirst().orElse(null));
                return;
            }
            stageRemoval(fileName);
            restrictedDelete(fileName);
        } else {
            File staged = join(GITLET_DIR, "stages");
            File fileToDelete = join(staged, fileName);
            if (fileToDelete.delete()) {
                return;
            }
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }


    /**
     * Display the log of commits, showing commit details in chronological order.
     */
    public static void log() {
        LinkedList<Commit> listCommits = readCommitsFromHead();

        for (Commit c : listCommits) {
            printCommitDetails(c);
        }
    }

    private static LinkedList<Commit> readCommitsFromHead() {
        File coms = Utils.join(GITLET_DIR, "head");
        return Utils.readObject(coms, LinkedList.class);
    }

    private static void printCommitDetails(Commit c) {
        System.out.println("===");
        System.out.println("commit " + c.getHash());
        System.out.println("Date: " + c.getTime());
        System.out.println(c.getMessage() + "\n");
    }

    /**
     * displays the log of all commits in the repository, showing commit details in chronological order.
     */
    public static void globalLog() {
        List<Commit> listCommits = readObject(join(GITLET_DIR, "allCommits"), LinkedList.class);
        for (Commit c : listCommits) {
            System.out.println("===");
            System.out.println("commit " + c.getHash());
            System.out.println("Date: " + c.getTime());
            System.out.println(c.getMessage() + "\n");
        }
    }

    /**
     * displays the status of the repository, showing branch information and the staged and removed files.
     */
    public static void status() {
        System.out.println("=== Branches ===");
        String currentBranchName = Utils.readObject(Utils.join(GITLET_DIR, "currentBranch"), String.class);
        List<String> branches = Utils.plainFilenamesIn(Utils.join(GITLET_DIR, "branches"));
        for (String branch : branches) {
            System.out.println(branch.equals(currentBranchName) ? "*" + branch : branch);
        }

        System.out.print("\n");
        System.out.println("=== Staged Files ===");
        List<String> staged = Utils.plainFilenamesIn(Utils.join(GITLET_DIR, "stages"));
        for (String file : staged) {
            System.out.println(file);
        }

        System.out.print("\n");
        System.out.println("=== Removed Files ===");
        List<String> stagedRemoval = Utils.plainFilenamesIn(Utils.join(GITLET_DIR, "stageRemoval"));
        for (String file : stagedRemoval) {
            System.out.println(file);
        }

        System.out.print("\n");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.print("\n");
        System.out.println("=== Untracked Files ===");
        System.out.print("\n");
    }

    /**
     * Creates a new branch with the given name, pointing to the current commit.
     *
     * @param name The name of the new branch to be created.
     */
    public static void branch(String name) {
        File branches = Utils.join(GITLET_DIR, "branches");
        List<String> branchNames = plainFilenamesIn(branches);

        if (branchNames.contains(name)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        File currentBranchFile = Utils.join(GITLET_DIR, "currentBranch");
        String currentBranchName = Utils.readObject(currentBranchFile, String.class);
        File currentBranch = Utils.join(branches, currentBranchName);

        File newBranch = Utils.join(branches, name);
        LinkedList<Commit> commits = Utils.readObject(currentBranch, LinkedList.class);
        Utils.writeObject(newBranch, commits);
    }

    /**
     * Finds and prints the commit hash for all commits with the given commit message.
     *
     * @param commitMsg The commit message to be searched for.
     */
    public static void find(String commitMsg) {
        File allCommitsFile = Utils.join(GITLET_DIR, "allCommits");
        LinkedList<Commit> allCommits = readObject(allCommitsFile, LinkedList.class);
        int count = 0;

        for (Commit commit : allCommits) {
            if (commit.getMessage().equals(commitMsg)) {
                System.out.println(commit.getHash());
                count++;
            }
        }

        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Resets the current branch and working directory to the specified commit's state.
     *
     * @param ID The ID of the commit to reset to.
     */
    public static void reset(String ID) {
        ID = findFullID(ID);
        Commit comm = readObject(Utils.join(GITLET_DIR, "commits", ID), Commit.class);

        /*Check if there are any untracked files to be commited*/
        Set<String> filesInCommit = new HashSet<>(comm.getCWD());

        File heads = Utils.join(GITLET_DIR, "head");
        LinkedList<Commit> head = readObject(heads, LinkedList.class);
        List<String> stringList = plainFilenamesIn(CWD);

        for (String fileName : stringList) {
            if (!head.get(0).getCWD().contains(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        for (int i = 0; i < stringList.size(); i += 1) {
            if (!head.get(0).getCWD().contains(stringList.get(i))) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        LinkedList<String> contents = new LinkedList<>();

        for (int i = 0; i < head.get(0).getCWD().size(); i += 1) {
            contents.add(head.get(0).cwdCons().get(i));
        }
        LinkedList<String> allFileNamesEver = readObject(Utils.join
                (GITLET_DIR, "allFileNamesEver"), LinkedList.class);

        for (String fileName : stringList) {
            if (filesInCommit.contains(fileName)) {
                String currentContents = readContentsAsString(Utils.join(CWD, fileName));
                if (!contents.contains(currentContents)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        List<String> stage = plainFilenamesIn(Utils.join(GITLET_DIR, "stages"));
        if (stage.size() > 0) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        for (int i = 0; i < stringList.size(); i += 1) {
            if (comm.getCWD().contains(stringList.get(i))) {
                restoreCommit(ID, stringList.get(i));
            } else {
                Utils.join(CWD, stringList.get(i)).delete();
            }
        }
        LinkedList<Commit> newH = new LinkedList<>();
        for (int i = 0; i < head.size(); i += 1) {
            if (head.get(i).getHash().equals(ID)) {
                newH.addFirst(head.get(i));
                break;
            }
            newH.addFirst(head.get(i));
        }
        Utils.writeObject(heads, newH);
    }

    /**
     * Helper function to find the full commit ID from a shortened commit ID (shortID).
     *
     * @param shortID The shortened commit ID.
     * @return The full commit ID if found, or null if no matching commit is found.
     */
    public static String findFullID(String shortID) {
        File c = Utils.join(GITLET_DIR, "commits");
        List<String> filenames = plainFilenamesIn(c);

        for (String filename : filenames) {
            Commit commit = readObject(join(c, filename), Commit.class);
            String commitShortID = commit.getHash().substring(0, 6);

            if (filename.equals(shortID) || shortID.equals(commitShortID)) {
                return commit.getHash();
            }
        }

        System.out.println("No commit with that id exists.");
        System.exit(0);
        return null; // This line is unreachable, but needed to satisfy the compiler.
    }

    /**
     * Switches to the specified branch by updating the head pointer and restoring the working directory
     * to the state of the most recent commit in the branch.
     *
     * @param name The name of the branch to switch to.
     */
    public static void switchBranch(String name) {
        File branches = join(GITLET_DIR, "branches");
        File currentBranchName = join(GITLET_DIR, "currentBranch");
        File head = join(GITLET_DIR, "head");

        String currentBranch = readObject(currentBranchName, String.class);
        LinkedList<Commit> headCommit = readObject(head, LinkedList.class);

        List<String> l = plainFilenamesIn(branches);
        if (!l.contains(name)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        if (name.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        LinkedList<Commit> commitsInBranch = readObject(join(branches, name), LinkedList.class);
        List<String> currentWD = plainFilenamesIn(CWD);

        if (currentWD.size() != headCommit.get(0).getCWD().size()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        for (int i = 0; i < currentWD.size(); i += 1) {
            if (!headCommit.get(0).getCWD().contains(currentWD.get(i))) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        LinkedList<String> headContents = new LinkedList<>();
        for (int i = 0; i < headCommit.get(0).getCWD().size(); i += 1) {
            headContents.add(headCommit.get(0).cwdCons().get(i));
        }

        LinkedList<String> allFileNamesEver = readObject(join(GITLET_DIR, "allFileNamesEver"), LinkedList.class);
        for (int i = 0; i < plainFilenamesIn(CWD).size(); i += 1) {
            if (!allFileNamesEver.contains(plainFilenamesIn(CWD).get(i))) {
                continue;
            }
            String currentWDContents = readContentsAsString(join(CWD, plainFilenamesIn(CWD).get(i)));
            if (!headContents.contains(currentWDContents)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        List<String> stage = plainFilenamesIn(join(GITLET_DIR, "stages"));
        if (stage.size() > 0) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        // Perform the actual switch once all conditions are met
        for (int i = 0; i < commitsInBranch.get(0).getFileList().size(); i += 1) {
            if (currentWD.contains(commitsInBranch.get(0).getFileList().get(i).getName())) {
                writeContents(join(CWD, commitsInBranch.get(0).getFileList().get(i).getName()), commitsInBranch.get(0).getBlob().get(i));
            } else {
                String oldFileNotInCWDName = commitsInBranch.get(0).getFileList().get(i).getName();
                File oldFileNotInCWD = join(CWD, oldFileNotInCWDName);
                try {
                    oldFileNotInCWD.createNewFile();
                } catch (IOException error) {
                    System.out.println("Error");
                }
                writeContents(oldFileNotInCWD, commitsInBranch.get(0).getBlob().get(i));
            }
        }

        List<String> newWD = commitsInBranch.get(0).getCWD();
        for (int i = 0; i < currentWD.size(); i += 1) {
            if (!newWD.contains(currentWD.get(i))) {
                join(CWD, currentWD.get(i)).delete();
            }
        }

        join(GITLET_DIR, "stages").delete();
        join(GITLET_DIR, "stages").mkdir();
        join(GITLET_DIR, "stageRemoval").delete();
        join(GITLET_DIR, "stageRemoval").mkdir();
        writeObject(currentBranchName, name);
        writeObject(head, commitsInBranch);
    }

    /**
     * Removes the specified branch if it exists. The current branch cannot be removed.
     *
     * @param name The name of the branch to be removed.
     */
    public static void rmBranch(String name) {
        File branches = Utils.join(GITLET_DIR, "branches");
        File branchToDelete = Utils.join(branches, name);
        String currentBranchName = Utils.readObject(Utils.join(GITLET_DIR, "currentBranch"), String.class);

        if (!branchToDelete.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (name.equals(currentBranchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branchToDelete.delete();
        }
    }


}
