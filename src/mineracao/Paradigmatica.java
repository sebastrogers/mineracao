package mineracao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import Jama.Matrix;


public class Paradigmatica {

    private Integer numeroDoc = 0;
    private List<String> palavrasExtraidas;
    private Map<String, List<Integer>> docs;
    private Map<String, Map<Integer, Integer>> palavraFreqDoc;
    private List<String> doc;
    private Matrix matriz;
    private Map<Integer, Integer> palavrasExtraidasDoc;
    private Map<String, Set<String>> bagOfWordsLeft;
    private Map<String, Set<String>> bagOfWordsRight;
    String diretorio = "";

    public Paradigmatica() {

        palavraFreqDoc = new HashMap<>();
        palavrasExtraidasDoc = new HashMap<>();
        docs = new HashMap<>();
        doc = new ArrayList<>();
        palavrasExtraidas = new ArrayList<>();
        bagOfWordsLeft = new HashMap<>();
        bagOfWordsRight = new HashMap<>();
    }

    public void lerArquivo() {

        try {
            File dirBaseP = new File(diretorio);

            FileReader arq = new FileReader(dirBaseP);

            BufferedReader lerArq = new BufferedReader(new InputStreamReader(new FileInputStream(dirBaseP), "ISO-8859-1"));
            String linha = lerArq.readLine();

            while (linha != null) {

                System.out.println(linha);

                if (!linha.equals("") && !linha.equals(" ")) {
                    
                    linha = eliminarCaractere(linha);
                    

                    String[] palavras = linha.split(" ");

                    String left = "left";
                    String right = "right";
                    String aux = "";
                    
                    
                    int count = 0;
                    for (String p : palavras) {

                        p = p.toLowerCase();

                        if (this.docs.containsKey(p)) {
                            List l = docs.get(p);
                            l.add(numeroDoc);
                            this.docs.put(p, l);
                            if (count == 0) {
                                count++;
                            }
                            aux = right;
                            right = left;
                            left = p;
                        } else if (StopWord.getStopwords().containsKey(p)) {

                        } else {

                            List<Integer> l = new ArrayList<>();
                            l.add(numeroDoc);
                            this.docs.put(p, l);
                            palavrasExtraidas.add(p);
                            count++;
                            aux = right;
                            right = left;

                            left = p;


                        }
                        if (palavraFreqDoc.containsKey(p)) {
                            Map<Integer, Integer> m = palavraFreqDoc.get(p);
                            m.put(numeroDoc, count);
                            palavraFreqDoc.put(p, m);
                        } else {
                            Map<Integer, Integer> m = new HashMap<>();
                            m.put(numeroDoc, count);
                            palavraFreqDoc.put(p, m);
                        }
                        
                        if(!right.equals("left") && !right.equals("") && !aux.equals("right") && !aux.equals("")){
                        bagOfWords(aux,left,right);
                        }

                    }

               
                    palavrasExtraidasDoc.put(numeroDoc, count);
                    numeroDoc++;
                    doc.add(linha);
                }

                linha = lerArq.readLine();
            }

            arq.close();


            
        } catch (IOException ex) {
            Logger.getLogger(Paradigmatica.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void bagOfWords(String aux, String left, String right){
    	if (!right.equals("right")) {
            if (bagOfWordsRight.containsKey(right)) {
                Set<String> d = bagOfWordsRight.get(right);
                d.add(left);
            } else {
                Set<String> d = new HashSet<>();
                d.add(right);
                bagOfWordsRight.put(left, d);
            }
        }
        if (!aux.equals("left")) {
            if (bagOfWordsLeft.containsKey(right)) {
                Set<String> d = bagOfWordsLeft.get(right);
                d.add(aux);
            } else {
                Set<String> d = new HashSet<>();
                d.add(aux);
                bagOfWordsLeft.put(right, d);
            }
        }
    }

    public String eliminarCaractere(String sentenca){
    	for (String caractere : StopWord.getCaracteres()) {
        	
            
            sentenca = sentenca.replaceAll("[" + caractere + "]", "");

        
    }
    	return sentenca;
    }
    public void matrizFrequencia() {

        matriz = new Matrix(docs.size(), numeroDoc);
        int aux = 0;

        for (String c : palavrasExtraidas) {

            List<Integer> value = docs.get(c);

            for (Integer vl : value) {

                double v = matriz.get(aux, vl) + 1.0;
                matriz.set(aux, vl, v);
            }
            aux++;
        }

        
    }

    public void matrizTFIDF() {

        int i = 0;
        
        for (String c : palavrasExtraidas) {

            for (int j = 0; j < numeroDoc; j++) {


                
                matriz.set(i, j, TF_IDF.calculoTFIDF(matriz.get(i, j), (double) palavrasExtraidasDoc.get(j), (double) numeroDoc, (double) palavraFreqDoc.get(c).size()));
            }
            i++;
        }
        
        matriz.print(0, 3);
    }

    public void similaridade(String w1, String w2) {

        Set<String> le1 = bagOfWordsLeft.get(w1);
        

        Set<String> sDw1 = bagOfWordsRight.get(w1);
        Set<String> sDw2 = bagOfWordsRight.get(w2);

        Set<String> sWD = new HashSet<>();
        for (String sdw1 : sDw1) {

            for (String sdw2 : sDw2) {
                if (sdw1.equals(sdw2)) {
                    sWD.add(sdw2);
                }
            }
        }

        double simD = 0.0;
        int linha = 0;
        int coluna = 0;
        for (String c : palavrasExtraidas) {
            coluna = 0;
            for (String sdw1 : sWD) {
                if (c.equals(sdw1)) {
                    System.out.println(linha + " " + coluna);
                    simD += matriz.get(linha, coluna);
                }

                coluna++;

                if (coluna > numeroDoc) {
                    break;
                }
            }

            linha++;

            if (linha > palavrasExtraidas.size()) {
                break;
            }
        }

        Set<String> sEw1 = bagOfWordsLeft.get(w1);
        Set<String> sEw2 = bagOfWordsLeft.get(w2);

        Set<String> sWE = new HashSet<>();
        for (String sew1 : sEw1) {

            for (String sew2 : sEw2) {
                if (sew1.equals(sew2)) {
                    sWE.add(sew2);
                }
            }
        }

        double simE = 0.0;
        linha = 0;
        coluna = 0;
        for (String c : palavrasExtraidas) {

            for (String sew1 : sWE) {
                if (c.equals(sew1)) {
                    simE += matriz.get(linha, coluna);
                }
                coluna++;
                if (coluna > numeroDoc) {
                    break;
                }
            }

            linha++;

            if (linha > palavrasExtraidas.size()) {
                break;
            }
        }

        double sim = (simE + simD);
        
        System.out.println(sim);
    }

    public static void main(String[] args) {
        Paradigmatica pp = new Paradigmatica();
        pp.diretorio = "Dataset.txt";

        String w1 = "microsoft";
        String w2 = "apple";

        pp.lerArquivo();
        pp.matrizFrequencia();
        pp.matrizTFIDF();

        pp.similaridade(w1, w2);
    }
}