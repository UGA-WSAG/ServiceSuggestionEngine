package workflowHelp;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;
import org.semanticweb.owlapi.model.OWLClass;
import parser.OntologyManager;
import parser.SchemaParser;

/**
*
* @author Alok Dhamanaskar
* @see LICENSE (MIT style license file).
*
*/
public class GetDocumentation
{
    /**
* The function returns all available documentation for the given parameter in the WSDL.
* It returns Documentation from the WSDL file as well as the Ontology
*
* @param WSDLURL : URL to the WSDL file
* @param paramName : Name of the Parameter from WSDL for which to find documentation
* @param owlURI : The URL of the Ontology file
* @return A list of available documentation including Ontological Definition, Description and
* Usage as well as WSDL Documentation
*/
    
    public static List<String> getParamInfo(String WSDLURL,String paramName, String owlURI)
    {
        List<String> DocList = new ArrayList<String>();
        Element paramElement= null;
        Namespace nameSpace = null;
        Namespace sawsdlNS = Namespace.getNamespace("sawsdl", "http://www.w3.org/ns/sawsdl");
        SchemaParser schemaParser = new SchemaParser();
        String paramIRI="";
        String wsdlDoc = "";
        
        try{
                // loading the Description Document and getting Schema root Element
                List<Element> schemaList = schemaParser.getSchemaElemList(WSDLURL);
                for(Element e : schemaList)
                {
                    nameSpace = e.getNamespace();
                    paramElement = schemaParser.getElementElemOfSchema(paramName, e);
                }

                // Retriving the Documentation From WSDL if Available
                if (paramElement.getChild("annotation", nameSpace)!=null)
                    if (paramElement.getChild("annotation",nameSpace).getChild("documentation",nameSpace) !=null)
                        if(!paramElement.getChild("annotation",nameSpace).getChild("documentation",nameSpace).getText().equals(""))
                                wsdlDoc = "Documentation:" + paramElement.getChild("annotation",nameSpace).getChild("documentation",nameSpace).getText();
                
                // Retriving the Cardinality from WSDL if Available
                org.jdom.Attribute cardAttrMIN = paramElement.getAttribute("minOccurs");
                org.jdom.Attribute cardAttrMAX = paramElement.getAttribute("maxOccurs");
                if (cardAttrMIN!=null)
                    if(Integer.parseInt(cardAttrMIN.getValue()) != 0 )
                        DocList.add("Cardinality Info : You need to enter a minimum of " + cardAttrMIN.getValue() + " of these.");
                if (cardAttrMAX!=null)
                    DocList.add("Cardinality Info : You can enter a maximum of " + cardAttrMAX.getValue() + " of these.");

                DocList.add(wsdlDoc);
                
                // Retriving the Definition, Usage, Description from the Ontology whichever available
                try{
                    org.jdom.Attribute attribute = paramElement.getAttribute("modelReference",sawsdlNS);
                    if(attribute!=null)
                        if (!attribute.getValue().equals(""))
                        {
                            try{
                                    paramIRI = attribute.getValue();
                                    OntologyManager parser = OntologyManager.getInstance(owlURI);
                                    OWLClass conceptClass = parser.getConceptClass(paramIRI);
                                    String OntoDef = parser.getClassDefinition(conceptClass);
                                    String usage = parser.getClassUsage(conceptClass);
                                    String description = parser.getClassDescription(conceptClass);

                                    if (!OntoDef.equals(""))
                                        DocList.add("Definition:" + OntoDef);
                                    if (!usage.equals(""))
                                        DocList.add("Usage:" + usage);
                                    if (!description.equals(""))
                                        DocList.add("Description:" + description);
                                }// try ends
                            catch(Exception e)
                            {
                                System.out.println("Exception occured when getting the Class "
                                        + "in the Ontology for given parameter: " + e);
                            }//Catch ends
                        }// if ends
                }//try ends
                catch(java.lang.NullPointerException e)
                {
                    System.out.println("There was an Error Loading Ontology\n" + e);
                }
            }//Outer try ends
            catch(java.lang.NullPointerException e)
            {
                System.out.println("The Web service document could not be found at the given address\n" + e);
                DocList.add("Unexpected Error Occurred check server log for Details !");
            }
            catch(Exception e)
            {
                System.out.println("Following Exception Occurred: " + e);
                DocList.add("Unexpected Error Occurred check server log for Details !");
            }
            finally
            {
                return DocList;
            }
        
    }// Method Ends
    
    public static void main(String[] args)
    {
        //System.out.println(getParamInfo("http://mango.ctegd.uga.edu/jkissingLab/SWS/Wsannotation/resources/clustalw2.sawsd", "clustering", "/home/alok/Desktop/SuggestionEngine/webService.owl"));
        
        //util.debuggingUtils.printCollection(getParamInfo("http://mango.ctegd.uga.edu/jkissingLab/SWS/Wsannotation/resources/wublast.sawsdl", "sequence", "/home/alok/Desktop/SuggestionEngine/webService.owl"));

        util.DebuggingUtils.printCollection(getParamInfo("http://mango.ctegd.uga.edu/jkissingLab/SWS/Wsannotation/resources/clustalw2.sawsdl", "sequence", "owl/webService.owl"));
        
    }
}