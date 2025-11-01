package de.amo.money;

import de.amo.view.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by private on 08.09.2015.
 */
public class BuchungszeilenEditor extends JDialog {

    MoneyController moneyController;

    private JPanel buttonPanel;
    private JButton pDatenSaveButton, cancelButton, /*kategorieSaveButton, */splittBuchungerzeugenButton;
    private BuchungszeilenEditorActionListener buchungszeilenEditorActionListener;

    Buchungszeile[]     buchungszeilen;
    ANumberInputField   lfdNr;
    ANumberInputField   pbetrag;
    AStringInputField   kommentar;
    AComboboxInputField kategorie;

    ANumberInputField   splittbetrag;
    AComboboxInputField splittKategorie;
    AStringInputField   splittKommentar;

    public BuchungszeilenEditor(MoneyController moneyController, Buchungszeile[] buchungszeilen, boolean nurKategorie) {

        this.moneyController = moneyController;
        this.buchungszeilen  = buchungszeilen;

        setModal(true);

        String buchungszeilenString = "";
        for (int i = 0; i < buchungszeilen.length; i++) {
            Buchungszeile buchungszeile = buchungszeilen[i];
            buchungszeilenString += buchungszeile.hauptbuchungsNr+"/"+buchungszeile.umbuchungNr+"; ";
        }


        if (nurKategorie) {
            setSize(1200, 250);     /*  ToDo: Festnageln auf die Größe???*/
            setTitle("Kategorien übergreifend festlegen : " + buchungszeilenString);
        } else {
            setSize(1200, 750);     /*  ToDo: Festnageln auf die Größe???*/
            setTitle("Buchungszeile bearbeiten : " + buchungszeilenString);
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.Y_AXIS));
        add(mainPanel);

        buchungszeilenEditorActionListener = new BuchungszeilenEditorActionListener(this);

        if (!nurKategorie) {
            mainPanel.add(createGrunddatenPanel(buchungszeilen[0]));
        }

        if (nurKategorie) {
            mainPanel.add(createKategoriePanel(buchungszeilen));
        }

        if (!nurKategorie) {

            mainPanel.add(createSplittPanel());

            if (!buchungszeilen[0].isUmbuchung()) {
                mainPanel.add(createPDatenPanel(buchungszeilen[0]));
            }
        }
    }

    private JPanel createGrunddatenPanel(Buchungszeile buchungszeile) {

        JPanel grunddatenPanel = new JPanel();
        grunddatenPanel.setBorder(new TitledBorder("Basisdaten (unveränderlich)"));
        grunddatenPanel.setLayout(new javax.swing.BoxLayout(grunddatenPanel, javax.swing.BoxLayout.Y_AXIS));

        JPanel nummernPanel = new JPanel();
        grunddatenPanel.add(nummernPanel);
        nummernPanel.setLayout(new javax.swing.BoxLayout(nummernPanel, javax.swing.BoxLayout.X_AXIS));

        AFieldPane aFieldPane;

        lfdNr = ANumberInputField.create(buchungszeile.hauptbuchungsNr, 0);
        lfdNr.setEditable(false);
        aFieldPane = new AFieldPane("Hauptbuchung", lfdNr, 25, 150, 40, 0);
        nummernPanel.add(aFieldPane);

        String suffix = "";
        if (buchungszeile.isUmbuchung()) {
            if (buchungszeile.umbuchungsPro) {
                suffix = " - pro";
            } else {
                suffix = " - contra";
            }
        }

        AStringInputField umbuchungsnr = AStringInputField.create(buchungszeile.umbuchungNr + suffix);
        umbuchungsnr.setEditable(false);
        aFieldPane = new AFieldPane("Umbuchung", umbuchungsnr, 25, 150, 70, 0);
        nummernPanel.add(aFieldPane);

        ADateInputField aDateInputField = ADateInputField.create(buchungszeile.datum);
        aDateInputField.setEditable(false);
        aFieldPane = new AFieldPane("Buchungsdatum", aDateInputField, 25, 150, ADateInputField.DATEINPUTLENGTH, 0);
        grunddatenPanel.add(aFieldPane);

        AStringInputField aStringInputField = AStringInputField.create(buchungszeile.quelleZiel);
        aStringInputField.setEditable(false);
        aFieldPane = new AFieldPane("Quelle / Ziel", aStringInputField, 25, 150, 990, 0);
        grunddatenPanel.add(aFieldPane);

        aStringInputField = AStringInputField.create(buchungszeile.buchungstext);
        aStringInputField.setEditable(false);
        aFieldPane = new AFieldPane("Buchungstext", aStringInputField, 25, 150, 990, 0);
        grunddatenPanel.add(aFieldPane);

        aStringInputField = AStringInputField.create(buchungszeile.verwendungszweck);
        aStringInputField.setEditable(false);
        aFieldPane = new AFieldPane("Verwendungszweck", aStringInputField, 25, 150, 990, 0);
        grunddatenPanel.add(aFieldPane);

        ANumberInputField aNumberInputField = ANumberInputField.create(buchungszeile.betrag, 2);
        aNumberInputField.setEditable(false);
        aFieldPane = new AFieldPane("Betrag", aNumberInputField, 25, 150, 100, 30);
        aFieldPane.getTrailingLabel().setText("EUR");
        grunddatenPanel.add(aFieldPane);

        return grunddatenPanel;
    }

    private JPanel createKategoriePanel(Buchungszeile[] buchungszeilen) {

        JPanel kategoriePanel = new JPanel();
        kategoriePanel.setBorder(new TitledBorder("Kategorie festlegen"));
        kategoriePanel.setLayout(new javax.swing.BoxLayout(kategoriePanel, javax.swing.BoxLayout.Y_AXIS));

        String sKat = null;
        AFieldPane aFieldPane;
        if (buchungszeilen.length == 1) {
            sKat = buchungszeilen[0].kategorie;
        }
        kategorie = AComboboxInputField.create(sKat, Kategoriefacade.get().getComboboxList());
        kategorie.setMaximumRowCount(40);
        aFieldPane = new AFieldPane("Kategorie", kategorie, 25, 150, 900, 0);
        kategoriePanel.add(aFieldPane);

        String kommentarVorbelegung = "";
        if (buchungszeilen.length == 1) {
            kommentarVorbelegung = buchungszeilen[0].kommentar;
        }

        kommentar = AStringInputField.create(kommentarVorbelegung);
        aFieldPane = new AFieldPane("Kommentar", kommentar, 25, 150, 990, 0);
        kategoriePanel.add(aFieldPane);

        JPanel p = new JPanel();
        JLabel label = new JLabel("Zum Verschieben der Buchung in die Auswertung des Vorjahres muss der Code '$Vorjahr$' im Kommentar enthalten sein.");
        label.setHorizontalAlignment(JLabel.RIGHT);
        label.setBackground(Color.orange);
        p.add(label);
        kategoriePanel.add(p);


        buttonPanel = createButtonPanel_Kategorie();
        buttonPanel.setBounds(200, 270, 300, 40);
        kategoriePanel.add(buttonPanel);

        return kategoriePanel;
    }

    private JPanel createSplittPanel() {
        JPanel splittbuchungsPanel = new JPanel();
        splittbuchungsPanel.setBorder(new TitledBorder("Splitt-Buchungen"));
        splittbuchungsPanel.setLayout(new javax.swing.BoxLayout(splittbuchungsPanel, javax.swing.BoxLayout.Y_AXIS));

        splittbetrag = ANumberInputField.create(0,2);
        AFieldPane aFieldPane = new AFieldPane("Splitt-Betrag", splittbetrag, 25, 150, 100, 500);
        aFieldPane.getTrailingLabel().setText("EUR    ( Sollte gleiches Vorzeichen wie die orignäre Buchung haben )");
        splittbuchungsPanel.add(aFieldPane);

        splittKategorie = AComboboxInputField.create(null, Kategoriefacade.get().getComboboxList());
        aFieldPane = new AFieldPane("Kategorie", splittKategorie, 25, 150, 900, 0);
        splittbuchungsPanel.add(aFieldPane);

        splittKommentar = AStringInputField.create(null);
        aFieldPane = new AFieldPane("Kommentar", splittKommentar, 25, 150, 700, 0);
        splittbuchungsPanel.add(aFieldPane);

        buttonPanel = createButtonPanel_Splittbuchung();
        buttonPanel.setBounds(200, 270, 300, 40);
        splittbuchungsPanel.add(buttonPanel);

        return splittbuchungsPanel;
    }

    private JPanel createPDatenPanel(Buchungszeile buchungszeile) {

        JPanel pdatenPanel = new JPanel();
        pdatenPanel.setBorder(new TitledBorder("P-Daten"));
        pdatenPanel.setLayout(new javax.swing.BoxLayout(pdatenPanel, javax.swing.BoxLayout.Y_AXIS));

        pbetrag = ANumberInputField.create(buchungszeile.pbetrag, 2);
        AFieldPane aFieldPane = new AFieldPane("P-Betrag", pbetrag, 25, 150, 100, 30);
        aFieldPane.getTrailingLabel().setText("EUR");
        pdatenPanel.add(aFieldPane);

        kommentar = AStringInputField.create(buchungszeile.kommentar);
        aFieldPane = new AFieldPane("Kommentar", kommentar, 25, 150, 990, 0);
        pdatenPanel.add(aFieldPane);

        buttonPanel = createButtonPanel_PDaten();
        buttonPanel.setBounds(200, 270, 300, 40);
        pdatenPanel.add(buttonPanel);

        return pdatenPanel;
    }


    private JPanel createButtonPanel_Kategorie() {

        buttonPanel = new JPanel();

        if (AmoStyle.isGuiTestMode()) {

            buttonPanel.setBackground(Color.red);
        }

        Dimension dimension = new Dimension(300, 40);
        buttonPanel.setSize(300, 40);
        buttonPanel.setPreferredSize(dimension);

        JLabel label = new JLabel();
        buttonPanel.add(label);

        AbstractAction kategorieSaveAction = new AbstractAction("Kategorie übernehmen", null) {

            @Override
            public void actionPerformed(ActionEvent e) {
                String selectetKategorie = kategorie.getText();
                String kategorie = Kategoriefacade.get().getKategorieFromComboboxString(selectetKategorie);
                for (int i = 0; i < buchungszeilen.length; i++) {
                    Buchungszeile buchungszeile = buchungszeilen[i];
                    buchungszeile.kategorie = kategorie;
                    buchungszeile.kommentar = kommentar.getText();
                }

                moneyController.getMoneyTr().setIsSaved(false);

                dispose();
            }
        };

        JButton kategorieSaveButton = new JButton();
        kategorieSaveButton.setDefaultCapable(true);
        kategorieSaveButton.setSelected(true);
        kategorieSaveButton.setAction(kategorieSaveAction);

        //Code, um mit Enter-Taste das Saven zu aktivieren":
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "Enter");
        getRootPane().getActionMap().put("Enter", kategorieSaveAction);

        buttonPanel.add(kategorieSaveButton);
        return buttonPanel;
    }

    private JPanel createButtonPanel_PDaten() {
        buttonPanel = new JPanel();
        if (AmoStyle.isGuiTestMode()) {

            buttonPanel.setBackground(Color.red);
        }

        Dimension dimension = new Dimension(300, 40);
        buttonPanel.setSize(300, 40);
        buttonPanel.setPreferredSize(dimension);

        JLabel label = new JLabel();
        buttonPanel.add(label);

        pDatenSaveButton = new JButton("P-Daten übernehmen");
        pDatenSaveButton.addActionListener(buchungszeilenEditorActionListener);

        buttonPanel.add(pDatenSaveButton);
        return buttonPanel;
    }

    private JPanel createButtonPanel_Splittbuchung() {
        buttonPanel = new JPanel();
        if (AmoStyle.isGuiTestMode()) {

            buttonPanel.setBackground(Color.red);
        }

        Dimension dimension = new Dimension(300, 40);
        buttonPanel.setSize(300, 40);
        buttonPanel.setPreferredSize(dimension);

        JLabel label = new JLabel();
        buttonPanel.add(label);

        splittBuchungerzeugenButton = new JButton("Splitt-Buchungen erzeugen");
        splittBuchungerzeugenButton.addActionListener(buchungszeilenEditorActionListener);

        buttonPanel.add(splittBuchungerzeugenButton);
        return buttonPanel;
    }

    class BuchungszeilenEditorActionListener implements java.awt.event.ActionListener {

        BuchungszeilenEditor editor;

        public BuchungszeilenEditorActionListener(BuchungszeilenEditor editor) {
            this.editor = editor;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String titel = "Fehler ";
            try {

                if (actionEvent.getSource() == cancelButton) {
                    titel += "beim Abbrechen";
                    dispose();
                }

                if (actionEvent.getSource() == pDatenSaveButton) {
                    titel += "beim Eintragen der Werte";

                    if (buchungszeilen[0].hauptbuchungsNr != editor.lfdNr.getIntValue()) {
                        throw new RuntimeException("Werte für Zeile " + editor.lfdNr.getIntValue() + " sollen in Zeile " + buchungszeilen[0].hauptbuchungsNr + " geschrieben werden");
                    }

                    buchungszeilen[0].kommentar = editor.kommentar.getText();
                    buchungszeilen[0].pbetrag = editor.pbetrag.getIntValue();

                    moneyController.getMoneyTr().recalculate();
                    moneyController.getMoneyTr().setIsSaved(false);

                    dispose();
                }

                if (actionEvent.getSource() == splittBuchungerzeugenButton) {
                    String selectetKategorie = splittKategorie.getText();
                    String kategorie= Kategoriefacade.get().getKategorieFromComboboxString(selectetKategorie);
                    moneyController.createSplittbuchungen(buchungszeilen[0], splittbetrag.getIntValue(), kategorie, splittKommentar.getText());

                    moneyController.getMoneyTr().setIsSaved(false);

                    dispose();
                }
            } catch (Exception e) {
                try {
                    dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                new ErrorMessageDialog(titel, e.getMessage(), e);
            }
        }
    }
}
