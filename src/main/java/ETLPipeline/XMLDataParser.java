package ETLPipeline;

import ETLPipeline.types.ETLConfig;
import ETLPipeline.types.ETLException;
import ETLPipeline.types.RawData;
import ETLPipeline.types.XMLChunk;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLDataParser implements AutoCloseable {
    
    private final SAXRecordParser saxRecordParser;

    public XMLDataParser() {
        this.saxRecordParser = new SAXRecordParser();
    }
    
    /**
     * Parse XML file and return raw data
     * @param xmlFilePath Path to XML file
     * @param config ETL configuration
     * @return RawData containing parsed records
     */
    public RawData parse(String xmlFilePath, ETLConfig config) {
        Path xmlPath = Paths.get(xmlFilePath);
        if (!Files.exists(xmlPath)) {
            throw new ETLException("XML file not found: " + xmlFilePath);
        }
        RowTagMetadata metadata = resolveStructure(xmlPath, config);
        List<String> issues = new ArrayList<>();
        List<Object> records = saxRecordParser.parse(
            xmlPath,
            metadata,
            new LocalDtdResolver(xmlPath, issues),
            new LenientErrorHandler(xmlFilePath, issues),
            issues
        );
        if (!issues.isEmpty()) {
            System.err.println("== Parsing issues detected in " + xmlFilePath + " ==");
            issues.forEach(issue -> System.err.println("  - " + issue));
        }
        return new RawData(xmlFilePath, records);
    }
    
    /**
     * Split large XML file into chunks for parallel processing
     * @param xmlFilePath Path to XML file
     * @param chunkSize Size of each chunk
     * @param numThreads Number of threads
     * @return List of XMLChunk objects
     */
    public List<XMLChunk> splitIntoChunks(String xmlFilePath, int chunkSize, int numThreads) {
        // TODO: Implement chunk splitting logic
        return List.of();
    }
    
    /**
     * Parse a specific chunk of XML
     * @param chunk XMLChunk to parse
     * @param config ETL configuration
     * @return RawData from the chunk
     */
    public RawData parseChunk(XMLChunk chunk, ETLConfig config) {
        // TODO: Implement chunk parsing
        return new RawData(chunk != null ? chunk.getSourceFilePath() : null, null);
    }

    @Override
    public void close() {
        // No resources to release; method provided for API symmetry and future extension.
    }

    private RowTagMetadata resolveStructure(Path xmlPath, ETLConfig config) {
        RowTagMetadata inspected = inspectXml(xmlPath);
        if (config == null) {
            return inspected;
        }

        String rowTag = config.rowTag != null && !config.rowTag.isBlank()
            ? config.rowTag
            : inspected.rowTag;
        String rootTag = config.rootTag != null && !config.rootTag.isBlank()
            ? config.rootTag
            : inspected.rootTag;

        return new RowTagMetadata(rootTag, rowTag);
    }

    private RowTagMetadata inspectXml(Path xmlPath) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        String root = null;
        String row = null;
        int depth = -1;

        try (InputStream stream = Files.newInputStream(xmlPath)) {
            XMLStreamReader reader = factory.createXMLStreamReader(stream);
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    depth++;
                    if (depth == 0) {
                        root = reader.getLocalName();
                    } else if (depth == 1) {
                        row = reader.getLocalName();
                        break;
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    depth--;
                }
            }
            reader.close();
        } catch (IOException | XMLStreamException e) {
            throw new ETLException("Failed to inspect XML structure for " + xmlPath, e);
        }

        if (row == null) {
            throw new ETLException("Unable to determine XML row element for " + xmlPath);
        }
        return new RowTagMetadata(root, row);
    }
    
    private static class LocalDtdResolver implements EntityResolver {
        private final Path xmlPath;
        private final List<String> issues;
        
        LocalDtdResolver(Path xmlPath, List<String> issues) {
            this.xmlPath = xmlPath;
            this.issues = issues;
        }
        
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (systemId == null || systemId.isBlank()) {
                return null;
            }
            
            Path baseDir = xmlPath.getParent() != null ? xmlPath.getParent() : Paths.get(".");
            Path candidate = baseDir.resolve(systemId).normalize();
            if (!Files.exists(candidate)) {
                Path fallback = Paths.get("data").resolve(systemId).normalize();
                if (Files.exists(fallback)) {
                    candidate = fallback;
                } else {
                    issues.add("DTD not found for system identifier '" + systemId + "'; continuing without validation.");
                    return null;
                }
            }
            
            InputStream stream = Files.newInputStream(candidate);
            InputSource source = new InputSource(stream);
            source.setPublicId(publicId);
            source.setSystemId(candidate.toUri().toString());
            return source;
        }
    }
    
    private static class LenientErrorHandler implements ErrorHandler {
        private final String filePath;
        private final List<String> issues;
        
        LenientErrorHandler(String filePath, List<String> issues) {
            this.filePath = filePath;
            this.issues = issues;
        }
        
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            log("WARNING", exception);
        }
        
        @Override
        public void error(SAXParseException exception) throws SAXException {
            log("ERROR", exception);
        }
        
        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            log("FATAL", exception);
            throw new SAXException(buildMessage("Fatal error", exception));
        }
        
        private String buildMessage(String level, SAXParseException exception) {
            return String.format("%s while parsing %s at line %d column %d: %s",
                                 level, filePath, exception.getLineNumber(),
                                 exception.getColumnNumber(), exception.getMessage());
        }

        private void log(String level, SAXParseException exception) {
            String message = buildMessage(level, exception);
            issues.add(message);
            System.err.println(message);
        }
    }

    public record RowTagMetadata(String rootTag, String rowTag) { }
}
