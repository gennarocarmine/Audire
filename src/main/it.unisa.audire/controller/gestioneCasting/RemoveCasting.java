package controller.gestioneCasting;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.CastingDAO;
import model.dao.CastingDirectorDAO;
import model.dto.CastingDTO;
import model.dto.CastingDirectorDTO;
import model.dto.UserDTO;
import utils.NotificationUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/cd/delete-casting")
public class RemoveCasting extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserDTO user = (session != null) ? (UserDTO) session.getAttribute("user") : null;

        if (user == null || user.getRole() != UserDTO.Role.CastingDirector) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            NotificationUtil.sendNotification(req, "ID Casting non valido.", "error");
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        CastingDAO castingDAO = new CastingDAO(ds);
        CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);

        try {
            int castingID = Integer.parseInt(idStr);

            CastingDTO casting = castingDAO.getByID(castingID);
            CastingDirectorDTO currentCd = cdDAO.getByUserID(user.getUserID());

            if (casting == null) {
                NotificationUtil.sendNotification(req, "Casting non trovato.", "error");
                resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
                return;
            }


            if (currentCd == null || casting.getCdID() != currentCd.getCdID()) {
                NotificationUtil.sendNotification(req, "Non hai i permessi per eliminare questo casting.", "error");
                resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
                return;
            }

            boolean deleted = castingDAO.delete(castingID);

            if (deleted) {
                NotificationUtil.sendNotification(req, "Casting eliminato con successo.", "success");
            } else {
                NotificationUtil.sendNotification(req, "Impossibile eliminare il casting.", "error");
            }

        } catch (NumberFormatException e) {
            NotificationUtil.sendNotification(req, "Formato ID non valido.", "error");
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationUtil.sendNotification(req, "Impossibile eliminare: ci sono candidature associate o errore DB.", "error");
        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.sendNotification(req, "Errore imprevisto del server.", "error");
        }

        resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
    }
}