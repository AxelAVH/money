package de.amo.money;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 17.04.14
 * Time: 21:52
 * To change this template use File | Settings | File Templates.
 */
public class Buchungszeile implements Cloneable {

    private static String Format_1822direkt_V1 = "1822direkt_V1";


    /** Null oder "", sonst:  "V1" oder "V2" usw.
     */
    public String   formatversion;
    public int      hauptbuchungsNr;    // "0" entspricht "nicht definiert"
    public int      umbuchungNr;
    public boolean  umbuchungsPro;

    // nur bei 1822direkt gibt es eine Uhrzeit. Bei der Speicherung wird die Uhrzeit an das Datumsfeld angehängt (20180131 09:57)
    public String datum             = "";   // Format 20171231
    public String zeit              = "";   // Format 04:09 Buchungszeit taucht erstmals bei 1822direkt auf


    public int    betrag            = 0;
    public String waehrung          = "";
    public int    saldo             = 0;
    public String buchungstext      = "";    // Gutschrift / Lastschrifteinzug / Dauerauftrag / ...
    public String quelleZiel        = "";
    public String verwendungszweck  = "";
    public int    pbetrag           = 0;
    public int    pSaldo            = 0;
    public String kommentar         = "";
    public String kategorie         = "";

    public int    saldoGeglaettet   = 0;

    public boolean isAllerersterSatz  = false;

    /* Während des Einlesens kann eine laufende Nummer vergeben werden, diese SOLLTE ABER ab "1" zählen, da "0" immer belegt ist und daher ohne Aussage
     */
    private String lfdNrWaehrendEinlesenS = "";

    /** teilt mit, ob ein Saldo eingelesen werden konnte (wenn nicht, wäre dies durch aufsaldieren zu ermitteln)
     */
    public boolean hasSaldo         = true;

    public void setLfdNrWaehrendEinlesen(int lfdNrWaehrendEinlesen) {
        lfdNrWaehrendEinlesenS = "000000000000" + lfdNrWaehrendEinlesen;
        lfdNrWaehrendEinlesenS  = lfdNrWaehrendEinlesenS.substring(lfdNrWaehrendEinlesenS.length() - 10);
    }

    public String getLfdNrWaehrendEinlesenS() {
        return lfdNrWaehrendEinlesenS;
    }

    /** Anhand dieses Keys sollen gleichartige Buchungsätze aus den Dateien der Banken erkannt werden.
     * ! 1822direkt liefert kein Saldo, daher wurde er hier komplett entfernt.
     */
    public String getUniquenessKey() {
        String s = "                    " + betrag;
        s = s.substring(s.length() - 20);
        // Der Saldo ist nicht geeignet, der wird nur
//        String t = "                    " + saldo;
//        t = t.substring(t.length() - 20);
//        String key = datum + "~" + zeit + "~" + s + "~" + t + "~" + quelleZiel.trim() + "~" + verwendungszweck.trim();

        // Verwendungszweck wird bei 1822direkt manchmal gekürzt: "Kredit Haus Axel und Susanne Mölle", owbwohl es auch Zeilen mit 35 Zeichen gibt
        String vzweckTmp = verwendungszweck.trim();
        if (vzweckTmp.length()>34) {
            vzweckTmp = vzweckTmp.substring(0,34);
        }

        String key = datum + "~" + zeit + "~" + s + "~" + quelleZiel.trim() + "~" + vzweckTmp;

        while (key.contains( "  " )) {
            key = key.replace( "  ", " " );
        }

        if (isUmbuchung()) {    // Umbuchungssätze tauchen nur in der database-Datei auf, müssen nicht zwischen Buchungssätzen und Databasezeilen gemerged werden
            key = hauptbuchungsNr + "~" + umbuchungNr + "~" + key;
        }

        return key;
    }

    /** Liefert einen Key, nach dem sich die Buchungssätze chronologisch sortieren lassen
     *  der Key ist auch für Sortierungen von Buchungssätzen geeignet, von denen einige aus der Datenbank kommen, andere soeben erst eingelesen wurden
     *  und noch keine Hauptbuchungsnummer usw. erhalten haben
     * @return
     */
    public String getSortKey() {
        String key1 = "                    " + hauptbuchungsNr;
        key1 = key1.substring( key1.length()-10 );
        String key2 = "                    " + umbuchungNr;
        key2 = key2.substring( key2.length()-10 );
        String key3 = umbuchungsPro == true ? "a" : "b";
        String sortkey = datum + "|" + key1 + "|" + key2 + "|" + key3 + "|" + getLfdNrWaehrendEinlesenS();
        return sortkey;
    }

    public boolean hasJahresumlage() {
        if (kommentar == null) {
            return false;
        }
        return kommentar.toLowerCase().startsWith("$umlage$");

    }

    public int getJahresumlageValue() {
        if (kommentar == null || !kommentar.toLowerCase().startsWith("$umlage$")) {
            return 0;
        }
        String value = kommentar.substring( "$umlage$".length() );
        value = value.replace( ",", "." );
        if (value.contains(" ")) {
            value = value.substring(0, value.indexOf(" "));
        }
        double d = Double.valueOf( value );
        BigDecimal bd = new BigDecimal( d );
        bd = bd.movePointRight( 2 );
        return bd.intValue();
    }


    public boolean isUmbuchung() {
        return umbuchungNr > 0;
    }


    /** Achtung: die Umbuchungsnummer wird erst beim Einhängen in die Liste erzeugt
     */
    public List<Buchungszeile> createSplittbuchungen(int betrag, String kategorie, String kommentar) {
        Buchungszeile pro, contra;
        try {
            pro    = (Buchungszeile) this.clone();
            contra = (Buchungszeile) this.clone();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pro.pbetrag         = 0;
        contra.pbetrag      = 0;
        pro.betrag          = -betrag;
        contra.betrag       = betrag;
        pro.kategorie       = this.kategorie;
        contra.kategorie    = kategorie;
        pro.kommentar       = kommentar;
        contra.kommentar    = kommentar;
        pro.umbuchungsPro   = true;
        contra.umbuchungsPro=false;
        List<Buchungszeile> ret = new ArrayList<Buchungszeile>();
        ret.add(pro);
        ret.add(contra);
        return ret;
    }

    public static Buchungszeile fromDatabaseZeile(String zeile) {
        Buchungszeile b = new Buchungszeile();

        String[] columns    = getColumns(zeile);
        b.formatversion     = columns[0];

        if ("V1".equals(b.formatversion)) {
            b.formatversion     = columns[0];
            b.hauptbuchungsNr   = Integer.parseInt(columns[1]);
            if (!columns[2].startsWith("0")) {
                b.umbuchungNr   = Integer.parseInt(columns[2].substring(0, columns[2].length() - 1));
                b.umbuchungsPro = "a".equals(columns[2].substring(columns[2].length() - 1));
            }
            b.kategorie         = columns[3];
            b.datum             = columns[4];

            if (b.datum.length() > 8) {
                b.zeit          = columns[4].substring(9,14);
                b.datum = b.datum.substring(0,8);
            }

            b.quelleZiel        = columns[5];
            b.buchungstext      = columns[6];
            b.verwendungszweck  = columns[7];
            b.betrag            = Integer.parseInt(columns[8]);
            b.waehrung          = columns[9];
            b.saldo             = Integer.parseInt(columns[10]);

            b.pbetrag           = Integer.parseInt(columns[11]);
            b.pSaldo            = Integer.parseInt(columns[12]);
            b.kommentar         = columns[13];
        } else {
            b.datum             = columns[0];
            b.quelleZiel        = columns[1];
            b.buchungstext      = columns[2];
            b.verwendungszweck  = columns[3];
            b.betrag            = Integer.parseInt(columns[4]);
            b.waehrung          = columns[5];
            b.saldo             = Integer.parseInt(columns[6]);

            b.pbetrag           = Integer.parseInt(columns[7]);
            b.pSaldo            = Integer.parseInt(columns[8]);
            b.kommentar         = columns[9];
        }

        return b;
    }

    private int fromDouble(double d) {
        BigDecimal bd = new BigDecimal(d);
        bd = bd.movePointRight(2);
        return bd.intValue();
    }

    private double fromInt(int i) {
        BigDecimal bd = new BigDecimal(i);
        bd = bd.movePointLeft(2);
        return bd.doubleValue();
    }

    public double getBetragAsDouble() {
        return fromInt(betrag);
    }

    public void setBetrag(double betrag) {
        this.betrag = fromDouble(betrag);
    }

    public double getSaldoAsDouble() {
        return fromInt(saldo);
    }

    public void setSaldo(double saldo) {
        this.saldo = fromDouble(saldo);
    }

    public double getPBetrag() {
        return fromInt(pbetrag);
    }

    public double getPBetragAsDouble() {
        return fromInt(pbetrag);
    }

    public void setPBetrag(double pbetrag) {
        this.pbetrag = fromDouble(pbetrag);
    }

    public double getPSaldoAsDouble() {
        return fromInt(pSaldo);
    }

    public double getSaldoGeglaettetAsDouble() {
        return fromInt(saldoGeglaettet);
    }

    public void setPSaldo(double pSaldo) {
        this.pSaldo = fromDouble(pSaldo);
    }

    public int getSaldoGeglaettet() {
        return saldoGeglaettet;
    }

    public void setSaldoGeglaettet( int saldoGeglaettet ) {
        this.saldoGeglaettet = saldoGeglaettet;
    }

    public void setSaldoGeglaettet( double saldoGeglaettet ) {
        this.saldoGeglaettet = fromDouble( saldoGeglaettet );
    }

    public static Buchungszeile fromIngDibaZeile( String zeile, boolean isFormatApril2018) {
        Buchungszeile b = new Buchungszeile();
        String[] columns = getColumns(zeile);
        String s            = columns[0];
        b.datum             = s.substring(6,10) + s.substring(3,5) + s.substring(0,2);
        b.quelleZiel        = columns[2];
        b.buchungstext      = columns[3];
        b.verwendungszweck  = columns[4];
        if (!isFormatApril2018) {
            b.betrag = readLong( columns[5] );
            b.saldo  = readLong( columns[7] );
        } else {
            b.saldo  = readLong( columns[5] );
            b.betrag = readLong( columns[7] );
        }
        b.waehrung          = columns[6];
        b.pbetrag           = b.betrag;
        return b;
    }

    public static Buchungszeile from1822direktZeile(String zeile) {
        Buchungszeile b = new Buchungszeile();
        String[] columns = getColumns(zeile);
        String s = columns[1];
        b.datum = s.substring(6, 10) + s.substring(3, 5) + s.substring(0, 2);
        b.zeit = s.substring(11, 16);
        b.quelleZiel = columns[8] + " - " + columns[7];
        b.buchungstext = columns[14];
        b.verwendungszweck = columns[13];

        // 1822direkt überträgt auch Buchungen, die noch keine Wertstellung haben.
        // in einer folgenden Datei tauchen diese dann noch einmal auch, dabei kann sich Datum/Zeit und auch Buchungstag verändert haben
        // was zum doppelten Anlegen führt.
        if ("".equals(columns[3].trim())) {
            return null;
        }
        b.betrag = readLong(columns[4]);
        b.waehrung          = "EUR";
        b.hasSaldo          = false;

        return b;
    }

    private static int readLong(String ds) {
        String orig = ds;
        if (ds==null) {
            return 0;
        }
        ds = ds.trim();
        if ("".equals(ds)) {
            return 0;
        }

        ds = ds.replace(".","");

        int pos = ds.indexOf(",");
        if (pos <0) {
            ds += ",00";
        } else if (pos == ds.length()-1) {
            ds += "00";
        } else if (pos == ds.length()-2) {
            ds += "0";
        }
        ds = ds.replace(",", "");
        int ret = 0;
        try {
            ret = Integer.parseInt(ds);
        } catch (Exception e ) {
            System.out.println("Fehler bei <"+orig+">");
        }
        return ret;
    }

    public String toDatabaseZeile() {
        String[] out = new String[14];
        out[0] = "V1";
        out[1] = "" + hauptbuchungsNr;
        out[2] = "" + umbuchungNr;
        if (umbuchungNr > 0) {
            if (umbuchungsPro) {
                out[2] += "a";
            } else {
                out[2] += "b";
            }
        }
        out[3] = kategorie;
        out[4] = datum;
        if (zeit != null && zeit.length() == 5) {
            out[4] = datum + " " + zeit;
        }
        out[5] = quelleZiel;
        out[6] = buchungstext;
        out[7] = verwendungszweck;
        out[8] = "" + betrag;
        out[9] = waehrung;
        out[10] = "" + saldo;
        out[11] = "" + pbetrag;
        out[12] = "" + pSaldo;
        out[13] = kommentar;

        String zeile = "";
        for (int i = 0; i < out.length; i++) {
            if (out[i] == null) {
                out[i] = "";
            }
            out[i] = out[i].replace( ";", ":" );        // damit das Format sicher einlesbar bleibt
//            out[i] = "\"" + out[i] + "\"";
            if (i != 0) {
                out[i] = ";" + out[i];
            }
            zeile += out[i];
        }
        return zeile;
    }

    public String toPexportZeile() {
        if (pbetrag == 0) {
            return null;
        }
        String[] out = new String[7];
        out[0] = datum;
        out[1] = quelleZiel;
        out[2] = buchungstext;
        out[3] = verwendungszweck;
//        out[4] = "" + betrag;
        out[4] = waehrung;
//        out[6] = "" + saldo;
        out[5] = "" + pbetrag;
        out[6] = "" + pSaldo;
//        out[9] = kommentar;

        String zeile = "";
        for (int i = 0; i < out.length; i++) {
            if (out[i] == null) {
                out[i] = "";
            }
            out[i] = out[i].replace( ";", ":" );        // damit das Format sicher einlesbar bleibt
            if (i != 0) {
                out[i] = ";" + out[i];
            }
            zeile += out[i];
        }
        return zeile;
    }

    public String toShow() {
        return datum.substring(6, 8) + "." + datum.substring(4, 6) + "." + datum.substring(0, 4) + " | " +
                (hauptbuchungsNr + " | " + umbuchungNr + " | ") +
                (quelleZiel + "                                                 ").substring(0, 30) + " | " +
                (buchungstext + "                                               ").substring(0, 20) + " | " +
                (verwendungszweck + "                                           ").substring(0, 40) + " | " +
                formatLongForEuroOutput(betrag) + "|" +
                formatLongForEuroOutput(saldo) + "|" +
                formatLongForEuroOutput(pbetrag) + "|" +
                formatLongForEuroOutput(pSaldo) + "|" +
                kommentar;
    }


    public static int readDatabaseFile(String filename, SortedMap<String, Buchungszeile> map, List<Buchungszeile> sortierteBuchungszeilen) throws Exception {

        InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"windows-1252");
        BufferedReader    br     = new BufferedReader(reader);

        String zeile;
        int satzNrErwartet = 0;
        boolean isFirst = true;
        while ((zeile = br.readLine()) != null) {
            Buchungszeile b = Buchungszeile.fromDatabaseZeile(zeile);
            if (isFirst) {
                isFirst             = false;
                b.isAllerersterSatz = true;
            }
            map.put(b.getUniquenessKey(), b); // durch den Database-Datensatz wird eine bereits bestehende Zeile überschieben
            sortierteBuchungszeilen.add(b);
        }

        reader.close();

        return satzNrErwartet;
    }

//    /**
//     * @return liefert die Buchungszeile für die letzte eingelesene Datei-Zeile zurück (was chronologisch die erste Buchung ist (bei ING-DIBA)
//     */
//    public static Buchungszeile readUmsatzFile(File file, SortedMap<String, Buchungszeile> map) throws Exception {
//
//        InputStreamReader reader            = new InputStreamReader(new FileInputStream(file),"windows-1252");
//        BufferedReader br                   = new BufferedReader(reader);
//        Buchungszeile  buchungszeileLast    = null;
//        String          zeile               = null;
//        boolean         startFound          = false;
//
//        while ((zeile = br.readLine()) != null) {
//            if (zeile.startsWith("\"Buchung\"")) {
//                startFound = true;
//                // todo: weitere Spalten absichern bzgl. der Erwartung
//                continue;
//            }
//
//            if (!startFound) {
//                continue;
//            }
//
//            Buchungszeile b = Buchungszeile.fromIngDibaZeile(zeile);
//            buchungszeileLast =b;
//            System.out.println(zeile);
//            if (!map.containsKey(b.getUniquenessKey())) {   // durch den Umsatz-Datensatz wird eine bereits bestehende Zeile NICHT überschieben
//                //System.out.println("KEY: " + b.getUniquenessKey());
//                map.put(b.getUniquenessKey(), b);
//            }
//        }
//        br.close();
//        reader.close();
//
//        return buchungszeileLast;
//    }
//

    public static void writeDatabaseFile(File file, List<Buchungszeile> zeilen) throws Exception {

        Writer w = new OutputStreamWriter(new FileOutputStream(file), "windows-1252");
        BufferedWriter out = new BufferedWriter(w);

//        PrintWriter printWriter = IOTools.openOutputFile(filename);

        for (Buchungszeile buchungszeile : zeilen) {
            out.write(buchungszeile.toDatabaseZeile());
            out.write("\n");
        }
        out.close();
        //IOTools.closeOutputFile(printWriter);
    }

    public static void writePExportFile(String filename, List<Buchungszeile> zeilen) throws Exception {

        String lineFeed = new String("\n"); // Unix LineFeed

        Writer w = new OutputStreamWriter(new FileOutputStream(filename), "windows-1252");
        BufferedWriter out = new BufferedWriter(w);

        for (Buchungszeile buchungszeile : zeilen) {
            if (buchungszeile.pbetrag != 0) {
                out.write(buchungszeile.toPexportZeile());
                out.write(lineFeed);
            }
        }
        out.close();

/*        PrintWriter printWriter = IOTools.openOutputFile(filename);

        for (Buchungszeile buchungszeile : zeilen) {
            if (buchungszeile.pbetrag != 0) {
                printWriter.println(buchungszeile.toPexportZeile());
            }
        }

        IOTools.closeOutputFile(printWriter);
*/
    }

    public static String formatLongForEuroOutput(int l) {
        int maxL = 10;
        String s = "                    " + l;
        s = s.substring(s.length()-(maxL-2));
        String ganze       = s.substring(0, s.length() - 2);
        String fraktionale = s.substring(s.length() - 2);
        fraktionale = fraktionale.replace(" ", "0");
        if (ganze.endsWith(" ")) {
            ganze = ganze.substring(0,ganze.length()-1) + "0";
        }
        return ganze + "," + fraktionale;
    }

    public static String[] getColumns(String zeile) {

        List<String> words = new ArrayList<>();

        zeile +=";";

        String word = "";
        boolean tuettelMode = false;

        for (int i = 0; i < zeile.length(); i++) {

            char c = zeile.charAt(i);

            if (';' == c) {
                if (!tuettelMode) {
                    word = word.trim();
                    words.add(word);
                    word = "";
                    continue;
                } else {

                }

            }

            if (c == '\"') {
                if (!tuettelMode) {
                    tuettelMode = true;
                    continue;
                } else {
                    tuettelMode = false;
                    continue;
                }
            }

            word += c;
        }

        return words.toArray(new String[0]);

    }



    public static String[] getColumnsAlt(String zeile) {
        if (!zeile.startsWith("\"") && !zeile.endsWith("\"")) {
            return new String[0];
        }
        zeile = zeile.substring(1);                     // wegen dem ersten "
        zeile = zeile.substring(0, zeile.length() - 1); // wegen dem letzten "
        if (zeile.endsWith("\";\"")) {
            zeile += "$null$";
        }
        String[] split = zeile.split("\";\"");
        if ("$null$".equals(split[split.length-1])) {
            split[split.length-1] = "";
        }
        // 11.10.2015: Trimmern IMMER
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();

        }
        return split;
    }


    @Override
    public String toString() {
        return     "Haupt: "     + hauptbuchungsNr
                + " Um: "       + umbuchungNr
                + " UmPro: "    + umbuchungsPro
                + " Datum: "    + datum
                + " Betrag: "   + betrag
                + " Saldo: "    + saldo
                + " Text: "     + buchungstext;
    }

    public static void main( String[] args) {
//        System.out.println(formatLongForEuroOutput(1));
//        System.out.println(formatLongForEuroOutput(11));
//        System.out.println(formatLongForEuroOutput(111));
String s = "01.02.2017;01.02.2017;Susanne Moeller;Dauerauftrag/Terminueberweisung;\"Unterhalt: Tabea 481, Julius 481,  Leonard 430, Susanne 555, Florian  230, gemeinsame Verpflichtungen 5  \";-2.185,00;EUR;7.384,92;EUR";
        System.out.println(s);
        String[] columns = getColumns(s);
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            System.out.println(column);
        }


        /*
01.02.2017;01.02.2017;Dr. Axel Moeller;Lastschrifteinzug;"Sparen  ";-200,00;EUR;9.569,92;EUR
01.02.2017;01.02.2017;Sylva Janiszewski;Überweisung;"Rechnung 2017/97, Mandant 63999  ";-381,04;EUR;9.769,92;EUR
31.01.2017;31.01.2017;E.ON Energie Deutschland;Lastschrifteinzug;"VK 232003032058 Alte Dorfstr 38 Vie  rhoefen ABSCHLAG 01 Strom WIR SAGEN  DANKE  ";-31,00;EUR;10.150,96;EUR
30.01.2017;
30.01.2017;SUSANNE MOLLER;Gutschrift;"Ruckzahlung Autoversicherung Susi  ";75,30;EUR;10.181,96;EUR
30.01.2017;30.01.2017;REWE 803 (895) See;Lastschrifteinzug;"REWE SAGT DANKE. 41400803//Seevetal  Maschen/DE
         */

        Buchungszeile buchungszeile = new Buchungszeile();
        buchungszeile.kommentar = "$umlage$123,45 noch ein Text";
        int value = buchungszeile.getJahresumlageValue();
        System.out.println(value);
        buchungszeile.kommentar = "$umlage$123,45 ";
        value = buchungszeile.getJahresumlageValue();
        System.out.println(value);
        buchungszeile.kommentar = "$umlage$123,45";
        value = buchungszeile.getJahresumlageValue();
        System.out.println(value);

    }

}
