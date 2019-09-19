import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import xmlClasses.*;

public class XmlRepoImport {


        private final static String JAXB_XML_GAME_PACKAGE_NAME = "xmlClasses";

        public static void main(String[] args) throws IOException {
            //InputStream inputStream = XmlRepoImport.class.getResourceAsStream("C:\\Magit\\ex1-medium.xml");
            InputStream inputStream = FileUtils.openInputStream(FileUtils.getFile("C:\\Magit\\ex1-medium.xml"));
            try {
                MagitRepository repo = deserializeFrom(inputStream);
                //System.out.println("name of first country is: " + countries.getCountry().get(0).getName());
                System.out.println("aaa");
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        private static MagitRepository deserializeFrom(InputStream in) throws JAXBException {
            JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GAME_PACKAGE_NAME);
            Unmarshaller u = jc.createUnmarshaller();
            return (MagitRepository) u.unmarshal(in);
        }



}
