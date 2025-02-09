
package engine.xml.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the engine.xml package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Head_QNAME = new QName("", "head");
    private final static QName _Creator_QNAME = new QName("", "creator");
    private final static QName _Author_QNAME = new QName("", "author");
    private final static QName _LastUpdater_QNAME = new QName("", "last-updater");
    private final static QName _Name_QNAME = new QName("", "name");
    private final static QName _DateOfCreation_QNAME = new QName("", "date-of-creation");
    private final static QName _CreationDate_QNAME = new QName("", "creation-date");
    private final static QName _LastUpdateDate_QNAME = new QName("", "last-update-date");
    private final static QName _Location_QNAME = new QName("", "location");
    private final static QName _Message_QNAME = new QName("", "message");
    private final static QName _Content_QNAME = new QName("", "content");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: engine.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PrecedingCommits }
     * 
     */
    public PrecedingCommits createPrecedingCommits() {
        return new PrecedingCommits();
    }

    /**
     * Create an instance of {@link MagitSingleBranch }
     * 
     */
    public MagitSingleBranch createMagitSingleBranch() {
        return new MagitSingleBranch();
    }

    /**
     * Create an instance of {@link MagitSingleFolder }
     * 
     */
    public MagitSingleFolder createMagitSingleFolder() {
        return new MagitSingleFolder();
    }

    /**
     * Create an instance of {@link MagitRepository }
     * 
     */
    public MagitRepository createMagitRepository() {
        return new MagitRepository();
    }

    /**
     * Create an instance of {@link RootFolder }
     * 
     */
    public RootFolder createRootFolder() {
        return new RootFolder();
    }

    /**
     * Create an instance of {@link Item }
     * 
     */
    public Item createItem() {
        return new Item();
    }

    /**
     * Create an instance of {@link PrecedingCommits.PrecedingCommit }
     * 
     */
    public PrecedingCommits.PrecedingCommit createPrecedingCommitsPrecedingCommit() {
        return new PrecedingCommits.PrecedingCommit();
    }

    /**
     * Create an instance of {@link MagitBlob }
     * 
     */
    public MagitBlob createMagitBlob() {
        return new MagitBlob();
    }

    /**
     * Create an instance of {@link MagitCommits }
     * 
     */
    public MagitCommits createMagitCommits() {
        return new MagitCommits();
    }

    /**
     * Create an instance of {@link MagitSingleCommit }
     * 
     */
    public MagitSingleCommit createMagitSingleCommit() {
        return new MagitSingleCommit();
    }

    /**
     * Create an instance of {@link MagitSingleBranch.PointedCommit }
     * 
     */
    public MagitSingleBranch.PointedCommit createMagitSingleBranchPointedCommit() {
        return new MagitSingleBranch.PointedCommit();
    }

    /**
     * Create an instance of {@link MagitBlobs }
     * 
     */
    public MagitBlobs createMagitBlobs() {
        return new MagitBlobs();
    }

    /**
     * Create an instance of {@link MagitSingleFolder.Items }
     * 
     */
    public MagitSingleFolder.Items createMagitSingleFolderItems() {
        return new MagitSingleFolder.Items();
    }

    /**
     * Create an instance of {@link MagitBranches }
     * 
     */
    public MagitBranches createMagitBranches() {
        return new MagitBranches();
    }

    /**
     * Create an instance of {@link MagitFolders }
     * 
     */
    public MagitFolders createMagitFolders() {
        return new MagitFolders();
    }

    /**
     * Create an instance of {@link MagitRepository.MagitRemoteReference }
     * 
     */
    public MagitRepository.MagitRemoteReference createMagitRepositoryMagitRemoteReference() {
        return new MagitRepository.MagitRemoteReference();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "head")
    public JAXBElement<String> createHead(String value) {
        return new JAXBElement<String>(_Head_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "creator")
    public JAXBElement<String> createCreator(String value) {
        return new JAXBElement<String>(_Creator_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "author")
    public JAXBElement<String> createAuthor(String value) {
        return new JAXBElement<String>(_Author_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "last-updater")
    public JAXBElement<String> createLastUpdater(String value) {
        return new JAXBElement<String>(_LastUpdater_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "date-of-creation")
    public JAXBElement<String> createDateOfCreation(String value) {
        return new JAXBElement<String>(_DateOfCreation_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "creation-date")
    public JAXBElement<String> createCreationDate(String value) {
        return new JAXBElement<String>(_CreationDate_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "last-update-date")
    public JAXBElement<String> createLastUpdateDate(String value) {
        return new JAXBElement<String>(_LastUpdateDate_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "location")
    public JAXBElement<String> createLocation(String value) {
        return new JAXBElement<String>(_Location_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "message")
    public JAXBElement<String> createMessage(String value) {
        return new JAXBElement<String>(_Message_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "content")
    public JAXBElement<String> createContent(String value) {
        return new JAXBElement<String>(_Content_QNAME, String.class, null, value);
    }

}
