package de.amo.money;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by private on 09.02.2017.
 */
public class UmsatzReader_INGDIBA implements UmsatzReaderIfc {


    public UmsatzReader_INGDIBA() {
    }

    public static boolean isMyFiletype(File file) {

        String fname = file.getName();

        return fname.startsWith("Umsatzanzeige_");
    }

    public String ermittleKontonummer(File file) throws Exception {

        InputStreamReader reader            = new InputStreamReader(new FileInputStream(file),"windows-1252");
        BufferedReader br                   = new BufferedReader(reader);
        String          zeile               = null;
        String          kontoZeilenAnfang1  = "\"Konto\";\"";
        String          kontoZeilenAnfang2  = "Konto;";
        String          kontoZeilenAnfang3  = "IBAN;";          // Format ab Ende April 2018
        String          kontonummerTmp      = null;

        while ((zeile = br.readLine()) != null) {

            if (zeile.startsWith(kontoZeilenAnfang1)) {
                kontonummerTmp = zeile.substring(kontoZeilenAnfang1.length());
                kontonummerTmp = kontonummerTmp.replace("\"","");

                if ("Girokonto: 5407433753".equals(kontonummerTmp)) {   // IngDIBA hat nach Einführung der SEPA-Nummern die Darstellung verändert
                    kontonummerTmp = "DE77500105175407433753";
                }

                break;
            }
            if (zeile.startsWith(kontoZeilenAnfang2)) {
                kontonummerTmp = zeile.substring(kontoZeilenAnfang2.length());

                if ("Girokonto: 5407433753".equals(kontonummerTmp)) {   // IngDIBA hat nach Einführung der SEPA-Nummern die Darstellung verändert
                    kontonummerTmp = "DE77500105175407433753";
                }

                break;
            }
            if (zeile.startsWith(kontoZeilenAnfang3)) {
                kontonummerTmp    = zeile.substring(kontoZeilenAnfang3.length()).replace(" ","");
                break;
            }
        }
        br.close();
        reader.close();
        return  kontonummerTmp;
    }

/* Liest alle Buchungssatz-Zeilen der Datei in Moneytransient.buchungszeilen ein

 */
    public Buchungszeile readUmsatzFile(File file, MoneyTransient moneyTransient) throws Exception {

        InputStreamReader reader            = new InputStreamReader(new FileInputStream(file),"windows-1252");
        BufferedReader br                   = new BufferedReader(reader);
        Buchungszeile  buchungszeileLast    = null;
        String          zeile               = null;
        boolean         startFound          = false;
        String          kontoZeilenAnfang1  = "\"Konto\";\"";
        String          kontoZeilenAnfang2  = "Konto;";
        String          kontoZeilenAnfang3  = "IBAN;";
        boolean         isFormatApril2018   = false;
        List<Buchungszeile> bZeilenDesFiles = new ArrayList<>();

        while ((zeile = br.readLine()) != null) {

            if (zeile.startsWith(kontoZeilenAnfang1) || zeile.startsWith(kontoZeilenAnfang2) || zeile.startsWith(kontoZeilenAnfang3)) {

                String kontonummerTmp;

                if (zeile.startsWith(kontoZeilenAnfang1)) {
                    kontonummerTmp = zeile.substring(kontoZeilenAnfang1.length());
                    kontonummerTmp = kontonummerTmp.replace("\"", "");
                } else if (zeile.startsWith( kontoZeilenAnfang2 )){
                    kontonummerTmp = zeile.substring(kontoZeilenAnfang2.length());
                } else {
                    kontonummerTmp = zeile.substring(kontoZeilenAnfang3.length()).replace(" ","");
                    isFormatApril2018 = true;
                }

                if ("Girokonto: 5407433753".equals(kontonummerTmp)) {   // IngDIBA hat nach Einführung der SEPA-Nummern die Darstellung verändert
                    kontonummerTmp = "DE77500105175407433753";
                }

                if (moneyTransient.getKontonnr() == null || moneyTransient.getKontonnr().equals("")) {
                    moneyTransient.setKontonnr(kontonummerTmp);
                } else {
                    if (!moneyTransient.getKontonnr().equals(kontonummerTmp)) {
                        throw new RuntimeException("Einlesen von Konto " + kontonummerTmp + " auf " + moneyTransient.getKontonnr()+" darf nicht sein!!");
                    }
                }
            }
            if (zeile.startsWith("\"Buchung\"") || zeile.startsWith("Buchung")) {
                startFound = true;
                // todo: weitere Spalten absichern bzgl. der Erwartung
                continue;
            }

            if (!startFound) {
                continue;
            }

            Buchungszeile b = Buchungszeile.fromIngDibaZeile(zeile, isFormatApril2018);
            buchungszeileLast =b;
            System.out.println(zeile);
            if (!moneyTransient.getBuchungszeilen().containsKey(b.getUniquenessKey())) {   // durch den Umsatz-Datensatz wird eine bereits bestehende Zeile NICHT überschieben
                //System.out.println("KEY: " + b.getUniquenessKey());
                moneyTransient.getBuchungszeilen().put(b.getUniquenessKey(), b);
                bZeilenDesFiles.add(b);
            }
        }
        br.close();
        reader.close();

        int lfd = 0;
        for (int i = bZeilenDesFiles.size() - 1; i >= 0; i--) {
            Buchungszeile b = bZeilenDesFiles.get(i);
            lfd++;                              // Diese Nummer soll bei "1" beginnend hochzählen
            b.setLfdNrWaehrendEinlesen(lfd);
        }


        return buchungszeileLast;
    }

}
