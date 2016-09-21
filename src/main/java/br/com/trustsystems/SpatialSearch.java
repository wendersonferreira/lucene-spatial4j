package br.com.trustsystems;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;


public class SpatialSearch {

    private IndexWriter indexWriter;
    private IndexSearcher searcher;
    private SpatialContext ctx;
    private SpatialStrategy strategy;

    public SpatialSearch(Path indexPath) {

        StandardAnalyzer a = new StandardAnalyzer(Version.LUCENE_44);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, a);
        Directory directory;

        try {
            directory = new SimpleFSDirectory(indexPath.toFile());
            indexWriter = new IndexWriter(directory, iwc);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.ctx = SpatialContext.GEO;

        SpatialPrefixTree grid = new GeohashPrefixTree(ctx, 11);
        this.strategy = new RecursivePrefixTreeStrategy(grid, "location");
    }

    public void indexDocuments() throws IOException {

        indexWriter.addDocument(newGeoDocument(1, "Bangalore", ctx.makePoint(12.9558, 77.620979)));
        indexWriter.addDocument(newGeoDocument(2, "Cubbon Park", ctx.makePoint(12.974045, 77.591995)));
        indexWriter.addDocument(newGeoDocument(3, "Tipu palace", ctx.makePoint(12.959365, 77.573792)));
        indexWriter.addDocument(newGeoDocument(4, "Bangalore palace", ctx.makePoint(12.998095, 77.592041)));
        indexWriter.addDocument(newGeoDocument(5, "Monkey Bar", ctx.makePoint(12.97018, 77.61219)));
        indexWriter.addDocument(newGeoDocument(6, "Chennai", ctx.makePoint(13.060422, 80.249583)));
        indexWriter.addDocument(newGeoDocument(7, "Elliot's Beach", ctx.makePoint(12.998976, 80.271286)));
        indexWriter.addDocument(newGeoDocument(8, "Kapaleeshwar Temple", ctx.makePoint(13.033889, 80.269722)));


        indexWriter.commit();
        indexWriter.close();
    }


    private Document newGeoDocument(int id, String name, Shape shape) {

        FieldType ft = new FieldType();
        ft.setIndexed(true);
        ft.setStored(true);

        Document doc = new Document();

        doc.add(new IntField("id", id, Store.YES));
        doc.add(new Field("name", name, ft));
        for (IndexableField f : strategy.createIndexableFields(shape)) {
            doc.add(f);
        }

        doc.add(new StoredField(strategy.getFieldName(), ctx.toString(shape)));

        return doc;
    }

    public void setSearchIndexPath(Path indexPath) throws IOException {
        IndexReader indexReader = DirectoryReader.open(new SimpleFSDirectory(indexPath.toFile()));
        this.searcher = new IndexSearcher(indexReader);
    }

    public void searchBBox(Double minLat, Double minLng, Double maxLat, Double maxLng) throws IOException {

        SpatialArgs args = new SpatialArgs(SpatialOperation.IsWithin, ctx.makeRectangle(minLat, maxLat, minLng, maxLng));

        Filter filter = strategy.makeFilter(args);
        int limit = 10;
        TopDocs topDocs = searcher.search(new MatchAllDocsQuery(), filter, limit);

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc s : scoreDocs) {
            Document doc = searcher.doc(s.doc);
            System.out.println(doc.get("id") + "\t" + doc.get("name"));
        }
    }

    public static void main(String[] args) throws IOException {

        Path indexPath = Paths.get(System.getProperty("user.home"),"geo_spatial_index");
        SpatialSearch s = new SpatialSearch(indexPath);

        //Indexes sample documents
        s.indexDocuments();
        s.setSearchIndexPath(indexPath);

        //Get Places Within Chennai Bounding Box.
        System.out.println("Places WithIn Chennai Bounding Box\n");
        s.searchBBox(12.9673, 80.184631, 13.15148, 80.306709);


        //Get Places Within Bangalore Bounding Box.
        System.out.println("Places WithIn Bangalore Bounding Box");
        s.searchBBox(12.76805, 77.465202, 13.14355, 77.776749);

    }

}

