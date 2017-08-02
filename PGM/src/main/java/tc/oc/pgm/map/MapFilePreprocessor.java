package tc.oc.pgm.map;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.inject.assistedinject.Assisted;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.HashingInputStream;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static com.google.common.base.Preconditions.checkNotNull;

public class MapFilePreprocessor {

    public interface Factory {
        MapFilePreprocessor create(MapSource source);
    }

    private final SAXBuilder builder;
    private final Logger logger;
    private final MapConfiguration mapConfiguration;
    private final MapSource source;
    private final Stack<Path> includeStack = new Stack<>();
    private final Map<Path, HashCode> includedFiles = new HashMap<>();

    @Inject MapFilePreprocessor(@Assisted MapSource source, Loggers loggers, SAXBuilder builder, MapConfiguration mapConfiguration) {
        this.source = source;
        this.logger = loggers.get(getClass(), source.getPath().toString());
        this.builder = builder;
        this.mapConfiguration = mapConfiguration;
    }

    public Map<Path, HashCode> getIncludedFiles() {
        return includedFiles;
    }

    public Document readRootDocument(Path file) throws InvalidXMLException {
        checkNotNull(file, "file");

        this.includeStack.clear();

        Document result = this.readDocument(file);

        this.includeStack.clear();

        if(source.globalIncludes()) {
            for(Path globalInclude : mapConfiguration.globalIncludes()) {
                final Path includePath = findIncludeFile(null, globalInclude, null);
                if(includePath != null) {
                    result.getRootElement().addContent(0, readIncludedDocument(includePath, null));
                }
            }
        }

        return result;
    }

    private Document readDocument(Path absolutePath) throws InvalidXMLException {
        final Path relativePath = source.getPath().relativize(absolutePath);
        Document doc;

        try(HashingInputStream istream = new HashingInputStream(Hashing.sha256(), new FileInputStream(absolutePath.toFile()))) {
            doc = this.builder.build(istream);
            doc.setBaseURI(relativePath.toString());
            this.includedFiles.put(absolutePath, istream.hash());

        } catch(FileNotFoundException e) {
            throw new InvalidXMLException("File not found", absolutePath.toString());
        } catch(IOException e) {
            throw new InvalidXMLException("Error reading file: " + e.getMessage(), absolutePath.toString());
        } catch(JDOMParseException e) {
            throw InvalidXMLException.fromJDOM(e, absolutePath.toString());
        } catch(JDOMException e) {
            throw new InvalidXMLException("Unhandled " + e.getClass().getSimpleName(), absolutePath.toString(), e);
        }

        this.processChildren(absolutePath, doc.getRootElement());

        return doc;
    }

    private @Nullable Path findIncludeFile(@Nullable Path basePath, Path includeFile, @Nullable Element includeElement) throws InvalidXMLException {
        Iterable<Path> includePaths = mapConfiguration.includePaths();
        if(basePath != null) {
            includePaths = Iterables.concat(includePaths, Collections.singleton(basePath));
        }

        for(Path includePath : includePaths) {
            Path fullPath = includePath.resolve(includeFile);
            if(Files.isRegularFile(fullPath)) {
                return fullPath.toAbsolutePath();
            }
        }

        return null;
    }

    private List<Content> readIncludedDocument(@Nullable Path basePath, Path includeFile, @Nullable Element includeElement) throws InvalidXMLException {
        final Path fullPath = findIncludeFile(basePath, includeFile, includeElement);
        if(fullPath == null) {
            throw new InvalidXMLException("Failed to find include: " + includeFile, includeElement);
        }
        return readIncludedDocument(fullPath, includeElement);
    }

    private List<Content> readIncludedDocument(Path fullPath, @Nullable Element includeElement) throws InvalidXMLException {
        if(includeStack.contains(fullPath)) {
            throw new InvalidXMLException("Circular include: " + Joiner.on(" --> ").join(includeStack), includeElement);
        }

        includeStack.push(fullPath);
        try {
            return readDocument(fullPath).getRootElement().cloneContent();
        } finally {
            includeStack.pop();
        }
    }

    private List<Content> processIncludeElement(Path baseFile, Element el) throws InvalidXMLException {
        Path path = XMLUtils.parseRelativePath(Node.fromRequiredAttr(el, "src"));
        return readIncludedDocument(baseFile.getParent(), path, el);
    }

    private <T> T getEnvironment(String key, Class<T> type, Node node) throws InvalidXMLException {
        Object value = mapConfiguration.environment().get(key);
        if(value == null) {
            logger.warning("Unknown environment variable '" + key + "', using default value: false");
            value = false;
        }
        if(!type.isInstance(value)) {
            throw new InvalidXMLException("Wrong variable type, expected " + type.getSimpleName() + ", was " + value.getClass().getSimpleName(), node);
        }
        return type.cast(value);
    }

    private List<Content> processConditional(Element el, boolean invert) throws InvalidXMLException {
        for(Node attr : Node.fromAttrs(el)) {
            boolean expected = XMLUtils.parseBoolean(attr);
            boolean actual = getEnvironment(attr.getName(), Boolean.class, attr);
            if(expected != actual) {
                return invert ? el.cloneContent() : Collections.<Content>emptyList();
            }
        }

        return invert ? Collections.<Content>emptyList() : el.cloneContent();
    }

    private void processChildren(Path file, Element parent) throws InvalidXMLException {
        for(int i = 0; i < parent.getContentSize(); i++) {
            Content content = parent.getContent(i);
            if(!(content instanceof Element)) continue;

            Element child = (Element) content;
            List<Content> replacement = null;

            switch(child.getName()) {
                case "include":
                    replacement = processIncludeElement(file, child);
                    break;

                case "if":
                    replacement = processConditional(child, false);
                    break;

                case "unless":
                    replacement = processConditional(child, true);
                    break;
            }

            if(replacement != null) {
                parent.removeContent(i);
                parent.addContent(i, replacement);
                i--; // Process replacement content
            } else {
                processChildren(file, child);
            }
        }
    }
}
