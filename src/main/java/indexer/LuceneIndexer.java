/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import com.Global;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Andrew
 */
public class LuceneIndexer {

    static Logger log = Logger.getLogger(LuceneIndexer.class);
    static Date finishTime;
    static Date startTime;
    static ExecutorService executor = null;
    static HashMap<Path, BasicFileAttributes> hm = new HashMap<>();
    static int amountOfDocuments;
    static String indexingTime;
    static String listBuildTime;
    static String totalTime;

    /**
     * Indexing the files. This method checks for the directories and then 
     * finishes out after the indexing is complete.
     * @param global This is for reference to the global class variables 
     * and methods.
     * @param createIndex If true a new index will be created from scratch
     * and the old index will be destroyed.
     */
    public static void IndexFiles(Global global, Boolean createIndex) {
        String dataDir = global.dataDir;
        String indexDir = global.indexDir;

        //Verifies that the data directory exists
        if (dataDir == null) {
            System.err.println("Data Directory Is not accessable, Unable to Index files.");
        }

        //Verifies that the data directory is readable and writeable
        final Path docDir = Paths.get(dataDir);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
        }

        startTime = new Date();
        try {
            System.out.println("Indexing to directory '" + indexDir + "'...");

            //Setups the analyzer
            Analyzer analyzer;
            try (Directory dir = FSDirectory.open(Paths.get(indexDir))) {

                analyzer = new StandardAnalyzer();
                IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
                if (createIndex) {
                    // Create a new index in the directory, removing any
                    // previously indexed documents:
                    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                } else {
                    // Add new documents to an existing index:
                    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                }
                iwc.setRAMBufferSizeMB(global.RAM_BUFFER_SIZE);
                iwc.setMaxBufferedDocs(global.MAX_BUFFERED_DOCS);

                LogDocMergePolicy ldmp = new LogDocMergePolicy();
                ldmp.setMergeFactor(global.MERGE_FACTOR);
                iwc.setMergePolicy(ldmp);

                try (IndexWriter writer = new IndexWriter(dir, iwc)) {
                    hm.clear();
                    indexDocs(writer, docDir, global);

                    //This is a costly operation, we scheduled the time to apply it
                    if (global.merge){
                        System.out.println("Starting Merge");
                        writer.forceMerge(1);
                        global.merge = false;
                    }
                    writer.close();
                }
                finishTime = new Date();
                long millis = finishTime.getTime() - startTime.getTime();
                totalTime = String.format("%02dhr %02dmin %02dsec", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                System.out.println("");
                System.out.println("");
                System.out.println("Start Time:          " + global.sdf.format(startTime.getTime()));
                System.out.println("Building List Time:  " + listBuildTime);
                System.out.println("Indexing Time:       " + indexingTime);
                System.out.println("Total Time:          " + totalTime);
                System.out.println("Number of Documents: " + amountOfDocuments);
                System.out.println("Finish Time:         " + global.sdf.format(finishTime.getTime()));
                System.out.println("");
            }
            analyzer.close();
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
            log.fatal(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    /**
     * Indexes the given file using the given writer, or if a directory is
     * given, recurses over files and directories found under the given
     * directory.
     *
     * @param writer Writer to the index where the given file/dir info will be
     * stored.
     * @param path The file to index, or the directory to recurse into to find
     * files to index.
     * @param global This is for reference to the global class variables 
     * and methods.
     * @param indexPanel If true it will also print the console printout lines 
     * to the main panel.
     * @throws IOException If there is a low-level I/O error
     */
    static void indexDocs(final IndexWriter writer, Path path, Global global) throws IOException {
        Date start = new Date();
        
        //load up last index time and write it to the time file
        Date lastIndexTime = new Date(Long.parseLong("0"));
        if (global.lastIndexTime != null) {
            lastIndexTime = global.lastIndexTime;
        }
        Date lastIndexnumbers = lastIndexTime;
        global.lastIndexTime = start;
        
        // delete and create index time file here.
        createNewTimeINI(global);
        
        //Get file information this is done file by file.
        if (Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (Global.validateFileType(file) 
                                && file.getFileName().toString().startsWith("DELETED_") == false 
                                && file.getFileName().toString().startsWith("~") == false) {
                            validateFile(global, file, attrs, lastIndexnumbers);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                log.fatal("Walking File Path Exception");
            }
        }
        Date end = new Date();
        long millis = end.getTime() - start.getTime();
        listBuildTime = String.format("%02dhr %02dmin %02dsec", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        System.out.println("");
        System.out.println("Finished Building List");
        amountOfDocuments = hm.size();
        
        //Run the multi-threaded index.
        Date indexStart = new Date();
            global.executorRunning = true;
            runIndexer(global, writer);
            global.executorRunning = false;
        Date indexEnd = new Date();
        long millis2 = indexEnd.getTime() - indexStart.getTime();
        indexingTime = String.format("%02dhr %02dmin %02dsec", TimeUnit.MILLISECONDS.toHours(millis2),
                TimeUnit.MILLISECONDS.toMinutes(millis2) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis2)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis2) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis2)));
    }

    /**
     * Building the list for indexing based on last index time and gathering 
     * file path to place them into a HashMap for indexing later. The hashMap
     * consists of File Path and Basic Attributes. Contains the check for new
     * or existing index.
     * 
     * @param global This is for reference to the global class variables 
     * and methods.
     * @param file The file to index, or the directory to recurse into to find
     * files to index.
     * @param attrs This is the attributes from the given file gathered from 
     * walking the file tree.
     * @param lastIndexTime The last time the Index was ran.
     */
    public static void validateFile(Global global, Path file, BasicFileAttributes attrs, Date lastIndexTime) {
        Date dateModified = new Date(attrs.lastModifiedTime().toMillis());

        if (global.newIndex == false && dateModified.after(lastIndexTime)) {
            hm.put(file, attrs);
            System.out.println("Last Modified: " + global.sdf.format(dateModified) + " - " + file.toString());
        } else if (global.newIndex == true) {
            hm.put(file, attrs);
            System.out.println(file.toString());
        }
    }

    /**
     * Thread Executor to handle the crawling of the individual documents. The 
     * number of threads used is tunable based off of the global variable
     * 
     * @param global This is for reference to the global class variables 
     * and methods.
     * @param writer Writer to the index where the given file/dir info will be
     * stored. 
     */
    public static void runIndexer(Global global, IndexWriter writer) {
        executor = Executors.newFixedThreadPool(global.NUM_THREADS);

        hm.entrySet().stream().forEach((Entry<Path, BasicFileAttributes> entry) -> {
            Runnable worker = new MyRunnable(writer, entry, global);
            executor.submit(worker);
        });

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        System.out.println("\nFinished all threads");
    }

    /**
     * This is the runnable for the Thread Executor service. One of these is
     * generated per thread and will close out after it is done its operation.
     */
    public static class MyRunnable implements Runnable {

        private final Path path;
        private final BasicFileAttributes attrs;
        private final IndexWriter writer;
        private final Global global;

        MyRunnable(IndexWriter writer, Entry file, Global global) {
            this.path = (Path) file.getKey();
            this.attrs = (BasicFileAttributes) file.getValue();
            this.writer = writer;
            this.global = global;
        }

        @Override
        public void run() {
            try {
                LuceneIndexerAddDocument.indexDoc(writer, path, attrs, global);
            } catch (IOException ex) {
                log.fatal("Thread Index Individual Document Error");
            }
        }
    }

    /**
     * This command is an aggressive tool to force shutdown during indexing
     * without destroying the index. It does so by shutting down the executor
     * service and waiting timeout. This allowed for the indexer to finish its
     * process and remove the write lock so next time we attempt to index there
     * will not be any issues.
     */
    public static void kill() {
        try {
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            log.fatal("Thread Index Kill Error");
        }
    }
    
    /**
     * This creates a new time txt file to reference if the application is
     * closed out. On restart of the application it will reference this time to
     * check against the last modified date of the individual files.
     * 
     * @param global This is for reference to the global class variables 
     * and methods.
     */
    private static void createNewTimeINI(Global global) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("time.txt")))) {
            bw.write("time = \"" + (global.lastIndexTime).getTime() + "\";");
            bw.close();
        } catch (FileNotFoundException ex) {
            log.fatal("File Not Found Creating New Time INI");
        } catch (IOException ex) {
            log.fatal("IO Exception New Time INI");
        }
    }

}
