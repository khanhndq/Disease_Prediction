package com.example.diseaseprediction.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.diseaseprediction.AppConstants;
import com.example.diseaseprediction.synonymous.Helper;
import com.example.diseaseprediction.synonymous.Node;
import com.example.diseaseprediction.synonymous.Tree;
import com.example.diseaseprediction.synonymous.Word;
import com.example.diseaseprediction.tokenize.RDRsegmenter;
import com.example.diseaseprediction.tokenize.Vocabulary;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.metadata.MetadataExtractor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 *
 */
public class DiseaseClassificationClient {
    private static final String TAG = "Interpreter";

    private static final int SENTENCE_LEN = 20; // The maximum length of an input sentence.
    // Simple delimiter to split words.
    private static final String SIMPLE_SPACE_OR_PUNCTUATION = " |\\,|\\.|\\!|\\?|\n";
    private static final String MODEL_PATH = AppConstants.MODEL_FILE_NAME;
    private static final String VOCAL_PATH = AppConstants.VOCAB_FILE_NAME;
    private static final String LABEL_PATH = AppConstants.LABEL_FILE_NAME;
    private Tree dictionary;
    /*
     * Reserved values in ImdbDataSet dic:
     * dic["<PAD>"] = 0      used for padding
     * dic["<START>"] = 1    mark for the start of a sentence
     * dic["<UNKNOWN>"] = 2  mark for unknown words (OOV)
     */
    private static final String START = "<START>";
    private static final String PAD = "0";
    private static final String UNKNOWN = "<OOV>";

    /**
     * Number of results to show in the UI.
     */
    private static final int MAX_RESULTS = 10;

    private final Context context;
    private final Map<String, Integer> dic = new HashMap<>();
    private final List<String> labels = new ArrayList<>();
    private Interpreter tflite;
    private AssetManager am;
    private InputStream is;
    private InputStream fis;
    private InputStream fsysn;
    Vocabulary vocabulary;
    RDRsegmenter rdRsegmenter;

    public DiseaseClassificationClient(Context context) {
        this.context = context;
        try {
            am = context.getAssets();
            is = am.open("SCRDR_Model.RDR");
            fis = am.open("vocab_tokenize.txt");
            fsysn = am.open("vocab_sym.txt");
            vocabulary = new Vocabulary();
            //add vocabulary of tokenize to vocabulary
            vocabulary.addVocabularyTokenize(fis);
            rdRsegmenter = new RDRsegmenter(is, fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Load the TF Lite model and dictionary so that the client can start classifying text.
     */
    public void load() {
        loadModel();
    }

    /**
     * Load TF Lite model.
     */
    private synchronized void loadModel() {
        try {
            // Load the TF Lite model
            ByteBuffer buffer = loadModelFile(this.context.getAssets(), MODEL_PATH);
            tflite = new Interpreter(buffer);
            Log.v(TAG, "TFLite model loaded.");

            // Use metadata extractor to extract the dictionary and label files.
            MetadataExtractor metadataExtractor = new MetadataExtractor(buffer);

            // Extract and load the dictionary file.
//      InputStream dictionaryFile = metadataExtractor.getAssociatedFile("model-088-v1.0-vocab.txt");
            InputStream dictionaryFile = am.open(AppConstants.VOCAB_FILE_NAME);
            System.out.println("check dic :" + dictionaryFile);
            loadDictionaryFile(dictionaryFile);
            Log.d(TAG, "Dictionary loaded.");

            // Extract and load the label file.
            InputStream labelFile = metadataExtractor.getAssociatedFile(AppConstants.LABEL_FILE_NAME);
            loadLabelFile(labelFile);
            Log.d(TAG, "Labels loaded.");

        } catch (IOException ex) {
            Log.e(TAG, "Error loading TF Lite model.\n", ex);
        }
    }

    /**
     * Free up resources as the client is no longer needed.
     */
    public synchronized void unload() {
        tflite.close();
        dic.clear();
        labels.clear();
    }

    /**
     * Classify an input string and returns the classification results.
     */
    public synchronized List<Result> classify(String text) throws IOException {
        // Pre-processing.
        int[][] input = tokenizeInputText(text);
        Log.d(TAG, Arrays.deepToString(input));
        // Run inference.
        Log.v(TAG, "Classifying text with TF Lite...");
        float[][] output = new float[1][labels.size()];

        tflite.run(input, output);

        // Find the best classifications.
        PriorityQueue<Result> pq =
                new PriorityQueue<>(
                        MAX_RESULTS, (lhs, rhs) -> Float.compare(rhs.getConfidence(), lhs.getConfidence()));
        for (int i = 0; i < labels.size(); i++) {
            pq.add(new Result("" + i, labels.get(i), output[0][i]));
        }
        final ArrayList<Result> results = new ArrayList<>();
        while (!pq.isEmpty()) {
            results.add(pq.poll());
        }

        Collections.sort(results);
        // Return the probability of each class.
        return results;
    }

    /**
     * Load TF Lite model from assets.
     */
    private static MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath)
            throws IOException {
        try (AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    /**
     * Load labels from model file.
     */
    private void loadLabelFile(InputStream ins) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        // Each line in the label file is a label.
        while (reader.ready()) {
            labels.add(reader.readLine());
        }
    }

    /**
     * Load dictionary from model file.
     */
    private void loadDictionaryFile(InputStream ins) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        // Each line in the dictionary has two columns.
        // First column is a word, and the second is the index of this word.
        while (reader.ready()) {
            List<String> line = Arrays.asList(reader.readLine().split(" "));
            if (line.size() < 2) {
                continue;
            }
            dic.put(line.get(0), Integer.parseInt(line.get(1)));
        }
    }

    /**
     * Pre-processing: tokenize and map the input words into a float array.
     */
    int[][] tokenizeInputText(String text) throws IOException {

        int[] tmp = new int[SENTENCE_LEN];
        List<String> array = Arrays.asList(text.split(SIMPLE_SPACE_OR_PUNCTUATION));
        int index = 0;
        // Prepend <START> if it is in vocabulary file.
        if (dic.containsKey(START)) {
            tmp[index++] = dic.get(START);
        }

        for (String word : array) {
            if (index >= SENTENCE_LEN) {
                break;
            }
            tmp[index++] = dic.containsKey(word) ? dic.get(word) : (int) dic.get(UNKNOWN);
        }
        // Padding and wrapping.
        Arrays.fill(tmp, index, SENTENCE_LEN - 1, Integer.parseInt(PAD));
        int[][] ans = {tmp};
        return ans;
    }

    /**
     *  Pre-processing: tokenize segmenter from RDRsegmenter and synonymous binary tree.
     * @param text
     * @return
     * @throws IOException
     */
    public String tokenize(String text) throws IOException {
        List<String> token = new ArrayList<>();
        //split the sentence into tokens with '_' each token separated by space
        text = rdRsegmenter.segmentTokenizedString(text.toLowerCase());
        token = Arrays.asList(text.split(" "));
        InputStream dictionaryFilevo = am.open("vocab_sym.txt");
        loadDictionary(dictionaryFilevo);
        System.out.println("Token : " + token.toString());
        String result = "";
        for (int i = 0; i < token.size(); i++) {
            String get_Token = token.get(i);
            get_Token = get_Token.replace("_", " ");
            System.out.println("get_Token : " + get_Token);
            get_Token = get_synonymous(get_Token);
            System.out.println("get synonymous " + get_Token);
            get_Token = get_Token.replace(" ", "_");
            System.out.println("get synonymous _ : " + get_Token);
            token.set(i, get_Token);
            result += get_Token + " ";
        }
        System.out.println("===============================" + token.toString());
        System.out.println(result);
        return result;
    }

    /**
     * Load data synonym from synonym file
     * @param dictionaryFile
     */
    public void loadDictionary(InputStream dictionaryFile) {
        dictionary = new Tree(null);
        /* Load the text file into an array of words and their synonyms,
         * we can specify the size of the array too */
        Word[] dicArr = Helper.getDictionary1(dictionaryFile, 89);
        // Sort synonym before add to array
        Arrays.sort(dicArr, new Comparator<Word>() {
            @Override
            public int compare(Word o1, Word o2) {
                if (o1.getTitle().compareTo(o2.getTitle()) == 0) {
                    return 0;
                } else if (o1.getTitle().compareTo(o2.getTitle()) < 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        // Add data in binary search tree
        dictionary.setRoot(dictionary.SArraytoBST(dicArr, 0, dicArr.length - 1));
    }

    /**
     * Get a synonym in data
     * @param word_sys
     * @return
     */
    public String get_synonymous(String word_sys) {
        // Find word in data
        Node word = dictionary.findWord(word_sys, dictionary.getRoot());
        //Check word exist
        if (word != null) {
            System.out.println(word);
            // Replace synonyms with medical words
            String rs = Helper.arrayToString(word.getWord().getSynonyms(), ", ");
            return rs;
        } else {
            // Keep the word the same
            return word_sys;
        }
    }


    Map<String, Integer> getDic() {
        return this.dic;
    }

    Interpreter getTflite() {
        return this.tflite;
    }

    List<String> getLabels() {
        return this.labels;
    }
}
