package controller.gestioneCasting;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@WebServlet("/cd/create-casting")
public class AddCasting extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");

        if (user == null || user.getRole() != UserDTO.Role.CastingDirector) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);
        ProductionDAO prodDAO = new ProductionDAO(ds);

        try {
            CastingDirectorDTO cdDTO = cdDAO.getByUserID(user.getUserID());
            if (cdDTO == null) {
                NotificationUtil.sendNotification(req, "Profilo non trovato.", "error");
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }

            List<ProductionDTO> myProductions = prodDAO.getProductionsByCdID(cdDTO.getCdID());

            req.setAttribute("myProductions", myProductions);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/cd/create-casting.jsp");
            dispatcher.forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = (UserDTO) req.getSession().getAttribute("user");

        String title = req.getParameter("title");
        String location = req.getParameter("location");
        String categoryStr = req.getParameter("category");
        String deadlineStr = req.getParameter("deadline");
        String description = req.getParameter("description");
        String productionIdStr = req.getParameter("productionID");

        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        CastingDAO castingDAO = new CastingDAO(ds);
        CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);

        try {
            CastingDirectorDTO cdDTO = cdDAO.getByUserID(user.getUserID());

            CastingDTO casting = new CastingDTO();
            casting.setTitle(title);
            casting.setLocation(location);
            casting.setDescription(description);
            casting.setCdID(cdDTO.getCdID());
            casting.setProductionID(Integer.parseInt(productionIdStr));
            casting.setPublishDate(LocalDateTime.now()); // Published now

            try {
                casting.setCategory(CastingDTO.Category.valueOf(categoryStr));
            } catch (IllegalArgumentException e) {
                NotificationUtil.sendNotification(req, "Categoria non valida.", "error");
                doGet(req, resp);
                return;
            }

            LocalDate dDate = LocalDate.parse(deadlineStr);
            casting.setDeadline(dDate.atTime(LocalTime.MAX)); // Set to 23:59:59 of that day

            if (casting.getDeadline().isBefore(LocalDateTime.now().plusDays(7))) {
                NotificationUtil.sendNotification(req, "La data di scadenza deve essere almeno tra una settimana.", "warning");
                doGet(req, resp);
                return;
            }

            castingDAO.save(casting);

            NotificationUtil.sendNotification(req, "Casting pubblicato con successo!", "success");
            resp.sendRedirect(req.getContextPath() + "/cd/view-castings");

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.sendNotification(req, "Errore nella creazione del casting.", "error");
            doGet(req, resp);
        }
    }
}