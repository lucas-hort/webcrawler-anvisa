package com.mackleaps.spiders;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;


public class SpiderLeg {

    private HashMap<String, List<String>> hashLists = new HashMap<String, List<String>>();

    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
    private static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> substances = new LinkedList<String>();
    private Document htmlDocument;

    /**
     * This performs all the work. It makes an HTTP request, checks the
     * response, and then gathers up all the links on the page. Perform a
     * searchForWord after the successful crawl
     *
     * @param url - The URL to visit
     * @return whether or not the crawl was successful
     */
    public boolean crawl(String url) {

        Connection connection = null;
        Document htmlDocument = null;

        Proxy proxy = new Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved("172.16.0.10", 3128)
        );

        try {
            connection = Jsoup.connect(url).userAgent(USER_AGENT).proxy(proxy);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }

        try {
            htmlDocument = connection.get();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        this.htmlDocument = htmlDocument;
        if (connection.response().statusCode() == 200) // 200 is the HTTP OK status code
        // indicating that everything is great.
        {
            System.out.println("\n**Visiting** Received web page at " + url);
        }
        if (!connection.response().contentType().contains("text/html")) {
            System.out.println("**Failure** Retrieved something other than HTML");
            return false;
        }


        //Extract the <td> tags in the table of substances
        Elements tdsOnPage = htmlDocument.select("td");


        /* The <td> we want to extract starts with index 6
         * and iterate through the table by 3 for the
         * reason of having 3 columns
         */
        final int firstSubstanceTD = 6;
        for (int i = tdsOnPage.size()-1; i >= firstSubstanceTD; i -= 3){


            printHashMap();


            Element td = tdsOnPage.get(i);
            System.out.println("TD inteiro: " + td);

            Elements allParagraphs = td.select("p");

            String action = "";

            System.out.println("== == TD "+((tdsOnPage.size()-i)/3+1)+" == =="); //Show <td> index
            for(Element p : allParagraphs){

                if(isAction(p)){
                    action = p.select("strong").first().text().toLowerCase();
                    System.out.println("Ação da Anvisa: " + action);
                }else{
                    try{
                        //Add substances on the list
                        if (action.trim().equals("inclusão")){
                            includeSubstances(extractSubstances(p,action));
                        }
                        //Remove substances of the list
                        if (action.trim().equals("exclusão")){
                            excludeSubstances(extractSubstances(p,action));
                        }
                        //Swap substances of the lists
                        if (action.trim().equals("transferência")){
                            transferSubstances(extractSubstances(p,action));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }

            printHashMap();

            System.out.println("");
            System.out.println(" === ");
            System.out.println("");
        }



        return true;
    }


    public boolean isAction(Element e){
        return e.select("strong").hasText();
    }

    /**
     * This extract the name of the list and the substances
     * of a paragraph.
     * This only works if it starts with "Lista", "Adendo" is not included.
     *
     * @param p - the paragraph to be splitted
     * @param action - action to be executed
     * @return the name of the list and all substances
     */

    public List<String> extractSubstances(Element p, String action){
        String []splitParagraph = null;
        String []firstSplit = null;
        String []secondSplit = null;

        String nameOfList = "";

        List<String> substances = new ArrayList<String>();

        splitParagraph = p.text().split(" ");
        if(splitParagraph[0].equals("Lista")){
            splitParagraph = p.text().split(":");

            firstSplit = splitParagraph[0].split(" "); //Name of list that substances will be added

            if (splitParagraph[1].contains(";")){
                secondSplit = splitParagraph[1].split(";");//The substances extracted
            }else{
                secondSplit = splitParagraph[1].split(" ");
            }



            if (firstSplit[0].equals("Lista")){
                System.out.println(action+" na lista " + firstSplit[1]);

                if (firstSplit[1].length() == 2){
                    nameOfList = firstSplit[1].trim();
                }else{
                    nameOfList = firstSplit[1].substring(1,3);
                }
                substances.add(nameOfList);

                //The list that will receive new substance
                if(action.trim().equals("transferência")){
                    nameOfList = firstSplit[5].substring(1,3);
                    substances.add(nameOfList);
                }

                //Split by 'e'


                for (int i = 0; i < secondSplit.length; i++ ){
                    String filterSubstance = secondSplit[i]
                                            .replace(".", "")
                                            .replace(",","")
                                            .trim();
                    if (!filterSubstance.equals("e") && filterSubstance.length() != 0){
                        substances.add(filterSubstance);
                        System.out.println(filterSubstance);
                    }
                }
            }
        }
        return substances;
    }

    /**
     * Include all substances in the HashMap
     *
     * @param substances - Substances to be added
     */
    public void includeSubstances(List<String> substances){
        if(!substances.isEmpty()){
            String nameOfList = substances.remove(0);

            if (hashLists.containsKey(nameOfList)) {
                for (String substance : substances){
                    //Avoid repetition
                    if (!hashLists.get(nameOfList).contains(substance))
                        hashLists.get(nameOfList).add(substance);
                }
            }else{
                hashLists.put(nameOfList,substances);
            }
            System.out.println("");
        }
    }


    /**
     * Remove all substances of the HashMap
     *
     * @param substances - Substances to be removed
     */
    public void excludeSubstances(List<String> substances){
        if(!substances.isEmpty()){
            String nameOfList = substances.remove(0);

            if (hashLists.containsKey(nameOfList)) {
                hashLists.get(nameOfList).removeAll(substances);
            }

            System.out.println("");
        }
    }


    /**
     * Swap substances of the list on HashMap
     *
     * @param substances - Substances to be swapped
     */
    private void transferSubstances(List<String> substances) {

        String []splitSubstance = null;

        if(!substances.isEmpty()){
            //Split substances that are linked with "e"    eg. 'Fluor e Testosterone'
            for (int i = 2; i < substances.size() ; i++){
                splitSubstance = substances.get(i).split(" ");
                substances.remove(substances.get(i));
            }
            for (int j = 0; j < splitSubstance.length ; j++){
                if (!splitSubstance[j].equals("e")){
                    substances.add(splitSubstance[j]);
                }
            }

            //Swap lists on the HashMap
            for (int k = 2; k < substances.size() ; k++){
                String oldList = substances.get(0);
                String newList = substances.get(1);
                String substance = substances.get(k);

                //Remove from the list
                if (hashLists.containsKey(oldList)){
                    hashLists.get(oldList).remove(substance);
                }

                //Add in another list
                if (hashLists.containsKey(newList)){
                    hashLists.get(newList).add(substance);
                }else{
                    List<String> tempList = new ArrayList<String>();
                    tempList.add(substance);
                    hashLists.put(newList, tempList);
                }
            }
        }
    }

    public void printHashMap(){
        //Loop hashmap
        Set<String> chaves = hashLists.keySet();
        System.out.println("\n========= hashmap =======");
        for (String chave : chaves){
            System.out.println(chave + hashLists.get(chave));
        }
        System.out.println("");

    }

}
