package de.amo.money;

import de.amo.view.cellrenderer.AIntegerCellRenderer;
import de.amo.view.fachwerte.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by private on 18.01.2016.
 */
public class Fachwerte {

    public static String KATEGORIE          = "Kategorie";
    public static String KOMMENTAR          = "Kommentar";
    public static String VERWENDUNGSZWECK   = "Verwendungszweck";
    public static String BUCHUNGSTEXT       = "Buchungstext";
    public static String QUELLEZIEL         = "QuelleZiel";
    public static String BETRAG             = "Betrag";
    public static String WAEHRUNG           = "Waehrung";
    public static String DATUM              = "Datum";
    public static String PBETRAG            = "P-Betrag";
    public static String SALDO              = "Saldo";
    public static String SALDOGEGLAETTET    = "Saldo-Geglättet";
    public static String PSALDO             = "p-Saldo";
    public static String HAUPTBUCHUNGSNR    = "Hauptbuchungsnr";
    public static String UMBUCHUNGSNR       = "Umbuchungsnr";

    public static List<Fachwert> getAlleFachwerte() {
        List<Fachwert> ret = new ArrayList();
        ret.add(getFachwert_HauptbuchungsNr());
        ret.add(getFachwert_UmbuchungNr());
        ret.add(getFachwert_Datum());
        ret.add(getFachwert_QuelleZiel());
        ret.add(getFachwert_Verwendungszweck());
        ret.add(getFachwert_Buchungstext());
        ret.add(getFachwert_Kategorie());
        ret.add(getFachwert_Kommentar());
        ret.add(getFachwert_Betrag());
        ret.add(getFachwert_Waehrung());
        ret.add(getFachwert_Saldo());
        ret.add(getFachwert_SaldoGeglaettet());
//        ret.add(getFachwert_PSaldo());

        return ret;
    }

    public static Fachwert getFachwert_HauptbuchungsNr() {

        Fachwert fw = new FachwertInteger(HAUPTBUCHUNGSNR);

        fw.setColumName("HauptbuchungsNr.");
        fw.setPreferredWidth(50);
        fw.setMinWidth(30);
        fw.setMaxWidth(100);
        fw.setLabel30("HauptbuchungsNr.");

        AIntegerCellRenderer tableCellRenderer = new AIntegerCellRenderer() {
            @Override
            public int getHorizontalAlignment() {
                return SwingConstants.CENTER;
            }
        };
        fw.setTableCellRenderer(tableCellRenderer);

        return fw;
    }

    public static Fachwert getFachwert_UmbuchungNr() {

        Fachwert fw = new FachwertInteger(UMBUCHUNGSNR);

        fw.setColumName("UmbuchungsNr.");
        fw.setPreferredWidth(50);
        fw.setMinWidth(30);
        fw.setMaxWidth(100);
        fw.setLabel30("UmbuchungsNr.");

        AIntegerCellRenderer tableCellRenderer = new AIntegerCellRenderer() {
            @Override
            public int getHorizontalAlignment() {
                return SwingConstants.CENTER;
            }
        };
        fw.setTableCellRenderer(tableCellRenderer);

        return fw;
    }

    public static Fachwert getFachwert_Buchungstext() {

        Fachwert fw = new FachwertString(BUCHUNGSTEXT);

        fw.setColumName("Buchungstext");
        fw.setPreferredWidth(200);
        fw.setMinWidth(150);
        fw.setMaxWidth(250);
        fw.setLabel30("Buchungstext");

        return fw;
    }

    public static Fachwert getFachwert_Verwendungszweck() {

        Fachwert fw = new FachwertString(VERWENDUNGSZWECK);

        fw.setColumName("Verwendungszweck");
        fw.setPreferredWidth(400);
        fw.setMinWidth(150);
        fw.setMaxWidth(700);
        fw.setLabel30("Verwendungszweck");

        return fw;
    }

    public static Fachwert getFachwert_QuelleZiel() {

        Fachwert fw = new FachwertString(QUELLEZIEL);

        fw.setColumName("Quelle/Ziel");
        fw.setPreferredWidth(300);
        fw.setMinWidth(150);
        fw.setMaxWidth(500);
        fw.setLabel30("Quelle/Ziel");

        return fw;
    }

    public static Fachwert getFachwert_Kommentar() {

        Fachwert fw = new FachwertString(KOMMENTAR);

        fw.setColumName("Kommentar");
        fw.setPreferredWidth(200);
        fw.setMinWidth(150);
        fw.setMaxWidth(800);
        fw.setLabel30("Kommentar");

        return fw;
    }

    public static Fachwert getFachwert_Kategorie() {

        Fachwert fw = new FachwertString(KATEGORIE);

        fw.setColumName("Kategorie");
        fw.setPreferredWidth(50);
        fw.setMinWidth(30);
        fw.setMaxWidth(100);
        fw.setLabel30("Kategorie");

        DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public int getHorizontalAlignment() {
                return SwingConstants.CENTER;
            }
        };
        fw.setTableCellRenderer(tableCellRenderer);

        return fw;
    }

    public static Fachwert getFachwert_Waehrung() {

        Fachwert fw = new FachwertString(WAEHRUNG);

        fw.setColumName("Waehrung");
        fw.setPreferredWidth(50);
        fw.setMinWidth(30);
        fw.setMaxWidth(100);
        fw.setLabel30("Waehrung");

        DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public int getHorizontalAlignment() {
                return SwingConstants.CENTER;
            }
        };
        fw.setTableCellRenderer(tableCellRenderer);

        return fw;
    }

    public static Fachwert getFachwert_Datum() {

        Fachwert fw = new FachwertDatum(DATUM);

        fw.setColumName("Datum");
        fw.setPreferredWidth(50);
        fw.setMinWidth(30);
        fw.setMaxWidth(100);
        fw.setLabel30("Datum");

        return fw;
    }

    public static Fachwert getFachwert_Betrag() {

        Fachwert fw = new FachwertDouble(BETRAG);

        fw.setColumName("Betrag");
        fw.setPreferredWidth(70);
        fw.setMinWidth(50);
        fw.setMaxWidth(100);
        fw.setLabel30("Betrag");
        fw.setSummable(true);

        return fw;
    }

    public static Fachwert getFachwert_Saldo() {

        Fachwert fw = new FachwertDouble(SALDO);

        fw.setColumName("Saldo");
        fw.setPreferredWidth(70);
        fw.setMinWidth(50);
        fw.setMaxWidth(100);
        fw.setLabel30("Saldo");

        return fw;
    }

    public static Fachwert getFachwert_PBetrag() {

        Fachwert fw = new FachwertDouble(PBETRAG);

        fw.setColumName("PBetrag");
        fw.setPreferredWidth(70);
        fw.setMinWidth(50);
        fw.setMaxWidth(100);
        fw.setLabel30("PBetrag");

        return fw;
    }


    public static Fachwert getFachwert_PSaldo() {

        Fachwert fw = new FachwertDouble(PSALDO);

        fw.setColumName("PSaldo");
        fw.setPreferredWidth(70);
        fw.setMinWidth(50);
        fw.setMaxWidth(100);
        fw.setLabel30("PSaldo");

        return fw;
    }


    public static Fachwert getFachwert_SaldoGeglaettet() {

        Fachwert fw = new FachwertDouble(SALDOGEGLAETTET);

        fw.setColumName("Saldo geglättet");
        fw.setPreferredWidth(70);
        fw.setMinWidth(50);
        fw.setMaxWidth(100);
        fw.setLabel30("Saldo Geglättet");

        return fw;
    }



}
