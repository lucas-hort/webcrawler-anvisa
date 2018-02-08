package com.mackleaps.spiders;

/**
 *
 * @author Lucas Hort
 */
public class SpiderTest {

    /**
     * This is our test. It creates a spider (which creates spider legs) and
     * crawls the web.
     *
     * @param args - not used
     */
    public static void main(String[] args) {
        Spider spider = new Spider();
        spider.search("http://portal.anvisa.gov.br/lista-de-substancias-sujeitas-a-controle-especial",
                        "MDMA");
    }
}

/*
* 1 - EXTRAIR OS DADOS DO SITE
* 2 - PROCURAR POR TABLE, TBODY, TD
* 3 - SEPARAR OS TDS
* 4 -
*
* */