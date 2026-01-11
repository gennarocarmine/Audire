package controller.gestioneCasting; // O controller.cd a seconda del tuo package

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.CastingDAO;
import model.dao.CastingDirectorDAO;
import model.dao.ProductionDAO;
import model.dto.CastingDTO;
import model.dto.CastingDirectorDTO;
import model.dto.UserDTO;
import utils.NotificationUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/cd/view-castings")
public class GetCastings extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null || user.getRole() != UserDTO.Role.CastingDirector) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);
        CastingDAO castingDAO = new CastingDAO(ds);
        ProductionDAO prodDAO = new ProductionDAO(ds);

        try {
            CastingDirectorDTO cdDTO = cdDAO.getByUserID(user.getUserID());
            if (cdDTO == null) {
                NotificationUtil.sendNotification(req, "Errore profilo utente.", "error");
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }

            Collection<CastingDTO> castings = castingDAO.getByCdID(cdDTO.getCdID());

            // ID Casting -> Valore: Titolo Produzione
            Map<Integer, String> productionTitles = new HashMap<>();

            for (CastingDTO c : castings) {
                String title = prodDAO.getTitleByID(c.getProductionID());
                productionTitles.put(c.getCastingID(), title);
            }

            req.setAttribute("castings", castings);
            req.setAttribute("productionTitles", productionTitles);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/cd/view-castings.jsp");
            dispatcher.forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Errore nel recupero dei dati.");
            req.getRequestDispatcher("/WEB-INF/views/cd/view-castings.jsp").forward(req, resp);
        }
    }
}