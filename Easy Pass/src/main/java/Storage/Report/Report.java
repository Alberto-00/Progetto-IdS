package Storage.Report;

import ApplicationLogic.Utils.JSONSerializable;
import Storage.Dipartimento.Dipartimento;
import Storage.SessioneDiValidazione.SessioneDiValidazione;
import org.json.simple.JSONObject;

import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Report implements Comparable<Report>, JSONSerializable {

    private int id;
    private Date data;
    private Time orario;
    private String pathFile;
    private Dipartimento dip;
    private SessioneDiValidazione sessione;

    public Report(int id, Date data, Time orario, String pathFile, Dipartimento dip, SessioneDiValidazione sessione) {
        this.id = id;
        this.data = data;
        this.orario = orario;
        this.pathFile = pathFile;
        this.dip = dip;
        this.sessione = sessione;
    }

    public Report() {
        id=0;
        data=null;
        orario=null;
        pathFile="";
        dip=null;
        sessione=null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Time getOrario() {
        return orario;
    }

    public void setOrario(Time orario) {
        this.orario = orario;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public Dipartimento getDip() {
        return dip;
    }

    public void setDip(Dipartimento dip) {
        this.dip = dip;
    }

    public SessioneDiValidazione getSessione() {
        return sessione;
    }

    public void setSessione(SessioneDiValidazione sessione) {
        this.sessione = sessione;
    }

    @Override
    public int compareTo(Report o) {
        return this.id - o.getId();
    }

    @Override
    public JSONObject toJson(){
        JSONObject object = new JSONObject();
        object.put("id", this.id);
        object.put("data", this.data.toString());
        object.put("orario", this.orario.toString());
        return object;
    }

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", data=" + data +
                ", orario=" + orario +
                ", pathFile='" + pathFile + '\'' +
                ", dip=" + dip +
                ", sessione=" + sessione +
                '}';
    }
}