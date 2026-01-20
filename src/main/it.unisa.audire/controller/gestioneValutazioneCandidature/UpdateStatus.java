package controller.gestioneValutazioneCandidature;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.ApplicationDAO;
import model.dao.CastingDAO;
import model.dao.CastingDirectorDAO;
import model.dto.ApplicationDTO;
import model.dto.CastingDTO;
import model.dto.CastingDirectorDTO;
import model.dto.UserDTO;
import utils.NotificationUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/cd/update-status")
public class UpdateStatus extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null || user.getRole() != UserDTO.Role.CastingDirector) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String appIdStr = req.getParameter("appId");
        String statusStr = req.getParameter("status");
        String feedback = req.getParameter("feedback");

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        ApplicationDAO appDAO = new ApplicationDAO(ds);
        CastingDAO castingDAO = new CastingDAO(ds);
        CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);

        try {
            int appID = Integer.parseInt(appIdStr);

            ApplicationDTO app = appDAO.getByID(appID);
            if (app == null) {
                NotificationUtil.sendNotification(req, "Candidatura non trovata.", "error");
                resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
                return;
            }

            CastingDTO casting = castingDAO.getByID(app.getCastingID());
            CastingDirectorDTO currentCD = cdDAO.getByUserID(user.getUserID());

            if (casting == null || currentCD == null || casting.getCdID() != currentCD.getCdID()) {
                NotificationUtil.sendNotification(req, "Non hai i permessi per valutare questa candidatura.", "error");
                resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
                return;
            }

            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    app.setStatus(ApplicationDTO.Status.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    NotificationUtil.sendNotification(req, "Stato non valido.", "error");
                }
            }

            if (feedback != null) {
                app.setFeedback(feedback.trim());
            }

            appDAO.save(app);

            NotificationUtil.sendNotification(req, "Valutazione aggiornata: " + app.getStatus(), "success");

            resp.sendRedirect(req.getContextPath() + "/cd/applications?id=" + casting.getCastingID());

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.sendNotification(req, "Errore durante l'aggiornamento.", "error");
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
        }
    }
}