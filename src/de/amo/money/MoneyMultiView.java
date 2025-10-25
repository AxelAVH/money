package de.amo.money;

import de.amo.view.AFieldPane;
import de.amo.view.AStringInputField;
import de.amo.view.AmoStyle;
import de.amo.view.ErrorMessageDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by private on 09.02.2017.
 */
public class MoneyMultiView extends JFrame {

    private SortedMap<String,MoneyController> konten = new TreeMap<>();

    private File        umsatzdateienDownloadDir;    // Das Verzeichnis, in dem die heruntergeladenen Dateien vorliegen
    private String      moneyDir;                    // das Konten-Root-Verzeichnis des Money-Programms

    private JPanel      mainPanel;
    private JPanel      umsatzDateienPanel;
    private JPanel      tabAllgemeinPanel;

    private AStringInputField umsatzDateienUploadDir;
    private AFieldPane  umsatzDateienUploadPane;

    // hier sollen sich die lfd. Ausgaben hindurch scrollen
    private JTextArea   messageTextArea;
    private JTabbedPane tabbedPane;
    private JButton     bankDatenEinlesenButton;
    private MyActionListener actionListener;


    public MoneyMultiView(File downloaddir, String moneyDir) {

        this.moneyDir                 = moneyDir;
        this.umsatzdateienDownloadDir = downloaddir;

        setTitle("Bankkontenverwaltung");

        actionListener = new MyActionListener();
        addWindowListener(new MyWindowListener());

        mainPanel = new JPanel();
        mainPanel.setBackground(Color.cyan);
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.Y_AXIS));

        Dimension dim = new Dimension(1200,800);
        mainPanel.setPreferredSize(dim);

        setContentPane(mainPanel);

        tabbedPane = new JTabbedPane();
        umsatzDateienPanel = createButtonPanel();

        tabAllgemeinPanel = new JPanel();
        tabAllgemeinPanel.setLayout(new javax.swing.BoxLayout(tabAllgemeinPanel, javax.swing.BoxLayout.Y_AXIS));

        tabAllgemeinPanel.add(createMessagePanel());
        tabAllgemeinPanel.add(umsatzDateienPanel);

        tabbedPane.add("Allgemein", tabAllgemeinPanel);

        tabbedPane.add("Kategorien", new KategorieEditor().createEditorPanel(messageTextArea));


        mainPanel.add(tabbedPane);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        pack();
        setVisible(true);

        addMessage("Start erfolgreich.");
        addMessage("Verzeichnis der Konten: " + moneyDir);

        // initiale Befüllungen:
        ermittleDownloadFiles(umsatzdateienDownloadDir);
        loadDatabases();
        umsatzDateienUploadDir .setValue(umsatzdateienDownloadDir.getAbsolutePath());
        umsatzDateienUploadPane.getTrailingLabel().setText(""+ ermittleDownloadFiles(umsatzdateienDownloadDir).size());
    }

    private JPanel createMessagePanel() {

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new GridLayout(1,1));
        if (AmoStyle.isGuiTestMode()) {
            messagePanel.setBackground(Color.yellow);
        }

        messageTextArea = new JTextArea();
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setAutoscrolls(true);
        messageTextArea.setTabSize(20);

        JScrollPane scrollPane = new JScrollPane(messageTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        messagePanel.add(scrollPane);

        return messagePanel;
    }

    public void addMessage(String msg) {
        messageTextArea.append("\n"+msg);
        messageTextArea.repaint();
        messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
    }

    private JPanel createButtonPanel() {

        umsatzDateienPanel = new JPanel();

        if (AmoStyle.isGuiTestMode()) {
            umsatzDateienPanel.setBackground(Color.red);
        }

        umsatzDateienUploadDir = AStringInputField.create("");
        umsatzDateienUploadDir.setEditable(false);
        umsatzDateienUploadPane = new AFieldPane("Bankdaten Download", umsatzDateienUploadDir,20,150,400,20);
        umsatzDateienPanel.add(umsatzDateienUploadPane);
        umsatzDateienPanel.setBorder(new TitledBorder("Datei-Operationen"));
        Dimension dimension = new Dimension(2000,60);
        umsatzDateienPanel.setPreferredSize(dimension);
        umsatzDateienPanel.setMaximumSize(dimension);
        umsatzDateienPanel.setMinimumSize(dimension);

        umsatzDateienPanel.setLayout(new javax.swing.BoxLayout(umsatzDateienPanel, javax.swing.BoxLayout.X_AXIS));

        JPanel b1 = new JPanel();
        b1.add(createBankDatenEinlesenButton());

        umsatzDateienPanel.add(b1);
        umsatzDateienPanel.setVisible(true);

        return umsatzDateienPanel;
    }

    private JButton createBankDatenEinlesenButton() {
        bankDatenEinlesenButton = new JButton("ING-DiBa-Dateien lesen");
        bankDatenEinlesenButton.setVisible(true);
        bankDatenEinlesenButton.addActionListener(actionListener);
        return bankDatenEinlesenButton;
    }

    class MyWindowListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {

            boolean isSaved = true;
            String ungesicherteKonten = null;
            for (String kontonr : konten.keySet()) {
                MoneyController controller = konten.get(kontonr);
                if (!controller.isSaved()) {
                    isSaved = false;
                    if (ungesicherteKonten == null) {
                        ungesicherteKonten = controller.getMoneyTr().getKontonnr();
                    } else {
                        ungesicherteKonten += ", " + controller.getMoneyTr().getKontonnr();
                    }
                }
            }

            if (isSaved) {
                System.exit(0);
            }

            int result = JOptionPane.showConfirmDialog(mainPanel, ungesicherteKonten + " nix gespeichert, wirklich schließen?", "Sicherheitsabfrage", JOptionPane.YES_NO_OPTION, 0);

            if (result == 0) {
                System.exit(0);
            }
        }
    }

    class MyActionListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String titel = "Fehler ";
            try {
                if (actionEvent.getSource() == bankDatenEinlesenButton) {

                    titel += "beim Einlesen der Umsatzdateien";

                    Map<String,ArrayList<File>> filesProKontonr  = new HashMap<>();

                    List<File> files = ermittleDownloadFiles(umsatzdateienDownloadDir);

                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        if (file.isDirectory()) {
                            continue;
                        }
                        if (!file.getName().toLowerCase().endsWith(".csv")) {
                            continue;
                        }
                        String          kontoNrTmp = null;
                        UmsatzReaderIfc reader     = null;
                        if (UmsatzReader_INGDIBA.isMyFiletype(file)) {
                            reader     = new UmsatzReader_INGDIBA();
                            kontoNrTmp = reader.ermittleKontonummer(file);
                        } else if (UmsatzReader_1822direkt.isMyFiletype(file)) {
                            reader     = new UmsatzReader_1822direkt();
                            kontoNrTmp = reader.ermittleKontonummer(file);
                        }

                        if (kontoNrTmp == null) {
                            addMessage("Datei gefunden: " + file.getName() + " Keine Kontonummer ermittelbar");
                            continue;
                        }

                        addMessage("Datei gefunden: " + file.getName() + " Kontonumer : " + kontoNrTmp);

                        ArrayList<File> filesDesKontos = filesProKontonr.get(kontoNrTmp);
                        if (filesDesKontos == null) {
                            filesDesKontos = new ArrayList();
                            filesProKontonr.put(kontoNrTmp,filesDesKontos);
                        }
                        filesDesKontos.add(file);

                        MoneyController moneyController = initKonto(kontoNrTmp);
                        moneyController.getMoneyTr().setUmsatzReader(reader);
                    }

                    for (Map.Entry<String, ArrayList<File>> kontoEntry : filesProKontonr.entrySet()) {
                        addMessage(kontoEntry.getKey() + " : Lese Datei(en) für Konto");
                        MoneyController moneyController = konten.get(kontoEntry.getKey());
                        ArrayList<File> value = kontoEntry.getValue();
                        moneyController.getMoneyDatabase().umsatzDateienEinlesen(moneyController.getMoneyTr(), value.toArray(new File[value.size()]));
                        moneyController.getMoneyView().updateGui();
                    }
                }
            } catch (Exception e) {
                new ErrorMessageDialog(titel, e.getMessage(), e);
            }
        }
    }

    private List<File> ermittleDownloadFiles(File umsatzdateienDownloadDir) {
        List<File> ret = new ArrayList<>();
        File[] files = umsatzdateienDownloadDir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                continue;
            }
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                continue;
            }

            ret.add(file);
        }
        return ret;
    }


    private void loadDatabases() {

        File mDir = new File(moneyDir);
        if (!mDir.exists()) {
            return;
        }
        if (!mDir.isDirectory()) {
            throw new RuntimeException("Der Pfad " + moneyDir + " muss ein Verzeichnis anzeigen (tut er aber nicht)");
        }

        File[] files = mDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory()) {
                continue;   // sollte es möglichst gar nicht geben, hier sollen nur die Datenbankverzeichnisse liegen
            }

            String kontoNrTmp = file.getName();
            addMessage(kontoNrTmp + " : Datenbankverzeichnis gefunden");
            MoneyController moneyController = initKonto(kontoNrTmp);
            moneyController.getMoneyDatabase().loadDatabase(moneyController.getMoneyTr());
            addMessage(kontoNrTmp + " : Datenbank geladen");
            moneyController.getMoneyView().updateGui();
        }
    }

    private MoneyController initKonto(String kontoNrTmp) {

        MoneyController moneyController = konten.get(kontoNrTmp);

        if (moneyController != null) {
            return moneyController;
        }

        MoneyTransient moneyTransient   = new MoneyTransient (kontoNrTmp, umsatzdateienDownloadDir);
        MoneyDatabase   moneyDatabase   = new MoneyDatabase  (new File(moneyDir,kontoNrTmp), messageTextArea);
        moneyController                 = new MoneyController(moneyTransient, moneyDatabase);
        moneyController.moneyView       = new MoneyView      (moneyController, messageTextArea);

        konten.put(kontoNrTmp, moneyController);
        tabbedPane.add(kontoNrTmp, moneyController.getMoneyView().getRootPane());

        return moneyController;
    }
}
