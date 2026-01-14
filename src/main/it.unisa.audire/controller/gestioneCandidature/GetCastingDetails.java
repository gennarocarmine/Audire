package controller.gestioneCandidature;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
        import model.dao.*;
        import model.dto.*;

        import javax.sql.DataSource;
import java.io.IOException;

@WebServlet("/casting-details")
public class GetCastingDetails extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        CastingDAO castingDAO = new CastingDAO(ds);
        ProductionDAO prodDAO = new ProductionDAO(ds);
        ApplicationDAO appDAO = new ApplicationDAO(ds);
        PerformerDAO perfDAO = new PerformerDAO(ds);

        try {
            int castingID = Integer.parseInt(idStr);
            CastingDTO casting = castingDAO.getByID(castingID);

            if (casting == null) {
                resp.sendError(404, "Casting non trovato");
                return;
            }

            // Recupera Titolo Produzione
            String productionTitle = prodDAO.getTitleByID(casting.getProductionID());

            boolean alreadyApplied = false;
            UserDTO user = (UserDTO) req.getSession().getAttribute("user");

            if (user != null && user.getRole() == UserDTO.Role.Performer) {
                PerformerDTO perf = perfDAO.getByUserID(user.getUserID());
                if (perf != null) {
                    alreadyApplied = appDAO.hasApplied(perf.getPerformerID(), castingID);
                }
            }

            req.setAttribute("casting", casting);
            req.setAttribute("productionTitle", productionTitle);
            req.setAttribute("alreadyApplied", alreadyApplied);

            req.getRequestDispatcher("/WEB-INF/views/casting-details.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}