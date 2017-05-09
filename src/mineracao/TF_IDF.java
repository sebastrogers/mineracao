package mineracao;


public class TF_IDF {
    
     public static Double calculoTFIDF(Double palavra_pdoc, Double qtd_palavrasDoc, 
    		 Double qtd_doc, Double palavra_docs){
        
           
        return (palavra_pdoc/qtd_palavrasDoc)*(Math.log(qtd_doc/palavra_docs));
    }
}
