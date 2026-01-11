package controller.gestioneCasting;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.CastingDAO;
import model.dao.CastingDirectorDAO;
import model.dao.ProductionDAO;
import model.dto.CastingDTO;
import model.dto.CastingDirectorDTO;
import model.dto.ProductionDTO;
import model.dto.UserDTO;
import utils.NotificationUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/cd/edit-casting")
public class UpdateCasting extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null || user.getRole() != UserDTO.Role.CastingDirector) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        CastingDAO castingDAO = new CastingDAO(ds);
        CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);
        ProductionDAO prodDAO = new ProductionDAO(ds);

        try {
            int castingID = Integer.parseInt(idStr);

            CastingDirectorDTO currentCd = cdDAO.getByUserID(user.getUserID());
            CastingDTO casting = castingDAO.getByID(castingID);

            if (casting == null) {
                NotificationUtil.sendNotification(req, "Casting non trovato.", "error");
                resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
                return;
            }

            if (currentCd == null || casting.getCdID() != currentCd.getCdID()) {
                NotificationUtil.sendNotification(req, "Non hai i permessi per modificare questo casting.", "error");
                resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
                return;
            }

            List<ProductionDTO> myProductions = prodDAO.getProductionsByCdID(currentCd.getCdID());

            req.setAttribute("casting", casting);
            req.setAttribute("myProductions", myProductions);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/cd/edit-casting.jsp");
            dispatcher.forward(req, resp);

        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");
        if (user == null || user.getRole() != UserDTO.Role.CastingDirector) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String idStr = req.getParameter("id");
        String title = req.getParameter("title");
        String location = req.getParameter("location");
        String categoryStr = req.getParameter("category");
        String deadlineStr = req.getParameter("deadline");
        String description = req.getParameter("description");
        String productionIdStr = req.getParameter("productionID");

        List<String> errors = new ArrayList<>();
        if (title == null || title.trim().isEmpty()) errors.add("Il titolo è obbligatorio.");
        if (deadlineStr == null || deadlineStr.isEmpty()) errors.add("La scadenza è obbligatoria.");

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            doGet(req, resp);
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        CastingDAO castingDAO = new CastingDAO(ds);
        CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);

        try {
            int castingID = Integer.parseInt(idStr);
            CastingDirectorDTO currentCd = cdDAO.getByUserID(user.getUserID());

            CastingDTO casting = castingDAO.getByID(castingID);

            if (casting == null || currentCd == null || casting.getCdID() != currentCd.getCdID()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            casting.setTitle(title.trim());
            casting.setLocation(location.trim());
            casting.setDescription(description.trim());
            casting.setProductionID(Integer.parseInt(productionIdStr));

            try {
                casting.setCategory(CastingDTO.Category.valueOf(categoryStr));
            } catch (IllegalArgumentException e) {
                errors.add("Categoria non valida.");
            }

            try {
                LocalDate dDate = LocalDate.parse(deadlineStr);
                casting.setDeadline(dDate.atTime(LocalTime.MAX));

                if (casting.getDeadline().isBefore(LocalDateTime.now())) {
                    errors.add("La nuova scadenza non può essere nel passato.");
                }
            } catch (Exception e) {
                errors.add("Formato data non valido.");
            }

            if (!errors.isEmpty()) {
                req.setAttribute("errors", errors);
                req.setAttribute("casting", casting);
                doGet(req, resp); // Ricarica
                return;
            }

            castingDAO.save(casting);

            NotificationUtil.sendNotification(req, "Casting aggiornato con successo!", "success");
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Errore durante il salvataggio.");
            doGet(req, resp);
        }
    }
}