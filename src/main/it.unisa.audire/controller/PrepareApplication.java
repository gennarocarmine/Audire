package controller;

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

@WebServlet("/performer/review-application")
public class PrepareApplication extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        CastingDAO castingDAO = new CastingDAO(ds);
        PerformerDAO perfDAO = new PerformerDAO(ds);
        ApplicationDAO appDAO = new ApplicationDAO(ds);
        ProductionDAO prodDAO = new ProductionDAO(ds);

        try {
            int castingID = Integer.parseInt(castingIdStr);

            PerformerDTO perfDTO = perfDAO.getByUserID(user.getUserID());

            if (perfDTO == null) {
                NotificationUtil.sendNotification(req, "Completa il tuo profilo prima di candidarti.", "warning");
                resp.sendRedirect(req.getContextPath() + "/performer/profile");
                return;
            }

            if (appDAO.hasApplied(perfDTO.getPerformerID(), castingID)) {
                NotificationUtil.sendNotification(req, "Hai già inviato la candidatura per questo casting.", "info");
                resp.sendRedirect(req.getContextPath() + "/performer/applications");
                return;
            }

            CastingDTO casting = castingDAO.getByID(castingID);
            if (casting == null) {
                NotificationUtil.sendNotification(req, "Casting non trovato o rimosso.", "error");
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }

            String productionTitle = prodDAO.getTitleByID(casting.getProductionID());

            req.setAttribute("casting", casting);
            req.setAttribute("productionTitle", productionTitle);
            req.setAttribute("performer", perfDTO);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/performer/review-application.jsp");
            dispatcher.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.sendNotification(req, "Errore nel caricamento della pagina.", "error");
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}
