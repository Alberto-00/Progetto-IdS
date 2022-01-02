package Storage.SessioneDiValidazione;

import ApplicationLogic.Utils.ServletLogic;
import Storage.PersonaleUnisa.Docente.Docente;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

import io.nayuki.qrcodegen.QrCode;

public class SessioneDiValidazione {

    private String qRCode;
    private boolean isInCorso;
    private Docente docente;
    private static final String url = "http://localhost:8080/EasyPass_war_exploded/sessioneServlet/showQRCode?sessionId=";

    public SessioneDiValidazione(boolean isInCorso, Docente docente) throws IOException, SQLException {
        Random r = new Random();
        int sessionId = 0;
        SessioneDiValidazione foundSession = null;
        SessioneDiValidazioneDAO sessioneDAO = new SessioneDiValidazioneDAO();
        File file;
        do {
            sessionId = r.nextInt(100000);
            foundSession = sessioneDAO.doRetrieveById(sessionId);
        }while (foundSession != null);

        BufferedImage qrImg = createqRCode(url+sessionId);
        String uploadPath = ServletLogic.getUploadPath() + "QRcodes" + File.separator;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(qrImg, "jpg", os);
        try(InputStream is = new ByteArrayInputStream(os.toByteArray())){
            file = new File(uploadPath + sessionId + ".jpg");
            System.out.println("Ho creato il file: "+ file.toPath());
            Files.copy(is, file.toPath());
        }


        System.out.println("SessionID = " + sessionId);
        this.isInCorso = isInCorso;
        this.docente = docente;
        this.qRCode = sessionId + ".jpg";
        System.out.println(this.qRCode);
    }

    public SessioneDiValidazione() {

        this.qRCode="";
        this.isInCorso=false;
        this.docente=null;
    }

    public SessioneDiValidazione(String qRCode, boolean isInCorso, Docente docente) {
        this.qRCode = qRCode;
        this.isInCorso = isInCorso;
        this.docente = docente;
    }

    private BufferedImage createqRCode(String url) throws IOException {
        QrCode qr0 = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
        BufferedImage img = toImage(qr0, 4, 10);  // See QrCodeGeneratorDemo
        return img;
    }

    public String getqRCode() {
        return qRCode;
    }

    public void setqRCode(String qRCode) {
        this.qRCode = qRCode;
    }

    public boolean isInCorso() {
        return isInCorso;
    }

    public void setInCorso(boolean inCorso) {
        isInCorso = inCorso;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    private static BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
        Objects.requireNonNull(qr);
        if (scale <= 0 || border < 0)
            throw new IllegalArgumentException("Value out of range");
        if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
            throw new IllegalArgumentException("Scale or border too large");

        BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                boolean color = qr.getModule(x / scale - border, y / scale - border);
                result.setRGB(x, y, color ? darkColor : lightColor);
            }
        }
        return result;
    }

    private static BufferedImage toImage(QrCode qr, int scale, int border) {
        return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
    }

    @Override
    public String toString() {
        return "SessioneDiValidazione{" +
                "qRCode='" + qRCode + '\'' +
                ", isInCorso=" + isInCorso +
                ", docente=" + docente +
                '}';
    }
}
