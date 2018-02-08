package com.mackleaps.spiders;

import com.sun.org.apache.xpath.internal.SourceTree;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.LinkedList;
import java.util.List;



public class SpiderLeg {

    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
    private static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> substancias = new LinkedList<String>();
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
        Elements tdsOnPage = htmlDocument.select("td");

        for (int i = 6; i < tdsOnPage.size(); i = i + 3){

            Element td = tdsOnPage.get(i);

            System.out.println("TD INTEIRO: ");
            System.out.println(td);

            System.out.println("AÇÃO: ");
            System.out.println(td.select("strong").text());

            System.out.println("SUBSTANCIA:");
            String []elementosDaTagP = null;
            int numerosDeP = td.select("p").size();

            for (int j = 1; j < numerosDeP; j++){
                elementosDaTagP = td.select("p").get(j).text().split(" ");
                if (elementosDaTagP[0].equals("Lista")){
                    System.out.println(elementosDaTagP[elementosDaTagP.length-1]);
                }
            }

            System.out.println("");
            System.out.println(" === ");
            System.out.println("");
        }



        //System.out.println(tdsOnPage.get(6).select("p"));

//        for (int i = 6 ; i < tdsOnPage.size(); i = i + 3){
//            System.out.println(tdsOnPage.get(i).select("strong").first().text());
//        }

        System.out.println("");
        System.out.println("");

        return true;

    }

    /**
     * Performs a search on the body of on the HTML document that is retrieved.
     * This method should only be called after a successful crawl.
     *
     * @param searchWord - The word or string to look for
     * @return whether or not the word was found
     */
    public boolean searchForWord(String searchWord) {
        // Defensive coding. This method should only be used after a successful crawl.
        if (this.htmlDocument == null) {
            System.out.println("ERROR! Call crawl() before performing analysis on the document");
            return false;
        }
        System.out.println("Searching for the word " + searchWord + "...");
        String bodyText = this.htmlDocument.body().text();
        return bodyText.toLowerCase().contains(searchWord.toLowerCase());
    }

    public List<String> getSubstancias() {
        return this.substancias;
    }

}
