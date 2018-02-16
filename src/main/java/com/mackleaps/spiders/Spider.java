package com.mackleaps.spiders;

public class Spider {

    /**
     * Our main launching point for the Spider's functionality. Internally it
     * creates spider legs that make an HTTP request and parse the response (the
     * web page).
     *
     * @param url - The starting point of the spider
     */
    public void search(String url) {

        SpiderLeg leg = new SpiderLeg();

        leg.crawl(url); // Lots of stuff happening here. Look at the crawl method in


        System.out.println("\n**Done** Visited " + url);
    }

}