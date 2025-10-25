package de.amo.money;

import de.amo.tools.Datum;
import de.amo.view.AToolTipHeader;
import de.amo.view.AmoStyle;
import de.amo.view.cellrenderer.Integer2FloatCellRenderer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

/**
 * Created by private on 14.01.2016.
 */
public class Reportgenerator {

    private SortedSet<String> reportKategorienFein;
    private SortedSet<String> reportKategorienGrob;
    private SortedSet<String> reportMonate;
    private SortedSet<String> reportJahre;
    private SortedMap<String, Integer> jahresSumme;

    public Reportgenerator(List<Buchungszeile> buchungszeilen, JTabbedPane tabbedPane) {
        // die Spaltenköpfe (betroffene Kategorien) ermitteln:
        initReportKategorien(buchungszeilen);

        // die betroffenen Monate und Jahre ermitteln:
        initReportZeitraeume();

        // die Summen pro Kategorie und Jahr ermitteln:
        berechneJahressummen(buchungszeilen);

        // Darstellung:
        int tabCount = tabbedPane.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            String title = tabbedPane.getTitleAt(i);
            if ("Auswertung".equals(title)) {
                tabbedPane.remove(i);
                break;
            }
        }

        JPanel tablePanel = createTablePanel();
        tabbedPane.add("Auswertung", tablePanel);
    }

    private void berechneJahressummen(List<Buchungszeile> buchungszeilen) {

        String heuteMonat = Datum.heute().substring(0,6);

        jahresSumme = new TreeMap<String, Integer>();

        for (Buchungszeile buchungszeile : buchungszeilen) {

            String datum = buchungszeile.datum;

            if (buchungszeile.kommentar.contains("$Minus1Tag$")) {
                datum = Datum.forward(datum,-1);
            }
            if (buchungszeile.kommentar.contains("$Minus2Tag$")) {
                datum = Datum.forward(datum,-2);
            }
            if (buchungszeile.kommentar.contains("$Minus3Tag$")) {
                datum = Datum.forward(datum,-3);
            }
            if (buchungszeile.kommentar.contains("$Minus4Tag$")) {
                datum = Datum.forward(datum,-4);
            }



            String jahr = datum.substring (0, 4);
/*
            if (buchungszeile.kommentar.contains("$Vorjahr$")) {
                datum = datum.substring(0,4);
                int datumI = Integer.parseInt(datum);
                datumI = datumI - 1;
                datum = "" + datumI;
                jahr = datum;
            }
            */
            // der heutige (angebrochene) Monat wird nicht betrachtet
            // ( Wenn Datum vorgezogen wurde, kann es nicht mehr im aktuellen Monat liegen)
            if (datum.startsWith(heuteMonat)) {
                continue;
            }

            String kat = buchungszeile.kategorie;
            if (kat == null) {
                kat = "";
            }
            kat = kat + "--";

            String jahrUndKatFein = jahr + kat.substring(0, 2);
            String jahrUndKatGrob = jahr + kat.substring(0, 1);

            Integer int1 = jahresSumme.get(jahrUndKatFein);
            Integer int2 = jahresSumme.get(jahrUndKatGrob);

            if (int1 == null) {
                int1 = new Integer(0);
            }
            if (int2 == null) {
                int2 = new Integer(0);
            }

            int1 += buchungszeile.betrag;
            int2 += buchungszeile.betrag;
            jahresSumme.put(jahrUndKatFein, int1);
            jahresSumme.put(jahrUndKatGrob, int2);
        }
    }


    private void initReportZeitraeume() {
        reportMonate = new TreeSet();
        reportJahre  = new TreeSet();
        String heute = Datum.heute();
        String endjahr = heute.substring(0, 4);
        int endYear = Integer.parseInt(endjahr);

        for (int i = 2013; i <= endYear; i++) {
            for (int monat = 1; monat <= 12; monat++) {
                String reportMonat = "0" + monat;
                reportMonat = "" + i + reportMonat.substring(reportMonat.length() - 2);
                reportMonate.add(reportMonat);
            }
            reportJahre.add("" + i);
        }
    }

    private void initReportKategorien(List<Buchungszeile> buchungszeilen) {
        reportKategorienFein = new TreeSet();
        reportKategorienGrob = new TreeSet();
        for (Buchungszeile buchungszeile : buchungszeilen) {
            String kat = buchungszeile.kategorie;
            if (kat == null) {
                kat = "";
            }
            kat = kat + "--";
            reportKategorienFein.add(kat.substring(0, 2));  // den kompletten Kategoriecode als Gruppierungsmerkmal
            reportKategorienGrob.add(kat.substring(0, 1));  // Nur das erste Zeichen der Kategorie als Gruppierungsmerkmal
        }
    }



    public JPanel createTablePanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel,BoxLayout.Y_AXIS));

        String heuteMonat = Datum.heute().substring(0,6);
        String monatserster = "01." + heuteMonat.substring(4,6)+"." + heuteMonat.substring(0,4);


        createTabelle(tablePanel, reportKategorienGrob, "Monatliche Umsätze pro Hauptkategorie (alles vor dem " + monatserster + ")");

        createTabelle(tablePanel, reportKategorienFein, "Monatliche Umsätze pro Kategorie (alles vor dem " + monatserster + ")");

        return tablePanel;
    }

    private void createTabelle(JPanel tablePanel, SortedSet<String> reportKategorien, String title) {
        ReportsaetzeTableModel model = new ReportsaetzeTableModel();
        JScrollPane comp = new JScrollPane(createTable(model, reportKategorien));
        tablePanel.add(comp);
        comp.setBorder(new TitledBorder(title));

        BigDecimal monateImJahr = new BigDecimal(12);
        String heute = Datum.heute();
        String monatHeute = heute.substring(4,6);
        int monatHeuteI = Integer.parseInt(monatHeute);

        for (String  jahr : reportJahre) {

            if (heute.startsWith(jahr)) {
                // der heutige (angebrochene) Monat wird nicht betrachtet
                monateImJahr = new BigDecimal(monatHeuteI - 1);
            }

            Object[] rowData = new Object[reportKategorien.size()+1];

            int coloumn = 0;

            rowData[coloumn] = jahr;    // in Spalte "0" steht das Jahr

            for (String kat : reportKategorien) {
                Integer value = jahresSumme.get(jahr + kat);
                coloumn++;
                if (value != null) {
                    if (monateImJahr.intValue() > 0) {
                        BigDecimal bd = new BigDecimal(value);
                        bd = bd.divide(monateImJahr, BigDecimal.ROUND_HALF_UP);
                        value = bd.intValue();
                    } else {
                        value = 0;
                    }
                }
                rowData[coloumn] = value;
            }

            model.addRow(rowData);
        }
    }

    private JTable createTable(DefaultTableModel model, SortedSet<String> reportKategorien) {

        model.addColumn("Jahr");

        for (String kat : reportKategorien) {
            model.addColumn(kat);
        }

        final Integer2FloatCellRenderer integerCellRenderer = new Integer2FloatCellRenderer();

        final JTable table = new JTable(model) {

            public boolean isCellEditable(int x, int y) {
                return false;
            }

            public TableCellRenderer getCellRenderer(int row, int column) {

                if (getModel().getColumnClass(column).equals(Integer.class)) {
                    return integerCellRenderer;
                }

                if (column == 0) {
                    return new DefaultTableCellRenderer() {
                        @Override
                        public int getHorizontalAlignment() {
                            return SwingConstants.CENTER;
                        }
                    };
                }
                // else...
                return super.getCellRenderer(row, column);
            }
        };

        // und jetzt noch Tooltips für die Spaltenköpfe:
        List <Kategorie> kategories = Kategoriefacade.get().getKategorien();
        Map<String, String> tooltipmap = new HashMap<>();
        for (Kategorie kategory : kategories) {
            tooltipmap.put(kategory.getCode(), kategory.getBeschreibung());
        }
        tooltipmap.put("-","Keine Kategoriezuordnung");
        tooltipmap.put("--","Keine Kategoriezuordnung");
        AToolTipHeader header = new AToolTipHeader(table.getColumnModel(), tooltipmap);
        //header.setToolTipText("Default ToolTip TEXT");
        table.setTableHeader(header);

        if (AmoStyle.isGuiTestMode()) {
            // der wirkt wirklich
            table.setBackground(Color.cyan);
        }
        table.setVisible(true);

        return table;

    }

    class ReportsaetzeTableModel extends DefaultTableModel {
        public Class getColumnClass(int c) {
            Object valueAt = getValueAt(0, c);
//            if (valueAt == null) {
//                return String.class;
//            }
            if (c == 0) {
                return String.class;
            } else {
                return Integer.class;
            }
//
//            Class aClass = valueAt.getClass();
//            return aClass;
        }
    }

}
