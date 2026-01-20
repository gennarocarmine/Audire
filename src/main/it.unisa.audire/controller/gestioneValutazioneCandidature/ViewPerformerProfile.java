package controller.gestioneValutazioneCandidature;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.*;
import model.dto.*;
import javax.sql.DataSource;
import java.io.IOException;

@WebServlet("/cd/performer-profile")
public class ViewPerformerProfile extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null || user.getRole() != UserDTO.Role.CastingDirector) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String perfIdStr = req.getParameter("id");
        String appIdStr = req.getParameter("appId");

        if (perfIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        PerformerDAO perfDAO = new PerformerDAO(ds);
        UserDAO userDAO = new UserDAO(ds);
        ApplicationDAO appDAO = new ApplicationDAO(ds);

        try {
            int perfID = Integer.parseInt(perfIdStr);

            PerformerDTO performer = perfDAO.getByID(perfID);
            if (performer == null) {
                resp.sendError(404, "Performer non trovato");
                return;
            }

            UserDTO performerUser = userDAO.getByID(performer.getUserID());

            if (appIdStr != null && !appIdStr.isEmpty()) {
                ApplicationDTO application = appDAO.getByID(Integer.parseInt(appIdStr));
                req.setAttribute("application", application);
            }

            req.setAttribute("performer", performer);
            req.setAttribute("performerUser", performerUser);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/cd/performer-profile.jsp");
            dispatcher.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
        }
    }
}
