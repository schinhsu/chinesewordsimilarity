/*
 * Copyright (C) 2008 SKLSDE(State Key Laboratory of Software Development and Environment, Beihang University)., All Rights Reserved.
 */
package edu.buaa.sklsde.wordsimilarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * ����Ϊ����Ŀ����Ҫ�ļ����ṩ����������ƶȵ�һЩ������ʽ����Ϊ��̬�� �����̰߳�ȫ�����Զ��̵߳��á� �����㷨�ο����ģ� �����ڣ�֪�����Ĵʻ��������ƶȼ��㡷����.pdf
 * 
 * @author Yingqiang Wu
 * @version 1.0
 */
public class WordSimilarity {
    /**
     * �ʿ������еľ���ʣ�������ԭ,֪���ж���Ĵ���
     */
    public static Map<String, List<Word>> ALLWORDS = new HashMap<String, List<Word>>();
    /**
     * ������ͬ������е����д��key���ţ�value���������µ����д�����ɵ�list�� list�е����дʶ�Ϊͬ��ʻ�����صĴ��
     */
    public static Map<String, List<String>> CILIN = new HashMap<String, List<String>>();
    /**
     * ������ͬ������е����д��keyΪ���valueΪ����
     */
    public static Map<String, String> ALLWORDS_IN_CILIN = new HashMap<String, String>();
    /**
     * sim(p1,p2) = alpha/(d+alpha)
     */
    private static double alpha = 1.6;
    /**
     * ����ʵ�ʵ����ƶȣ�������������ԭȨ��
     */
    private static double beta1 = 0.5;
    /**
     * ����ʵ�ʵ����ƶȣ�������������ԭȨ��
     */
    private static double beta2 = 0.2;
    /**
     * ����ʵ�ʵ����ƶȣ���������ϵ��ԭȨ��
     */
    private static double beta3 = 0.17;
    /**
     * ����ʵ�ʵ����ƶȣ���������ϵ������ԭȨ��
     */
    private static double beta4 = 0.13;
    /**
     * ���������ԭ�����ƶ�һ�ɴ���Ϊһ���Ƚ�С�ĳ���. ����ʺ;���ʵ����ƶȣ������������ͬ����Ϊ1������Ϊ0.
     */
    private static double gamma = 0.2;
    /**
     * ����һ�ǿ�ֵ���ֵ�����ƶȶ���Ϊһ���Ƚ�С�ĳ���
     */
    private static double delta = 0.2;
    /**
     * �����޹���ԭ֮���Ĭ�Ͼ���
     */
    private static int DEFAULT_PRIMITIVE_DIS = 20;
    /**
     * ֪���е��߼�����
     */
    private static String LOGICAL_SYMBOL = ",~^";
    /**
     * ֪���еĹ�ϵ����
     */
    private static String RELATIONAL_SYMBOL = "#%$*+&@?!";
    /**
     * ֪���е�������ţ���ʣ�������
     */
    private static String SPECIAL_SYMBOL = "{";
    /**
     * the logger for this class
     */
    private static final Logger logger;
    /**
     * Ĭ�ϼ����ļ�
     */
    static {
        System.setProperty("java.util.logging.config.file",
                "test_files/logging.properties");
        logger = Logger.getLogger("global");
        loadGlossary();
        loadCiLin();
    }

    /**
     * load the file, dict/��������Ϣ�����о���ͬ��ʴ�����չ��.txt.
     */
    private static void loadCiLin() {
        String line = null;
        BufferedReader reader = null;
        try {

            reader = new BufferedReader(new FileReader(
                    "dict/��������Ϣ�����о���ͬ��ʴ�����չ��.txt"));
            logger.log(Level.INFO,
                    "start to load the file dict/��������Ϣ�����о���ͬ��ʴ�����չ��.txt");
            line = reader.readLine();
            while (line != null) {
                String[] strs = line.split(" ");
                String category = strs[0];
                List<String> list = new ArrayList<String>();
                for (int i = 1; i < strs.length; i++) {
                    ALLWORDS_IN_CILIN.put(strs[i], category);
                    list.add(strs[i]);
                }
                CILIN.put(category, list);
                line = reader.readLine();
            }
            logger.log(Level.INFO,
                    "finished loading the file dict/��������Ϣ�����о���ͬ��ʴ�����չ��.txt");
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Failed to load the file dict/��������Ϣ�����о���ͬ��ʴ�����չ��.txt, "
                            + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE,
                        "Failed to load the file dict/��������Ϣ�����о���ͬ��ʴ�����չ��.txt, "
                                + e.getMessage());
            }
        }
    }

    /**
     * ���� glossay.dat �ļ���֪����
     */
    private static void loadGlossary() {
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("dict/glossary.dat"));
            logger.log(Level.INFO,
            "start to load the file dict/glossary.dat");
            line = reader.readLine();
            while (line != null) {
                // parse the line
                // the line format is like this:
                // �������� N place|�ط�,capital|����,ProperName|ר,(the United Arab Emirates|����������������)
                line = line.trim().replaceAll("\\s+", " ");
                String[] strs = line.split(" ");
                String word = strs[0];
                String type = strs[1];
                // ��Ϊ�ǰ��ո񻮷֣����һ���ֵļӻ�ȥ
                String related = strs[2];
                for (int i = 3; i < strs.length; i++) {
                    related += (" " + strs[i]);
                }
                // Create a new word
                Word w = new Word();
                w.setWord(word);
                w.setType(type);
                parseDetail(related, w);
                // save this word.
                addWord(w);
                // read the next line
                line = reader.readLine();
            }
            logger.log(Level.INFO,
            "finished loading the file dict/glossary.dat");
        } catch (Exception e) {
            System.out.println("Error line: " + line);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * �����������֣��������Ľ������<code>Word word</code>.
     * 
     * @param related
     */
    private static void parseDetail(String related, Word word) {
        // spilt by ","
        String[] parts = related.split(",");
        boolean isFirst = true;
        boolean isRelational = false;
        boolean isSimbol = false;
        String chinese = null;
        String relationalPrimitiveKey = null;
        String simbolKey = null;
        for (int i = 0; i < parts.length; i++) {
            // ����Ǿ���ʣ��������ſ�ʼ�ͽ�β: (Bahrain|����)
            if (parts[i].startsWith("(")) {
                parts[i] = parts[i].substring(1, parts[i].length() - 1);
                // parts[i] = parts[i].replaceAll("\\s+", "");
            }
            // ��ϵ��ԭ��֮��Ķ��ǹ�ϵ��ԭ
            if (parts[i].contains("=")) {
                isRelational = true;
                // format: content=fact|����
                String[] strs = parts[i].split("=");
                relationalPrimitiveKey = strs[0];
                String value = strs[1].split("\\|")[1];
                word.addRelationalPrimitive(relationalPrimitiveKey, value);

                continue;
            }
            String[] strs = parts[i].split("\\|");
            // ��ʼ�ĵ�һ���ַ���ȷ���Ƿ�Ϊ��ԭ������������ϵ��
            int type = getPrimitiveType(strs[0]);
            // �������Ĳ��ֵĴ���,�������û�����Ľ���
            if (strs.length > 1) {
                chinese = strs[1];
            }
            if (chinese != null
                    && (chinese.endsWith(")") || chinese.endsWith("}"))) {
                chinese = chinese.substring(0, chinese.length() - 1);
            }
            // ��ԭ
            if (type == 0) {
                // ֮ǰ��һ����ϵ��ԭ
                if (isRelational) {
                    word
                            .addRelationalPrimitive(relationalPrimitiveKey,
                                    chinese);
                    continue;
                }
                // ֮ǰ��һ���Ƿ�����ԭ
                if (isSimbol) {
                    word.addRelationSimbolPrimitive(simbolKey, chinese);
                    continue;
                }
                if (isFirst) {
                    word.setFirstPrimitive(chinese);
                    isFirst = false;
                    continue;
                } else {
                    word.addOtherPrimitive(chinese);
                    continue;
                }
            }
            // ��ϵ���ű�
            if (type == 1) {
                isSimbol = true;
                isRelational = false;
                simbolKey = Character.toString(strs[0].charAt(0));
                word.addRelationSimbolPrimitive(simbolKey, chinese);
                continue;
            }
            if (type == 2) {
                // ���
                if (strs[0].startsWith("{")) {
                    // ȥ����ʼ��һ���ַ� "{"
                    String english = strs[0].substring(1);
                    // ȥ���а벿�� "}"
                    if (chinese != null) {
                        word.addStructruralWord(chinese);
                        continue;
                    } else {
                        // ���û�����Ĳ��֣���ʹ��Ӣ�Ĵ�
                        word.addStructruralWord(english);
                        continue;
                    }
                }
            }
        }
    }

    /**
     * <p>
     * ��Ӣ�Ĳ���ȷ�������ԭ�����
     * </p>
     * <p>
     * 0-----Primitive<br/> 1-----Relational<br/> 2-----Special
     * </p>
     * 
     * @param english
     * @return һ������������������ֵΪ1��2��3��
     */
    public static int getPrimitiveType(String str) {
        String first = Character.toString(str.charAt(0));
        if (RELATIONAL_SYMBOL.contains(first)) {
            return 1;
        }
        if (SPECIAL_SYMBOL.contains(first)) {
            return 2;
        }
        return 0;
    }

    /**
     * ����������������ƶ�
     */
    public static double simWord(String word1, String word2) {
        double sim1 = simWordHowNet(word1,word2);
        
        double sim2 = simWordCiLin(word1, word2);
        //System.out.println("how net sim1: "+sim1);
        //System.out.println("ci line sim2: "+sim2);
        // ���֪����û����¼�Ĵ��ʹ��ͬ��ʴ����������������ƶ�
//        if(sim1!=0.0 && sim2!=0.0){
//            return sim1*0.4+sim2*0.6;
//        }
//        if(sim1==0.0){
//            return sim2;
//        }else{
//            return sim1;
//        }
        //ȡ����ֵ�ϴ���, 2008-11-15
        return sim1>sim2 ? sim1 : sim2; 
    }
    public static double simWordHowNet(String word1,String word2){
        if (ALLWORDS.containsKey(word1) && ALLWORDS.containsKey(word2)) {
            List<Word> list1 = ALLWORDS.get(word1);
            List<Word> list2 = ALLWORDS.get(word2);
            double max = 0;
            for (Word w1 : list1) {
                for (Word w2 : list2) {
                    double sim = simWord(w1, w2);
                    max = (sim > max) ? sim : max;
                }
            }
            return max;
        }
        if(!ALLWORDS.containsKey(word1)){
            logger.log(Level.WARNING, word1+"û�б�֪������¼");
        }
        if(!ALLWORDS.containsKey(word2)){
            logger.log(Level.WARNING, word2+"û�б�֪������¼");
        }
        return 0.0;
    }
    /**
     * ʹ��֪�����һ�����ڵ�������ظ��
     * @param word
     * @return
     */
    public static List<String> getReletiveWords(String word){
        List<String> list = new ArrayList<String>();
        if(ALLWORDS.containsKey(word)){
            List<Word> list1 = ALLWORDS.get(word);
            for(int i=0;i<list1.size();i++){
                list.add(list1.get(i).getWord());
            }
        }
        return list;
    }
    /**
     * ʹ��ͬ��ʴ��ֻ������ʵĽ����
     * @param word
     * @return
     */
    public static List<String> getSynonym(String word){
        List<String> list = new ArrayList<String>();
        if(ALLWORDS_IN_CILIN.containsKey(word)){
            List<String> list1 = CILIN.get(ALLWORDS_IN_CILIN.get(word));
            for(String w : list1){
                if(!w.equals(word)){
                    list.add(w);
                }
            }
        }
        return list;
    }
    /**
     * caculate the word similarity using CiLin.
     * @param word1
     * @param word2
     * @return
     */
    public static double simWordCiLin(String word1,String word2){
        if(ALLWORDS_IN_CILIN.containsKey(word1)&&ALLWORDS_IN_CILIN.containsKey(word2)){
            logger.log(Level.INFO, "use cilin to calulate the word similarity");
            String category1 = ALLWORDS_IN_CILIN.get(word1);
            String category2 = ALLWORDS_IN_CILIN.get(word2);
            return simCategory(category1,category2);
        }
        if(!ALLWORDS_IN_CILIN.containsKey(word1)){
            logger.log(Level.WARNING, word1+"û�б�ͬ��ʴ�����¼");
        }
        if(!ALLWORDS_IN_CILIN.containsKey(word2)){
            logger.log(Level.WARNING, word2+"û�б�ͬ��ʴ�����¼");
        }
        return 0.0;
    }
    
    /**
     * �����������ֱ�ӵľ��룬�ڴ����У����ǽ���������ƶȣ���ͬ�ڴ��������������ƶ�.<br/>
     * category��Aa01B03#<br/>
     * ��һλ����д��ĸ������,��һ��<br/>
     * �ڶ�λ��Сд��ĸ������,�ڶ���<br/>
     * ��������λ�����֣�С�࣬������<br/>
     * ����λ����д��ĸ����Ⱥ�����ļ�<br/>
     * ��������λ�����֣�ԭ�Ӵ�Ⱥ�����弶<br/>
     * �ڰ�λ����=#@������=��������ȣ�ͬ�壻��#���������ȣ�ͬ�ࣻ��@���������ҷ�գ��������ڴʵ��û��ͬ��ʣ�Ҳû����ش�<br/>
     * @param category1
     * @param category2
     * @return
     */
    public static double simCategory(String category1,String category2){
        String big1 = category1.substring(0,1);
        String middle1 = category1.substring(1,2);
        String small1 = category1.substring(2,4);
        String wordGroup1 = category1.substring(4,5);
        String UnitWordGroup1 = category1.substring(5,7);
        String big2 = category2.substring(0,1);
        String middle2 = category2.substring(1,2);
        String small2 = category2.substring(2,4);
        String wordGroup2 = category2.substring(4,5);
        String UnitWordGroup2 = category2.substring(5,7);
        //d Ϊ�������,ʹ��1/d
      //  int d = 0;
       // int a = 2.5; //
        if(!big1.equals(big2)){
            //Ĭ��ʹ����Զ����
            return 0.1;
            //d=10;
           // return 
        }
        if(!middle1.equals(middle2)){
            //d = 8
            return 0.125;
        }
        if(!small1.equals(small2)){
            //d=6
            return 0.1667;
        }
        if(!wordGroup1.equals(wordGroup2)){
            //4
            return 0.25;
        }
        if(!UnitWordGroup1.equals(UnitWordGroup2)){
            //2
            return 0.5;
        }
        return 1;
    }

    /**
     * ����������������ƶ�
     * 
     * @param w1
     * @param w2
     * @return
     */
    public static double simWord(Word w1, Word w2) {
        // ��ʺ�ʵ�ʵ����ƶ�Ϊ��
        if (w1.isStructruralWord() != w2.isStructruralWord()) {
            return 0;
        }
        // ���
        if (w1.isStructruralWord() && w2.isStructruralWord()) {
            List<String> list1 = w1.getStructruralWords();
            List<String> list2 = w2.getStructruralWords();
            return simList(list1, list2);
        }
        // ʵ��
        if (!w1.isStructruralWord() && !w2.isStructruralWord()) {
            // ʵ�ʵ����ƶȷ�Ϊ4������
            // ������ԭ���ƶ�
            String firstPrimitive1 = w1.getFirstPrimitive();
            String firstPrimitive2 = w2.getFirstPrimitive();
            double sim1 = simPrimitive(firstPrimitive1, firstPrimitive2);
            // ����������ԭ���ƶ�
            List<String> list1 = w1.getOtherPrimitives();
            List<String> list2 = w2.getOtherPrimitives();
            double sim2 = simList(list1, list2);
            // ��ϵ��ԭ���ƶ�
            Map<String, List<String>> map1 = w1.getRelationalPrimitives();
            Map<String, List<String>> map2 = w2.getRelationalPrimitives();
            double sim3 = simMap(map1, map2);
            // ��ϵ�������ƶ�
            map1 = w1.getRelationSimbolPrimitives();
            map2 = w2.getRelationSimbolPrimitives();
            double sim4 = simMap(map1, map2);
            double product = sim1;
            double sum = beta1 * product;
            product *= sim2;
            sum += beta2 * product;
            product *= sim3;
            sum += beta3 * product;
            product *= sim4;
            sum += beta4 * product;
            return sum;
        }
        return 0.0;
    }

    /**
     * map�����ƶȡ�
     * 
     * @param map1
     * @param map2
     * @return
     */
    public static double simMap(Map<String, List<String>> map1,
            Map<String, List<String>> map2) {
        if (map1.isEmpty() && map2.isEmpty()) {
            return 1;
        }
        int total = map1.size() + map2.size();
        double sim = 0;
        int count = 0;
        for (String key : map1.keySet()) {
            if (map2.containsKey(key)) {
                // shallow copy
                List<String> list1 = new ArrayList<String>(map1.get(key));
                List<String> list2 = new ArrayList<String>(map2.get(key));
                sim += simList(list1, list2);
                count++;
            }
        }
        return (sim + delta * (total - 2 * count)) / (total - count);
    }

    /**
     * �Ƚ��������ϵ����ƶ�
     * 
     * @param list1
     * @param list2
     * @return
     */
    public static double simList(List<String> list1, List<String> list2) {
        if (list1.isEmpty() && list2.isEmpty())
            return 1;
        int m = list1.size();
        int n = list2.size();
        int big = m > n ? m : n;
        int N = (m < n) ? m : n;
        int count = 0;
        //int index1 = 0, index2 = 0;
        double sum = 0;
        double max = 0;
        //����������
        Map<String,Double> map = new HashMap<String, Double>();
        for (int i = 0; i < list1.size(); i++) {
            for (int j = 0; j < list2.size(); j++) {
                double sim = innerSimWord(list1.get(i), list2.get(j));
                map.put(i+"#"+j, sim);
            }
        }
        while(count < N){
            max = 0;
            String index = "";
            for(String key : map.keySet()){
                double sim = map.get(key);
                if(sim >= max){
                    max = sim;
                    index = key;
                }
            }
            sum += max;
            //remove the useless value in the temp map.
            map.remove(index);
            int sharp_index=index.indexOf('#');
//            if(sharp_index==-1){
//                System.out.println(list1);
//                System.out.println(list2);
//            }
            String str1 = index.substring(0,sharp_index);
            String str2 = index.substring(sharp_index+1);
            Set<String> keys = new HashSet<String>(map.keySet());
            for(String key : keys){
                if(key.startsWith(str1+'#')||key.endsWith('#'+str2)){
                    map.remove(key);
                }
            }
            count++;
        }
        
//        while (count < N) {
//            max = 0;
//            for (int i = 0; i < list1.size(); i++) {
//                for (int j = 0; j < list2.size(); j++) {
//                    double sim = innerSimWord(list1.get(i), list2.get(j));
//                    if (sim > max) {
//                        index1 = i;
//                        index2 = j;
//                        max = sim;
//                    }
//                }
//            }
//            sum += max;
//            list1.remove(index1);
//            list2.remove(index2);
//            count++;
//        }
        return (sum + delta * (big - N)) / big;
    }

    /**
     * �ڲ��Ƚ������ʣ�������Ϊ����ʣ�Ҳ��������ԭ
     * 
     * @param word1
     * @param word2
     * @return
     */
    private static double innerSimWord(String word1, String word2) {
        boolean isPrimitive1 = Primitive.isPrimitive(word1);
        boolean isPrimitive2 = Primitive.isPrimitive(word2);
        // ������ԭ
        if (isPrimitive1 && isPrimitive2)
            return simPrimitive(word1, word2);
        // �����
        if (!isPrimitive1 && !isPrimitive2) {
            if (word1.equals(word2))
                return 1;
            else
                return 0;
        }
        // ��ԭ�;���ʵ����ƶ�, Ĭ��Ϊgamma=0.2
        return gamma;
    }

    /**
     * @param primitive1
     * @param primitive2
     * @return
     */
    public static double simPrimitive(String primitive1, String primitive2) {
        int dis = disPrimitive(primitive1, primitive2);
        return alpha / (dis + alpha);
    }

    /**
     * ����������ԭ֮��ľ��룬���������ԭ���û�й�ͬ�ڵ㣬���������ǵľ���Ϊ20��
     * 
     * @param primitive1
     * @param primitive2
     * @return
     */
    public static int disPrimitive(String primitive1, String primitive2) {
        List<Integer> list1 = Primitive.getParents(primitive1);
        List<Integer> list2 = Primitive.getParents(primitive2);
        for (int i = 0; i < list1.size(); i++) {
            int id1 = list1.get(i);
            if (list2.contains(id1)) {
                int index = list2.indexOf(id1);
                return index + i;
            }
        }
        return DEFAULT_PRIMITIVE_DIS;
    }

    /**
     * ����һ������
     * 
     * @param word
     */
    public static void addWord(Word word) {
        List<Word> list = ALLWORDS.get(word.getWord());

        if (list == null) {
            list = new ArrayList<Word>();
            list.add(word);
            ALLWORDS.put(word.getWord(), list);
        } else {
            list.add(word);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        BufferedReader reader = new BufferedReader(new FileReader(
                "dict/glossary.dat"));
        Set<String> set = new HashSet<String>();
        String line = reader.readLine();
        while (line != null) {
            // System.out.println(line);
            line = line.replaceAll("\\s+", " ");
            String[] strs = line.split(" ");
            for (int i = 0; i < strs.length; i++) {
                System.out.print(" " + strs[i]);
            }
            System.out.println();
            set.add(strs[1]);
            line = reader.readLine();
        }
        System.out.println(set.size());
        for (String name : set) {
            System.out.println(name);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}