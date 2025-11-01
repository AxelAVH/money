package de.amo.money;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/** !! Die Dateien von 1822direkt enthalten kein Saldo. Dafür aber eine Buchungszeit (neben dem Datum)
 *    Daher genügt ein einmaliges striktes Sortieren nach Datum und Zeit, die damit einmalig festgelegte Reihenfolge
 *    wird mit der Buchungsnummer in der Buchungszeile dann für die Ewigkeit fixiert
 *
 * Created by private on 09.02.2017.
 */
public class UmsatzReader_1822direkt implements de.amo.money.UmsatzReaderIfc {


    List<Buchungszeile> buchungszeilenNeu;

    public UmsatzReader_1822direkt() {
        buchungszeilenNeu = new ArrayList<>();
    }

    public static boolean isMyFiletype(File file) {
        String fname = file.getName();

        return fname.startsWith("umsaetze-");
    }


    public String ermittleKontonummer(File file) throws Exception {

        InputStreamReader reader            = new InputStreamReader(new FileInputStream(file),"windows-1252");
        BufferedReader    br                = new BufferedReader(reader);
        String            zeile             = null;
        boolean           isFirstLine       = true;
        String            kontonummerTmp    = null;

        while ((zeile = br.readLine()) != null) {

            if (isFirstLine) {
                if (!zeile.startsWith("Kontonummer;Datum/Zeit;Buchungstag;Wertstellung;Soll/Haben;Buchungsschlüssel;")) {
                    throw new Exception("Unerwartete Anfang der ersten Zeile,\n" +
                            "erwartet\n+" +
                            "Kontonummer;Datum/Zeit;Buchungstag;Wertstellung;Soll/Haben;Buchungsschlüssel;\n" +
                            "erhalten\n" +
                            zeile);
                }
                isFirstLine = false;
                continue;
            }

            int pos        = zeile.indexOf(";");
            kontonummerTmp = zeile.substring(0, pos);
            break;
        }
        br.close();
        reader.close();
        return kontonummerTmp;
    }

    /**
     * Liest alle Buchungssatz-Zeilen der Datei in Moneytransient.buchungszeilen ein
     *
     * @return die unter den eingelesenen Buchungszeilen im Buchungsablauf erste Buchungszeile
     */
    public Buchungszeile readUmsatzFile(File file, MoneyTransient moneyTransient) throws Exception {

        InputStreamReader reader            = new InputStreamReader(new FileInputStream(file),"windows-1252");
        BufferedReader    br                = new BufferedReader(reader);
        Buchungszeile     buchungszeileLast = null;
        String            zeile             = null;
        boolean           isFirstLine       = true;
        List<Buchungszeile> bZeilenDesFiles = new ArrayList<>();

        while ((zeile = br.readLine()) != null) {

            if (isFirstLine) {
                if (!zeile.startsWith("Kontonummer;Datum/Zeit;Buchungstag;Wertstellung;Soll/Haben;Buchungsschlüssel;")) {
                    throw new Exception("Unerwarteter Anfang der ersten Zeile,\n" +
                            "erwartet\n+" +
                            "Kontonummer;Datum/Zeit;Buchungstag;Wertstellung;Soll/Haben;Buchungsschlüssel;\n" +
                            "erhalten\n" +
                            zeile);
                }
                isFirstLine = false;
                continue;
            }

            int    pos            = zeile.indexOf(";");
            String kontonummerTmp = zeile.substring(0, pos);

            if (moneyTransient.getKontonnr() == null || moneyTransient.getKontonnr().equals("")) {
                moneyTransient.setKontonnr(kontonummerTmp);
            } else {
                if (!moneyTransient.getKontonnr().equals(kontonummerTmp)) {
                    throw new RuntimeException("Einlesen von Konto " + kontonummerTmp + " auf " + moneyTransient.getKontonnr() + " darf nicht sein!!");
                }
            }

            Buchungszeile b   = Buchungszeile.from1822direktZeile(zeile);

            if (b == null) {
                // es gibt u.U. Zeilen ohne Wertstellungsdatum, die wiederholen sich in anderen Dateien mit anderem Aussehen, das macht Ärger
                continue;
            }
            buchungszeileLast = b;
            System.out.println(zeile);

            if (!moneyTransient.getBuchungszeilen().containsKey(b.getUniquenessKey())) {   // durch den Umsatz-Datensatz wird eine bereits bestehende Zeile NICHT überschieben
                //System.out.println("KEY: " + b.getUniquenessKey());
                moneyTransient.getBuchungszeilen().put(b.getUniquenessKey(), b);
                bZeilenDesFiles.add(b);
            }
        }

        int lfd = 0;
        for (int i = bZeilenDesFiles.size() - 1; i >= 0; i--) {
            Buchungszeile b = bZeilenDesFiles.get(i);
            lfd++;                              // Diese Nummer soll bei "1" beginnend hochzählen
            b.setLfdNrWaehrendEinlesen(lfd);
        }

        br.close();
        reader.close();

        return buchungszeileLast;
    }
}
