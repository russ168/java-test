package org.elasticsearch.thrift.test;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.thrift.transport.TTransportException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * User: Dong ai hua
 * Date: 13-4-19
 * Time: 下午5:16
 * To change this template use File | Settings | File Templates.
 */
public class LuceneTests {
    private Directory dir;

    private IndexWriter writer;
    private IndexReader reader;
    private IndexSearcher searcher;
    private StandardAnalyzer analyzer;

    private IndexWriterConfig config;

    private int hitsPerPage = 10;
    private TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

    @BeforeMethod
    public void setupIndexWriterSearcher() throws IOException, TTransportException {
        dir = new RAMDirectory();
        analyzer = new StandardAnalyzer(Version.LUCENE_42);
        config = new IndexWriterConfig(Version.LUCENE_42, analyzer);
        writer = new IndexWriter(dir, config);
        writer.commit();

        reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
    }

    @AfterMethod
    public void close() throws IOException {
        writer.close();
        reader.close();
    }

    @Test
    public void testIndex() throws Exception {
        addDoc(writer, "Lucene in Action", "193398817");
        addDoc(writer, "Lucene for Dummies", "55320055Z");
        addDoc(writer, "Managing Gigabytes", "55063554A");
        addDoc(writer, "The Art of Computer Science", "9900333X");

        try {
            String qstring = "art";
            Query q = new QueryParser(Version.LUCENE_42, "title", analyzer).parse(qstring);

            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            System.out.println("Found " + hits.length + " hits.");
            for(int i=0;i<hits.length;++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
                System.out.flush();
            }
            //System.out.println("message to stdout");
            //System.err.println("message to stderr");
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
        }
        reader.close();
    }

    @Test
    public void testFacet() throws Exception {
    }

    @Test
    public void testGrouping() throws Exception {
    }

    @Test
    public void testFiedCache() throws Exception {
    }

    @Test
    public void testFieldSelector() throws Exception {
    }

    private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("isbn", isbn, Field.Store.YES));
        w.addDocument(doc);
    }
}
