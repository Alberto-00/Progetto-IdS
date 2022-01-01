package ApplicationLogic.Ajax;

import ApplicationLogic.Utils.InvalidRequestException;
import ApplicationLogic.Utils.ServletLogic;
import Storage.Dipartimento.Dipartimento;
import Storage.Dipartimento.DipartimentoDAO;
import Storage.PersonaleUnisa.Direttore.DirettoreDiDipartimento;
import Storage.PersonaleUnisa.Docente.Docente;
import Storage.PersonaleUnisa.Docente.DocenteDAO;
import Storage.Report.Report;
import Storage.Report.ReportDAO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

@WebServlet(name = "AjaxServlet", value = "/report/*")
public class AjaxServlet extends ServletLogic {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = getPath(request);
        DirettoreDiDipartimento direttore = (DirettoreDiDipartimento) request.getSession().getAttribute("direttoreSession");

        try {
            switch (path) {
                case "/search": {
                    DocenteDAO docenteDAO = new DocenteDAO();
                    List<Docente> docenti = docenteDAO.doRetrieveAllWithRelations();
                    JSONObject root = new JSONObject();
                    JSONArray arr = new JSONArray();
                    root.put("listName", arr);

                    for (int i = 1; i < docenti.size(); i++) {
                        if(docenti.get(i).getNome() != null){
                            JSONObject obj = new JSONObject();
                            obj.put("name", docenti.get(i).getNome() + " " + docenti.get(i).getCognome());
                            arr.add(obj);
                        }
                    }
                    sendJson(response, root);
                    break;
                }

                case "/search_report": {
                    String firstDate = request.getParameter("firstDate");
                    String secondDate = request.getParameter("secondDate");
                    String nameDoc = request.getParameter("nameDoc");

                    DipartimentoDAO dipartimentoDAO = new DipartimentoDAO();
                    Dipartimento tmpDip = dipartimentoDAO.doRetrieveById(direttore.getDipartimento().getCodice());

                    JSONObject root = new JSONObject();
                    JSONArray arrRep  = new JSONArray();
                    JSONArray arrDoc = new JSONArray();

                    if (firstDate.compareTo("") != 0 && secondDate.compareTo("") != 0){
                        Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(firstDate);
                        Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(secondDate);
                        boolean checkDate = date1.compareTo(date2) < 0 || date1.compareTo(date2) == 0;

                        if (checkDate){
                            search(date1, date2, nameDoc, tmpDip, root, arrRep, arrDoc, response);
                            break;
                        } else {
                            root.put("dateError", "La prima data deve essere minore della seconda data.");
                            sendJson(response, root);
                            break;
                        }
                    } else if (firstDate.compareTo("") != 0 && secondDate.compareTo("") == 0){
                        Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(firstDate);
                        search(date1, date1, nameDoc, tmpDip, root, arrRep, arrDoc, response);
                        break;
                    } else if (firstDate.compareTo("") == 0 && secondDate.compareTo("") != 0){
                        root.put("dateError", "Inserire la prima data.");
                        sendJson(response, root);
                        break;
                    }
                    if (nameDoc != null && nameDoc.compareTo("") != 0){
                        Docente docente = new Docente();
                        docente.setCognome(cognome(nameDoc));
                        docente.setNome(nome(nameDoc));

                        TreeMap<Report, Docente> treeMap = tmpDip.ricercaReportSoloDocente(docente);
                        searchReport(root, arrRep, arrDoc, treeMap);
                    } else root.put("emptyy", "empty");
                    sendJson(response, root);
                    break;
                }

                case "/delete": {
                    String str = request.getParameter("report");
                    JSONObject root = new JSONObject();

                    if (str.compareTo("") != 0){
                        ReportDAO reportDAO = new ReportDAO();
                        DipartimentoDAO dipartimentoDAO = new DipartimentoDAO();
                        String[] idReport = str.split(",");
                        Dipartimento tmpDip = dipartimentoDAO.doRetrieveById(direttore.getDipartimento().getCodice());
                        JSONArray arrRep = new JSONArray();
                        root.put("listReports", arrRep);

                        for (String id : idReport) {
                            tmpDip.eliminaReport(reportDAO.doRetrieveById(Integer.parseInt(id)));
                            JSONObject obj = new JSONObject();
                            obj.put("report", id);
                            arrRep.add(obj);
                        }
                    }else root.put("listReports", "empty");
                    sendJson(response, root);
                    break;
                }
            }
        } catch(SQLException ex){
            log(ex.getMessage());
        } catch (ParseException e){
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
            e.handle(request, response);
        }
    }

    private void search(Date date1, Date date2, String nameDoc, Dipartimento tmpDip,
                        JSONObject root, JSONArray arrRep, JSONArray arrDoc, HttpServletResponse response)
            throws SQLException, InvalidRequestException, IOException {

        if (nameDoc != null && nameDoc.compareTo("") != 0){
            Docente docente = new Docente();
            docente.setCognome(cognome(nameDoc));
            docente.setNome(nome(nameDoc));

            TreeMap<Report, Docente> treeMap = tmpDip.ricercaCompletaReport(docente, date1, date2);
            searchReport(root, arrRep, arrDoc, treeMap);
        } else {
            TreeMap<Report, Docente> treeMap = tmpDip.ricercaReportSoloData(date1, date2);
            searchReport(root, arrRep, arrDoc, treeMap);
        }
        sendJson(response, root);
    }

    private void searchReport(JSONObject root, JSONArray arrRep, JSONArray arrDoc,
                              TreeMap<Report, Docente> treeMap) {
        if (treeMap.isEmpty())
            root.put("emptyy", "empty");
        else {
            root.put("listDoc", arrDoc);
            root.put("listRep", arrRep);

            for (Report r : treeMap.keySet()){
                JSONObject obj = new JSONObject();
                JSONObject obj2 = new JSONObject();
                obj.put("report", r.toJson());
                arrRep.add(obj);
                obj2.put("docenti", treeMap.get(r).toJson());
                arrDoc.add(obj2);
            }
        }
    }

    private String nome(String str){
        String[] token = str.split(" ");
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < token.length; i++) {
            if (!checkUppercase(token[i])) {
                out.append(token[i]);
                if ((i + 1) < token.length)
                    if (!checkUppercase(token[i + 1]))
                        out.append(" ");
            }
        }
        return out.toString();
    }

    private String cognome(String str){
        StringBuilder out = new StringBuilder();
        String[] token = str.split(" ");

        for (int i = 0; i < token.length; i++) {
            if (checkUppercase(token[i])) {
                out.append(token[i]);
                if ((i + 1) < token.length)
                    if (checkUppercase(token[i + 1]))
                        out.append(" ");
            }
        }
        return out.toString();
    }

    private boolean checkUppercase(String str){
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            //if any character is not in upper case, return false
            if (!Character.isUpperCase(c))
                return false;
        }
        return true;
    }
}
