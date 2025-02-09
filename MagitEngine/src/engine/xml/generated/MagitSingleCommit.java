
package engine.xml.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}root-folder"/>
 *         &lt;element ref="{}message"/>
 *         &lt;element ref="{}author"/>
 *         &lt;element ref="{}date-of-creation"/>
 *         &lt;element ref="{}preceding-commits" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "rootFolder",
    "message",
    "author",
    "dateOfCreation",
    "precedingCommits"
})
@XmlRootElement(name = "MagitSingleCommit")
public class MagitSingleCommit {

    @XmlElement(name = "root-folder", required = true)
    protected RootFolder rootFolder;
    @XmlElement(required = true)
    protected String message;
    @XmlElement(required = true)
    protected String author;
    @XmlElement(name = "date-of-creation", required = true)
    protected String dateOfCreation;
    @XmlElement(name = "preceding-commits")
    protected PrecedingCommits precedingCommits;
    @XmlAttribute(name = "id", required = true)
    protected String id;

    /**
     * Gets the value of the rootFolder property.
     * 
     * @return
     *     possible object is
     *     {@link RootFolder }
     *     
     */
    public RootFolder getRootFolder() {
        return rootFolder;
    }

    /**
     * Sets the value of the rootFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link RootFolder }
     *     
     */
    public void setRootFolder(RootFolder value) {
        this.rootFolder = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the dateOfCreation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateOfCreation() {
        return dateOfCreation;
    }

    /**
     * Sets the value of the dateOfCreation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateOfCreation(String value) {
        this.dateOfCreation = value;
    }

    /**
     * Gets the value of the precedingCommits property.
     * 
     * @return
     *     possible object is
     *     {@link PrecedingCommits }
     *     
     */
    public PrecedingCommits getPrecedingCommits() {
        return precedingCommits;
    }

    /**
     * Sets the value of the precedingCommits property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrecedingCommits }
     *     
     */
    public void setPrecedingCommits(PrecedingCommits value) {
        this.precedingCommits = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
