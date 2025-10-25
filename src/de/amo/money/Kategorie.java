package de.amo.money;

import de.amo.view.fachwerte.Fachwert;
import de.amo.view.fachwerte.FachwertString;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by private on 19.01.2016.
 */
public class Kategorie {

    public static String CODE           = "code";
    public static String BESCHREIBUNG   = "beschreibung";


    private String code;
    private String beschreibung;

    public Kategorie() {
    }

    public Kategorie(String code, String beschreibung) {
        this.code = code;
        this.beschreibung = beschreibung;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }


    static FachwertString getFachwert_Code() {
        FachwertString fw = new FachwertString(CODE);

        fw.setColumName("Code");

        fw.setPreferredWidth(80);
        fw.setMinWidth(50);
        fw.setMaxWidth(120);

        DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public int getHorizontalAlignment() {
                return SwingConstants.CENTER;
            }
        };
        fw.setTableCellRenderer(tableCellRenderer);

        return fw;
    }

    static FachwertString getFachwert_Beschreibung() {

        FachwertString fw = new FachwertString(BESCHREIBUNG);

        fw.setColumName("Beschreibung");
        fw.setPreferredWidth(400);
        fw.setMinWidth(200);
        fw.setMaxWidth(1800);

        return fw;
    }

    public static List<Fachwert> getAlleFachwerte() {
        List<Fachwert> ret = new ArrayList();
        ret.add(getFachwert_Code());
        ret.add(getFachwert_Beschreibung());
        return ret;
    }
}
