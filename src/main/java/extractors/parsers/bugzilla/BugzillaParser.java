package extractors.parsers.bugzilla;

import extractors.parsers.bugzilla.entity.BugCommentInfo;
import extractors.parsers.bugzilla.entity.BugInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by xiaohan on 2017/4/4.
 */
public class BugzillaParser {
    public static BugInfo getBugInfo(String fileName) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        BugInfo bug = new BugInfo();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(fileName);

            // <bugzilla>
            Element rootElement = document.getDocumentElement();
            NodeList nodeList = rootElement.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    // <bug>
                    Element nodeBug = (Element) node;
                    NodeList bugChildren = nodeBug.getChildNodes();

                    for (int j = 0; j < bugChildren.getLength(); j++) {
                        Node nodeChild = bugChildren.item(j);
                        if (nodeChild.getNodeType() == Node.ELEMENT_NODE) {
                            if (nodeChild.getNodeName().equals("bug_id")) {
                                bug.setBugId(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("creation_ts")) {
                                bug.setCreationTs(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("short_desc")) {
                                bug.setShortDesc(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("delta_ts")) {
                                bug.setDeltaTs(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("classification")) {
                                bug.setClassification(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("product")) {
                                bug.setProduct(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("component")) {
                                bug.setComponent(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("version")) {
                                bug.setVersion(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("rep_platform")) {
                                bug.setRepPlatform(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("op_sys")) {
                                bug.setOpSys(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("bug_status")) {
                                bug.setBugStatus(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("resolution")) {
                                bug.setResolution(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("priority")) {
                                bug.setPriority(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("bug_severity")) {
                                bug.setBugSeverity(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("reporter")) {
                                Element element = (Element) nodeChild;
                                bug.setReporterName(element.getAttribute("name"));
                                bug.setReporter(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("assigned_to")) {
                                Element element = (Element) nodeChild;
                                bug.setAssignedToName(element.getAttribute("name"));
                                bug.setAssignedTo(nodeChild.getTextContent());
                            } else if (nodeChild.getNodeName().equals("long_desc")) {

                                BugCommentInfo comment = new BugCommentInfo();
                                NodeList commentChildren = nodeChild.getChildNodes();
                                for (int k = 0; k < commentChildren.getLength(); k++) {
                                    Node commentChild = commentChildren.item(k);
                                    if (commentChild.getNodeType() == Node.ELEMENT_NODE) {
                                        if (commentChild.getNodeName().equals("commentid")) {
                                            comment.setCommentId(commentChild.getTextContent());
                                        } else if (commentChild.getNodeName().equals("who")) {
                                            Element element = (Element) commentChild;
                                            comment.setWhoName(element.getAttribute("name"));
                                            comment.setWho(commentChild.getTextContent());
                                        } else if (commentChild.getNodeName().equals("bug_when")) {
                                            comment.setBugWhen(commentChild.getTextContent());
                                        } else if (commentChild.getNodeName().equals("thetext")) {
                                            comment.setThetext(commentChild.getTextContent());
                                        }
                                    }
                                }

                                bug.addComment(comment);
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return bug;
    }
}
