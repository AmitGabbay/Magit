
package engine.xml.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{}MagitBlob" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "magitBlob"
})
@XmlRootElement(name = "MagitBlobs")
public class MagitBlobs {

    @XmlElement(name = "MagitBlob")
    protected List<MagitBlob> magitBlob;

    /**
     * Gets the value of the magitBlob property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the magitBlob property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMagitBlob().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MagitBlob }
     * 
     * 
     */
    public List<MagitBlob> getMagitBlob() {
        if (magitBlob == null) {
            magitBlob = new ArrayList<MagitBlob>();
        }
        return this.magitBlob;
    }

}
