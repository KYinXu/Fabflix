package ETLPipeline;

import ETLPipeline.types.ETLException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;

/**
 * SAX-based parser that streams XML records into simple map structures.
 * Designed to be reused by {@link XMLDataParser} for modular parsing.
 */
public class SAXRecordParser {

    /**
     * Parse the given XML file using SAX and return record maps.
     * @param xmlPath Path to XML file
     * @param metadata Inferred XML structure metadata
     * @param resolver Entity resolver for local DTD handling
     * @param errorHandler Strict error handler to bubble validation failures
     * @return List of records represented as maps
     */
    public List<Object> parse(Path xmlPath,
                              XMLDataParser.RowTagMetadata metadata,
                              EntityResolver resolver,
                              ErrorHandler errorHandler,
                              List<String> issues) {
        try (InputStream stream = Files.newInputStream(xmlPath)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            factory.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setEntityResolver(resolver);
            reader.setErrorHandler(errorHandler);

            RecordHandler handler = new RecordHandler(metadata.rowTag(), issues);
            reader.setContentHandler(handler);
            reader.parse(new InputSource(stream));

            return new ArrayList<>(handler.getRecords());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ETLException("SAX parsing failed for " + xmlPath, e);
        }
    }

    private static class RecordHandler extends DefaultHandler {
        private final String rowTag;
        private final List<Object> records = new ArrayList<>();
        private final Deque<ElementContext> contextStack = new ArrayDeque<>();
        private final List<String> issues;

        private boolean inRecord = false;

        RecordHandler(String rowTag, List<String> issues) {
            this.rowTag = rowTag;
            this.issues = issues;
        }

        List<Object> getRecords() {
            return records;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            String elementName = !"".equals(localName) ? localName : qName;

            if (!inRecord) {
                if (rowTag.equals(elementName)) {
                    inRecord = true;
                } else {
                    return;
                }
            }

            ElementContext context = new ElementContext(elementName);
            contextStack.push(context);

            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrName = attributes.getLocalName(i);
                    if (attrName == null || attrName.isBlank()) {
                        attrName = attributes.getQName(i);
                    }
                    context.addAttribute(attrName, attributes.getValue(i));
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (!inRecord || contextStack.isEmpty()) {
                return;
            }
            contextStack.peek().appendText(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!inRecord || contextStack.isEmpty()) {
                return;
            }

            String elementName = !"".equals(localName) ? localName : qName;
            ElementContext context = contextStack.pop();

            if (!context.name.equals(elementName)) {
                issues.add("Mismatched element: expected '" + context.name + "' but found '" + elementName + "'.");
            }

            Object value = context.buildValue();

            if (contextStack.isEmpty()) {
                records.add(value);
                inRecord = false;
            } else {
                ElementContext parent = contextStack.peek();
                parent.addChild(context.name, value);
            }
        }

        private static class ElementContext {
            private final String name;
            private final Map<String, Object> children = new LinkedHashMap<>();
            private final Map<String, String> attributes = new LinkedHashMap<>();
            private final StringBuilder text = new StringBuilder();

            ElementContext(String name) {
                this.name = name;
            }

            void addAttribute(String attributeName, String value) {
                if (value != null) {
                    attributes.put(attributeName, value);
                }
            }

            void appendText(char[] ch, int start, int length) {
                text.append(ch, start, length);
            }

            void addChild(String childName, Object value) {
                if (value == null) {
                    if (!children.containsKey(childName)) {
                        children.put(childName, null);
                    }
                    return;
                }

                Object existing = children.get(childName);
                if (existing == null) {
                    children.put(childName, value);
                } else if (existing instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) existing;
                    list.add(value);
                } else {
                    List<Object> list = new ArrayList<>();
                    list.add(existing);
                    list.add(value);
                    children.put(childName, list);
                }
            }

            Object buildValue() {
                String trimmedText = text.toString().trim();
                boolean hasChildren = !children.isEmpty();
                boolean hasAttributes = !attributes.isEmpty();

                if (!hasChildren && !hasAttributes) {
                    return trimmedText.isEmpty() ? null : trimmedText;
                }

                Map<String, Object> node = new LinkedHashMap<>();
                if (hasAttributes) {
                    attributes.forEach((key, value) -> node.put("@" + key, value));
                }
                if (hasChildren) {
                    node.putAll(children);
                }
                if (!trimmedText.isEmpty()) {
                    node.put("$text", trimmedText);
                }
                return node;
            }
        }
    }
}

