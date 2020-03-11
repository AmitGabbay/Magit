# Magit


This system demonstrates a very simple git implementation with basic functions like:

- Load repository from an XML file (to the system UI and the local disk)
- Load repository from disk
- Show current commit files + metadata (file/folder hash, last modified time, last editor)
- Show status (open changes from the current commit)
- Commit
- Branches basic functionality: create, delete, checkout, show all

#### Examples:

Main menu:

![image](https://i.ibb.co/KhmkWrq/mainMenu.jpg)

Branches menu:

![image](https://i.ibb.co/nmyjmmF/branches-Info.jpg)


-----
-----


# Download

download from here... dependencies included (add details)

```
Give examples
```

------------------
-----------------------------------------------

# System Design

Magit stores files (blobs), folders and commits in objects files. Each object is identified by a unique SHA-1 hash, which is calculated based on the object's type.

**Blob** - Represent a text file, and contains its content. The SHA-1 is calculated on this content.

**Folder** - Contains a text description for each file/folder that is stored inside it directly: name, sha1 identifier, last modifier and last modified time. The SHA-1 is calculated on the concatenation of those descriptions.

**Commit** - Contains a root folder SHA-1, parent commit SHA-1, description, creation time and author. The SHA-1 is calculated on the string concatenation of those properties.


Objects are stored in memory as nearly as possible to the format they are saved in files. Few additional fields that required for system operation marked as transient and recovered in runtime when loading a repository from folder or XML.


----

### File system structure

Similarly to Git, any repository that is managed by the Magit system contains a folder named .magit, which stores the data created by the system. The .magit folder includes:

**branches folder** - Each branch is represented by a file contains the SHA-1 hash of its pointed commit, and a HEAD file contains the active branch name.

**objects folder** - Contains all repository files, folders, and commits data, Gzipped and named by their SHA-1 hash 

**RepoSettings file** - Includes repository name, disk path, and head branch. Saved in binary format. 

----

### XML structure

Supported XML file structure be found at the [magit.xsd](MagitEngine/src/engine/xml/generated/MAGit.xsd) file. 
A sample XML file with repository data is available [here](https://drive.google.com/file/d/13zcj2-dSHvATMPJJqfi306SDgBi7V2Jm/view?usp=sharing).

----

### Major Classes by modules

**[MagitConsole][MagitConsole] – Manages the console runtime and all end-user I\O:**
- *Main Menu* – The main method (PSVM) is inside it, manages the system runtime and menu functionality.

- *MenuItem*  – Enum class that manages the menu items.

- *Magit* - The most significant class in the module, has methods that responsible for any operation the user selects in the main menu. Uses the Repository and MagitFilesUtils classes from the MagitEngine module

**[MagitEngine][MagitEngine] – Main logic engine, processes all user command and operation:**
- *Repository* – The main class in the system, responsible for almost any operation on an existing repository, and for creating a new one and loading a one from folder

- Nested class *RepoFileUtils* – Responsible for any operation on the filesystem

- *MagitFileUtils* – Contains static methods responsible for file system operation that happens before Repository instance creation (or loading from file\xml)

- *XmlRepoImporter* – Responsible for creating repository from xml file. Uses methods from Repository and MagitFilesUtils classes

- *Xml.generated package* – Classes that were generated from the given Magit xml schema file. When loading an xml – a repository representation using this classes is generated and the XmlRepoImporter class converts it to my Magit repository representation.

- *Blob, MagitFolder, Commit and Branch* – represents those git object types

- *Sha1Able* – interface implemented by Blob, MagitFolder and Commit classes, requires implementing the calcSha1() function

- *MagitObject* – parent class for Blob and MagitFolder


-----
-----
# Known issues
- Xml loading of empty repository throws an exception and backs to the main menu
- Loading of a repository from folder with no commits is not allowed (normal behavior - throws an exception and backs to the main menu)

-----
-----
# For more information
Please contact me by mail:

amit.gabbay1@gmail.com

---
---


[Repository]: repo.xml "Repository"
[MagitEngine]: MagitEngine/src/engine "MagitEngine"
[MagitConsole]: ConsoleUI/src/consoleUI "MagitConsole"
