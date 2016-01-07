/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.epub.EpubParser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.odf.OpenDocumentParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrew
 */
public class LuceneIndexerAddDocument {

    /**
     * Indexes a single document with the aid of Apache Tika.
     *
     * @param writer Writer to the index where the given file/dir info will be
     * stored.
     * @param file The file to index, or the directory to recurse into to find
     * files to index.
     * @param attrs This is the attributes from the given file gathered from
     * walking the file tree.
     * @param global This is for reference to the global class variables and
     * methods.
     * @param indexPanel If true it will also print the console printout lines
     * to the main panel.
     * @throws IOException
     */
    static void indexDoc(IndexWriter writer, Path file, BasicFileAttributes attrs, Global global, boolean indexPanel) throws IOException {
        File document = file.toFile();
        if (document.renameTo(document)) {
            try (InputStream stream = Files.newInputStream(file)) {

                //make a new, empty document
                Document doc = new Document();

                //Add the path of the file as a field named "path".
                Field pathField = new StringField("path", file.toString(), Field.Store.YES);
                doc.add(pathField);

                //Add the last modified date of the file as a field named "modified".
                doc.add(new LongField("modified", attrs.lastModifiedTime().toMillis(), Field.Store.YES));

                //Add the created date of the file as a field named "created".
                doc.add(new LongField("created", attrs.creationTime().toMillis(), Field.Store.YES));

                //Add the document File Name
                doc.add(new StringField("filename", file.getFileName().toString(), Field.Store.YES));

                //Add the contents of the file as a field named "vcontents". 
                //Parser type for Tika
                BodyContentHandler handler = new BodyContentHandler(global.WRITE_LIMIT);
                Metadata metadata = new Metadata();
                FileInputStream inputstream = new FileInputStream(new File(file.toString()));
                ParseContext pcontext = new ParseContext();

                //New Field Type
                FieldType bodyType = new FieldType();
                bodyType.setStored(true);
                bodyType.setTokenized(true);
                // for Highlighter, FastvectorHighlighter
                bodyType.setStoreTermVectors(true);
                bodyType.setStoreTermVectorPositions(true);
                bodyType.setStoreTermVectorOffsets(true);
                // for PostingsHighlighter
                bodyType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

                /**
                 * Determine the document type and the proper parser for the
                 * document After the document is determined we grab the content
                 * and position offset for highlighting.
                 */
                try {
                    if (file.toString().endsWith(".pdf")) {
                        PDFParser pdfparser = new PDFParser();
                        pdfparser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else if (file.toString().endsWith(".docx") || file.toString().endsWith(".pptx") || file.toString().endsWith(".xlsx")
                            || file.toString().endsWith(".docm") || file.toString().endsWith(".pptm") || file.toString().endsWith(".xlsm")) {
                        OOXMLParser msofficeparser = new OOXMLParser();
                        msofficeparser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else if (file.toString().endsWith(".doc") || file.toString().endsWith(".ppt") || file.toString().endsWith(".xlx")) {
                        OfficeParser msofficeparser = new OfficeParser();
                        msofficeparser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else if (file.toString().endsWith(".odt") || file.toString().endsWith(".odp") || file.toString().endsWith(".ods")) {
                        OpenDocumentParser openofficeparser = new OpenDocumentParser();
                        openofficeparser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else if (file.toString().endsWith(".epub")) {
                        EpubParser epubParser = new EpubParser();
                        epubParser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else if (file.toString().endsWith(".xml")) {
                        XMLParser XMLparser = new XMLParser();
                        XMLparser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else if (file.toString().endsWith(".htm") || file.toString().endsWith(".html") || file.toString().endsWith(".mhtml")) {
                        HtmlParser HTMLparser = new HtmlParser();
                        HTMLparser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else if (file.toString().endsWith(".rtf")) {
                        RTFParser RTFparser = new RTFParser();
                        RTFparser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else if (file.toString().endsWith(".txt")) {
                        TXTParser TXTparser = new TXTParser();
                        TXTparser.parse(inputstream, handler, metadata, pcontext);
                        doc.add(new Field("vcontent", handler.toString(), bodyType));
                    } else {
                        BufferedReader buffedRead = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                        doc.add(new TextField("vcontent", buffedRead));
                    }
                } catch (SAXException | TikaException ex) {
                    Logger.getLogger(LuceneIndexer.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                    // New index, so we just add the document (no old document can be there):
                    writer.addDocument(doc);
                    System.out.println("adding " + file);
                    if (indexPanel) {
                        Global.indexPanelPrintOut(global, "adding " + file + "\n");
                    }
                } else {
                    /**
                     * Existing index (an old copy of this document may have
                     * been indexed) so we use updateDocument instead to replace
                     * the old one matching the exact path, if present:
                     */
                    writer.updateDocument(new Term("path", file.toString()), doc);
                    System.out.println("updating " + file);
                    if (indexPanel) {
                        Global.indexPanelPrintOut(global, "updating " + file + "\n");
                    }
                }
            }
        } else {
            System.out.println("LOCKED: " + file);
            if (indexPanel) {
                Global.indexPanelPrintOut(global, "LOCKED: " + file + "\n");
            }
        }
    }

}
