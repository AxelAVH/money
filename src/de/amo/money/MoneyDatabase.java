package de.amo.money;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * Created by private on 11.10.2015.
 */
public class MoneyDatabase {

    private File      kontodir;
    private JTextArea messageTextArea;

    public MoneyDatabase(File kontodir, JTextArea   messageTextArea) {
        this.kontodir        = kontodir;
        this.messageTextArea = messageTextArea;
    }

    public String getKontodir() {
        return kontodir.getAbsolutePath();
    }

    private File getBackupDir() {
        File bdir = new File(kontodir,"backup");
        if (!bdir.exists()) {
            bdir.mkdir();
        }
        return bdir;
    }

    public File getArchivDir() {
        File adir = new File(kontodir,"archiv");
        if (!adir.exists()) {
            adir.mkdir();
        }
        return adir;
    }

    public File getDatabaseFile() {
        return new File(kontodir, "database.csv");
    }


    public void umsatzDateienEinlesen(MoneyTransient moneyTr, File[] files) {

        int datensaetzeVorher = moneyTr.getBuchungszeilen().size();

        List<File> verarbeiteteFiles = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {

            File file = files[i];

            if (datensaetzeVorher == 0 && verarbeiteteFiles.size() > 0) {
                throw new RuntimeException("Beim Einlesen der ersten Buchungssätze einer Datenbank darf nur eine Datei vorgelegt werden.");
            }

            addMessage(moneyTr.getKontonnr() + " : Lese Datei " + file.getName());
            try {
                // die letzte in der Datei übermittelte Zeile ist in der Buchungsreihenfolge die Erste
                Buchungszeile buchungszeileFirst = moneyTr.getUmsatzReader().readUmsatzFile(file, moneyTr);

                if (datensaetzeVorher == 0) {
                    buchungszeileFirst.isAllerersterSatz = true;
                }
                verarbeiteteFiles.add(file);

            } catch (Exception e) {
                addMessage("Abbruch: " + e);
                e.printStackTrace();
            }
        }

        //moneyTr.sortierteBuchungszeilen = moneyTr.getUmsatzReader().sortiere(moneyTr.buchungszeilen);
        SortedMap<String, Buchungszeile> sortedMap = new TreeMap<String, Buchungszeile>();
        for (Buchungszeile buchungszeile : moneyTr.buchungszeilen.values()) {
            sortedMap.put(buchungszeile.getSortKey(), buchungszeile);
        }

        moneyTr.sortierteBuchungszeilen = new ArrayList<Buchungszeile>();
        moneyTr.sortierteBuchungszeilen.addAll(sortedMap.values());

        moneyTr.recalculate();

        // Erst jetzt addieren, es können Exceptions geflogen sein wegen Lücken in den Dateien:
        moneyTr.getEingelesesUmsatzDateien().addAll(verarbeiteteFiles);

        int datensaetzeHinterher = moneyTr.getBuchungszeilen().size();

        if (datensaetzeVorher != datensaetzeHinterher) {
            moneyTr.setIsSaved(false);
        }

        addMessage(moneyTr.getKontonnr() + " : Bank-Dateien eingelesen.");
    }


    public void loadDatabase(MoneyTransient moneyTr) {

        File f = getDatabaseFile();
        moneyTr.getBuchungszeilen().clear();

        if (f.exists()) {
            try {
                Buchungszeile.readDatabaseFile(f.getAbsolutePath(), moneyTr.getBuchungszeilen(), moneyTr.sortierteBuchungszeilen);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            moneyTr.getEingelesesUmsatzDateien().clear();
            moneyTr.recalculate();
            moneyTr.setIsSaved(true);
            moneyTr.setLastBackupDatabaseFile(null);
            addMessage(moneyTr.getKontonnr() + " : Datenbank eingelesen ");
        } else {
            moneyTr.getEingelesesUmsatzDateien().clear();
            moneyTr.setIsSaved(true);
            moneyTr.setLastBackupDatabaseFile(null);
            addMessage(moneyTr.getKontonnr() + " : Keine Datenbank eingelesen ");
        }
    }

    public String saveDatabase(MoneyTransient moneyTr) {

        File f = getDatabaseFile();

        if (f.exists()) {
            File backupDir = getBackupDir();
            File backupDatabase = new File(backupDir, f.getName() + "_" + new Date().getTime());
            boolean b = f.renameTo(backupDatabase);
            if (b) {
                moneyTr.setLastBackupDatabaseFile(backupDatabase);
            }
        } else {
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
        }
        try {
            Buchungszeile.writeDatabaseFile(f, moneyTr.getAktuelleDaten());

            List<File> eingelesesUmsatzDateien = moneyTr.getEingelesesUmsatzDateien();
            for (File file : eingelesesUmsatzDateien) {
                File archivFile = new File(getArchivDir(),file.getName());
                if (archivFile.exists()) {
                    // ToDo: Gleichheit noch besser prüfen:
                    if (file.length() == archivFile.length()) {
                        archivFile.delete();    // Inkonsequent, wenn beide wirklich gleich wären ....
                    }
                }
                file.renameTo(archivFile);
            }
            eingelesesUmsatzDateien.clear();
        } catch (Exception e) {
            RuntimeException rte = new RuntimeException("Abbruch beim Sichern", e);
            throw rte;
        }
        moneyTr.setIsSaved(true);
        return "Gespeichert und Backup der Vorgängerversion erzeugt.";
    }

    public void addMessage(String msg) {
        messageTextArea.append("\n"+msg);
        messageTextArea.repaint();
        messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
    }
}
