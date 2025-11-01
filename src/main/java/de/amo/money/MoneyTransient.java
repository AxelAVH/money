package de.amo.money;

import de.amo.tools.Datum;
import de.amo.tools.Environment;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by private on 06.09.2015.
 */
public class MoneyTransient {

    int     saldo   = 0;
    int     psaldo  = 0;
    String  message = "";
    boolean isSaved = true;
    String  kontonnr = "";

//    String monatsAbgrenzDatum        = null;

    File lastBackupDatabaseFile;

    /**
     * key: Unigueness-String der Buchungszeile
     * value: die Buchungszeile
     */
    SortedMap<String, Buchungszeile>    buchungszeilen          = new TreeMap<String, Buchungszeile>();
    List<Buchungszeile>                 sortierteBuchungszeilen = new ArrayList<Buchungszeile>();

    File umsatzdateienDownloadDir;

    UmsatzReaderIfc umsatzReader;

    public MoneyTransient(String kontonummer, File umsatzdateienDownloadDir) {
        this.umsatzdateienDownloadDir =  umsatzdateienDownloadDir;
        this.kontonnr                   = kontonummer;
    }

    List<File> eingelesesUmsatzDateien = new ArrayList<File>();

    public int getSaldo() {
        return saldo;
    }

    public UmsatzReaderIfc getUmsatzReader() {
        return umsatzReader;
    }

    public void setUmsatzReader(UmsatzReaderIfc umsatzReader) {
        this.umsatzReader = umsatzReader;
    }

    public SortedMap<String, Buchungszeile> getBuchungszeilen() {
        return buchungszeilen;
    }

    /**
     * Liefert die sortierten Buchungss�tze
     * @return
     */
    public List<Buchungszeile> getAktuelleDaten() {
        return sortierteBuchungszeilen;
    }

    public List<File> getEingelesesUmsatzDateien() {
        return eingelesesUmsatzDateien;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setIsSaved(boolean isSaved) {
        this.isSaved = isSaved;
    }

    public void setLastBackupDatabaseFile(File lastBackupDatabaseFile) {
        this.lastBackupDatabaseFile = lastBackupDatabaseFile;
    }

    public String getKontonnr() {
        return kontonnr;
    }

    public void setKontonnr(String kontonnr) {
        this.kontonnr = kontonnr;
    }


    /** Berechnet die Salden anhand der aktuellen Buchungszeilen
     */
    public void recalculate() {

        saldo   = 0;
        psaldo  = 0;
        message = "";

        int     lastHauptbuchungsnr = 0;
        boolean isFirst             = true;

        for (Buchungszeile buchungszeile : sortierteBuchungszeilen) {


            if (isFirst) {
                isFirst = false;

                if (!buchungszeile.hasSaldo) {
                    buchungszeile.saldo= buchungszeile.betrag;
                }

                saldo = buchungszeile.saldo;
            } else {
                saldo += buchungszeile.betrag;

                if (!buchungszeile.hasSaldo) {
                    buchungszeile.saldo = saldo;
                } else {
                    if (buchungszeile.saldo != saldo) {
                            System.out.println("Neu berechneter Saldo weicht vom gespeicherten Saldo ab: saldo berechnet: " + saldo + " <-> " + buchungszeile.saldo + "\n" + buchungszeile.toShow());
                    }
                }
            }

            if (buchungszeile.hauptbuchungsNr > 0) {
                lastHauptbuchungsnr = buchungszeile.hauptbuchungsNr;
            } else {
                lastHauptbuchungsnr++;
                buchungszeile.hauptbuchungsNr = lastHauptbuchungsnr;
            }

        }

        // Aufteilen der als Umlage markierten Beträge in Jahres-Zwölftel
        Map<String,Integer> jahresumlagevolumen = new HashMap<>(  );

        for (Buchungszeile buchungszeile : sortierteBuchungszeilen) {
            if (buchungszeile.hasJahresumlage()) {
                String jahr       = buchungszeile.datum.substring( 0,4 );
                Integer jahresVol = jahresumlagevolumen.get(jahr);
                if (jahresVol == null) {
                    jahresVol = new Integer( 0 );
                }
                jahresumlagevolumen.put(jahr, jahresVol + buchungszeile.getJahresumlageValue());
            }
        }

        String jahr         = "";
        String monat        = "";
        int    monatsUmlage = 0;
        int    jahresUmlage = 0;

        for ( Buchungszeile buchungszeile : sortierteBuchungszeilen ) {

            String aktJahr  = buchungszeile.datum.substring( 0, 4 );
            String aktMonat = buchungszeile.datum.substring( 0, 6 );

            if ( !aktJahr.equals( jahr ) ) {
                jahr = aktJahr;
                Integer b = jahresumlagevolumen.get( aktJahr );
                if ( b != null ) {
                    jahresUmlage = b;
                } else {
                    jahresUmlage = 0;
                }
                monatsUmlage = 0;
            }

            // ToDo: wenn ein Monat keine Werte hätte, würde für ihn nicht hochgezählt werden
            if ( !aktMonat.equals( monat ) ) {
                monat        = aktMonat;
                monatsUmlage = monatsUmlage + jahresUmlage / 12;
            }

            if (buchungszeile.hasJahresumlage()) {
                monatsUmlage = monatsUmlage - buchungszeile.getJahresumlageValue();
            }

            buchungszeile.saldoGeglaettet = buchungszeile.saldo + monatsUmlage;
        }
    }

    public void addUmbuchungszeilen(Buchungszeile pro, Buchungszeile contra) {

        int nextBuchungsnr = 0;
        int lastIndex      = 0;

        for (int i = 0; i < sortierteBuchungszeilen.size(); i++) {

            Buchungszeile bz = sortierteBuchungszeilen.get(i);

            if (bz.hauptbuchungsNr == 0) {
                throw new RuntimeException("Datenbank ist noch nicht migriiert.");
            }

            if (bz.hauptbuchungsNr != pro.hauptbuchungsNr) {
                continue;
            }

            if (bz.umbuchungNr >= nextBuchungsnr) {
                nextBuchungsnr = bz.umbuchungNr + 1;
            }

            lastIndex = i;
        }

        pro   .umbuchungNr = nextBuchungsnr;
        contra.umbuchungNr = nextBuchungsnr;

        sortierteBuchungszeilen.add(lastIndex + 1, pro);
        sortierteBuchungszeilen.add(lastIndex + 2, contra);
    }
}
