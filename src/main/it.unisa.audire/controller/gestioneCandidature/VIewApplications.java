package controller.gestioneCandidature;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.*;
import model.dto.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/performer/applications")
public class VIewApplications extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null || user.getRole() != UserDTO.Role.Performer) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        PerformerDAO perfDAO = new PerformerDAO(ds);
        ApplicationDAO appDAO = new ApplicationDAO(ds);
        CastingDAO castingDAO = new CastingDAO(ds);

        try {
            PerformerDTO perfDTO = perfDAO.getByUserID(user.getUserID());

            Collection<ApplicationDTO> applications = appDAO.getByPerformerID(perfDTO.getPerformerID());

            // Mappa per i Titoli (CastingID -> Titolo)
            Map<Integer, String> castingTitles = new HashMap<>();

            for (ApplicationDTO app : applications) {
                CastingDTO c = castingDAO.getByID(app.getCastingID());
                if (c != null) {
                    castingTitles.put(app.getCastingID(), c.getTitle());
                } else {
                    castingTitles.put(app.getCastingID(), "Casting rimosso");
                }
            }

            req.setAttribute("applications", applications);
            req.setAttribute("castingTitles", castingTitles);

            req.getRequestDispatcher("/WEB-INF/views/performer/my-applications.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}
