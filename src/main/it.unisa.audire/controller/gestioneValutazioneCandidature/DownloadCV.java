package controller.gestioneValutazioneCandidature;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.PerformerDAO;
import model.dto.PerformerDTO;
import model.dto.UserDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/download-cv")
public class DownloadCV extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Devi effettuare il login.");
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID Performer mancante.");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        PerformerDAO perfDAO = new PerformerDAO(ds);

        try {
            int performerID = Integer.parseInt(idStr);

            PerformerDTO performer = perfDAO.getCvData(performerID);

            if (performer == null || performer.getCvData() == null || performer.getCvData().length == 0) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Nessun CV presente per questo utente.");
                return;
            }

            boolean isCD = (user.getRole() == UserDTO.Role.CastingDirector);
            boolean isOwner = false;

            if (!isCD && user.getRole() != UserDTO.Role.Performer) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Non hai i permessi.");
                return;
            }

            String mimeType = performer.getCvMimeType();
            if (mimeType == null) mimeType = "application/octet-stream"; // Default binario
            resp.setContentType(mimeType);

            resp.setContentLength(performer.getCvData().length);

            String extension = "pdf"; // Default comune
            if (mimeType.contains("word")) extension = "doc";
            if (mimeType.contains("image")) extension = "jpg";

            String fileName = "CV_Performer_" + performerID + "." + extension;

            resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            try (ServletOutputStream out = resp.getOutputStream()) {
                out.write(performer.getCvData());
                out.flush();
            }

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID non valido.");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore database.");
        }
    }
}