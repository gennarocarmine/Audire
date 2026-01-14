package controller.gestioneCandidature;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.ApplicationDAO;
import model.dao.PerformerDAO;
import model.dto.ApplicationDTO;
import model.dto.PerformerDTO;
import model.dto.UserDTO;
import utils.NotificationUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

@WebServlet("/performer/apply")
public class SendApplication extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");

        if (user == null || user.getRole() != UserDTO.Role.Performer) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String castingIdStr = req.getParameter("id");
        if (castingIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        PerformerDAO perfDAO = new PerformerDAO(ds);
        ApplicationDAO appDAO = new ApplicationDAO(ds);

        try {
            int castingID = Integer.parseInt(castingIdStr);

            PerformerDTO perfDTO = perfDAO.getByUserID(user.getUserID());

            if (perfDTO == null) {
                NotificationUtil.sendNotification(req, "Errore profilo: Dati mancanti.", "error");
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }

            if (appDAO.hasApplied(perfDTO.getPerformerID(), castingID)) {
                NotificationUtil.sendNotification(req, "Hai già inviato la candidatura per questo casting.", "warning");
            } else {
                ApplicationDTO app = new ApplicationDTO();
                app.setPerformerID(perfDTO.getPerformerID());
                app.setCastingID(castingID);
                app.setSendingDate(LocalDateTime.now());
                app.setStatus(ApplicationDTO.Status.In_attesa);
                app.setFeedback(""); // Feedback vuoto inizialmente

                appDAO.save(app);

                NotificationUtil.sendNotification(req, "Candidatura inviata con successo! In bocca al lupo.", "success");
            }

            resp.sendRedirect(req.getContextPath() + "/performer/applications");

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.sendNotification(req, "Errore tecnico durante l'invio.", "error");
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}