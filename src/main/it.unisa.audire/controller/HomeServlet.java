package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.CastingDAO;
import model.dao.ProductionDAO;
import model.dto.CastingDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        CastingDAO castingDAO = new CastingDAO(ds);
        ProductionDAO prodDAO = new ProductionDAO(ds);

        try {
            List<CastingDTO> activeCastings = castingDAO.getAllActive();

            Map<Integer, String> productionTitles = new HashMap<>();

            for (CastingDTO c : activeCastings) {
                String title = prodDAO.getTitleByID(c.getProductionID());
                productionTitles.put(c.getCastingID(), title);
            }

            req.setAttribute("activeCastings", activeCastings);
            req.setAttribute("productionTitles", productionTitles);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore Database");
        }
    }
}