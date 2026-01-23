package controller.gestioneTeam;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.CastingDirectorDAO;
import model.dao.ProductionDAO;
import model.dao.ProductionManagerDAO;
import model.dao.TeamDAO;
import model.dto.*;
import utils.NotificationUtil;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

@WebServlet("/pm/team")
public class AssignCD extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null || user.getRole() != UserDTO.Role.ProductionManager) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String prodIdStr = req.getParameter("id");
        if (prodIdStr == null || prodIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/pm/productions");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        ProductionDAO prodDAO = new ProductionDAO(ds);
        TeamDAO teamDAO = new TeamDAO(ds);
        ProductionManagerDAO pmDAO = new ProductionManagerDAO(ds);

        try {
            int prodID = Integer.parseInt(prodIdStr);
            ProductionDTO production = prodDAO.getByID(prodID);
            ProductionManagerDTO pmDTO = pmDAO.getByUserID(user.getUserID());

            if (production == null) {
                NotificationUtil.sendNotification(req, "Produzione non trovata.", "error");
                resp.sendRedirect(req.getContextPath() + "/pm/productions");
                return;
            }

            if (pmDTO == null || production.getPmID() != pmDTO.getPmID()) {
                NotificationUtil.sendNotification(req, "Accesso negato.", "error");
                resp.sendRedirect(req.getContextPath() + "/pm/productions");
                return;
            }

            //Membri attuali
            List<UserDTO> currentTeam = teamDAO.getTeamMembers(prodID);

            //CD Disponibili
            List<UserDTO> availableCDs = teamDAO.getAvailableCastingDirectors(prodID);

            req.setAttribute("production", production);
            req.setAttribute("currentTeam", currentTeam);
            req.setAttribute("availableCDs", availableCDs);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/pm/manage-team.jsp");
            dispatcher.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/pm/productions");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String prodIdStr = req.getParameter("productionId");

        String userIdStr = req.getParameter("userId");

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        TeamDAO teamDAO = new TeamDAO(ds);
        CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);

        try {
            int prodID = Integer.parseInt(prodIdStr);
            int userId = Integer.parseInt(userIdStr);

            CastingDirectorDTO cdDTO = cdDAO.getByUserID(userId);

            if (cdDTO != null) {
                if ("add".equals(action)) {
                    TeamDTO team = new TeamDTO(prodID, cdDTO.getCdID());
                    teamDAO.save(team);
                    NotificationUtil.sendNotification(req, "Casting Director aggiunto con successo!", "success");
                }
            } else {
                NotificationUtil.sendNotification(req, "Errore: Profilo Casting Director non trovato.", "error");
            }

            resp.sendRedirect(req.getContextPath() + "/pm/team?id=" + prodID);

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.sendNotification(req, "Errore durante l'operazione.", "error");
            resp.sendRedirect(req.getContextPath() + "/pm/productions");
        }
    }
}