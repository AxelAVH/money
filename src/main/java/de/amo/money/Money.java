package de.amo.money;

import de.amo.tools.Environment;
import de.amo.tools.FileHandler;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by amo on 22.08.2015.
 */
public class Money {


    public static void main(String args[]) {
        if (args.length == 0) {
            File testDir = new File("C:\\Users\\private\\IdeaProjects\\Money\\test\\run");

            File testRessourceDir = new File("C:\\Users\\private\\IdeaProjects\\Money\\test\\");

            String kontoDir = testDir.getAbsolutePath();

            // das Run-Verzeichnis leeren
            File[] files = testDir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    file.deleteOnExit();
                }
            }


            FileHandler.copyDir(new File(testRessourceDir, "initial03"), testDir);

            File database = new File("C:\\Users\\axelm\\softwareprojekte\\idea\\Money2025\\manuelleTests\\database.csv");
            FileHandler.copyTo(database, new File(testDir, "database.csv"));

            Money.main2(new String[]{"moneyDir=" + kontoDir});

        } else {

            main2(args);

        }

    }

    public static void main2(String args[]) {

        String kontodir = null;

        File umsatzdateienDownloadDir = Environment.getOS_DownloadDir();

        for (int i = 0; i < args.length; i++) {
            int pos = args[i].indexOf("=");
            if (pos < 1) {
                continue;
            }
            String key   = args[i].substring(0, pos);
            String value = args[i].substring(pos + 1);
            if (key.toLowerCase().equals("moneydir") && args[i].length() > (pos + 1)) {
                kontodir = value;
                File f = new File(kontodir);
                if (!f.exists()) {
                    System.out.println("<" + kontodir + "> ist kein gueltiges Verzeichnis.");
                    System.exit(0);
                }
                if (!f.isDirectory()) {
                    System.out.println("<" + kontodir + "> ist kein Verzeichnis.");
                    System.exit(0);
                }
            }
            if (key.toLowerCase().equals("downloaddir")) {
                umsatzdateienDownloadDir = new File(value);
            }
        }

        if (kontodir == null) {
            System.out.println("Parameter 'moneyDir' ist anzugeben!");
            System.exit(0);
        }

        try {
            Kategoriefacade.init(kontodir);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

//        MoneyTransient  moneyTr             = new MoneyTransient (umsatzdateienDownloadDir);
//        MoneyDatabase   moneyDatabase       = new MoneyDatabase  (moneyDir);
//        MoneyController moneyController     = new MoneyController(moneyTr, moneyDatabase);


//        MoneyView       moneyView           = new MoneyView      (moneyController);
//
//        moneyController.moneyView = moneyView;

        MoneyMultiView  moneyMultiView      = new MoneyMultiView(umsatzdateienDownloadDir, kontodir);


//        moneyDatabase.loadDatabase(moneyTr);
//
//
//        moneyTr.recalculate();
//        moneyView.updateGui();

    }


}
