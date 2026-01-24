package controller.gestioneValutazioneCandidature;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.*;
import model.dto.*;
import utils.NotificationUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/cd/applications")
public class GetApplications extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null || user.getRole() != UserDTO.Role.CastingDirector) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String castingIdStr = req.getParameter("id");
        if (castingIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        ApplicationDAO appDAO = new ApplicationDAO(ds);
        CastingDAO castingDAO = new CastingDAO(ds);
        PerformerDAO perfDAO = new PerformerDAO(ds);
        UserDAO userDAO = new UserDAO(ds);
        ProductionDAO prodDAO = new ProductionDAO(ds);

        try {
            int castingID = Integer.parseInt(castingIdStr);

            CastingDTO casting = castingDAO.getByID(castingID);

            CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);
            CastingDirectorDTO cdDTO = cdDAO.getByUserID(user.getUserID());
            if (casting == null || casting.getCdID() != cdDTO.getCdID()) {
                NotificationUtil.sendNotification(req, "Accesso negato.", "error");
                resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
                return;
            }

            List<ProductionDTO> authorizedProductions = prodDAO.getProductionsByCdID(cdDTO.getCdID());

            boolean isStillInTeam = authorizedProductions.stream()
                    .anyMatch(p -> p.getProductionID() == casting.getProductionID());

            if (!isStillInTeam) {
                NotificationUtil.sendNotification(req, "Non puoi visualizzare le candidature: sei stato rimosso dal team.", "error");
                resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
                return;
            }

            Collection<ApplicationDTO> applications = appDAO.getByCastingID(castingID);

            // Chiave: PerformerID -> Valore: UserDTO (per i nomi)
            Map<Integer, UserDTO> userDetails = new HashMap<>();
            // Chiave: PerformerID -> Valore: PerformerDTO (per foto/cv)
            Map<Integer, PerformerDTO> performerProfiles = new HashMap<>();

            for (ApplicationDTO app : applications) {
                int perfID = app.getPerformerID();

                if (!performerProfiles.containsKey(perfID)) {
                    PerformerDTO p = perfDAO.getByID(perfID);
                    if (p != null) {
                        performerProfiles.put(perfID, p);
                        UserDTO u = userDAO.getByID(p.getUserID());
                        userDetails.put(perfID, u);
                    }
                }
            }

            req.setAttribute("casting", casting);
            req.setAttribute("applications", applications);
            req.setAttribute("userDetails", userDetails);
            req.setAttribute("performerProfiles", performerProfiles);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/cd/view-candidates.jsp");
            dispatcher.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.sendNotification(req, "Errore nel recupero candidati.", "error");
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
        }
    }
}