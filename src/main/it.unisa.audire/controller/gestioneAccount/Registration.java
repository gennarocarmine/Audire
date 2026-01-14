package controller.gestioneAccount;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.dao.CastingDirectorDAO;
import model.dao.PerformerDAO;
import model.dao.ProductionManagerDAO;
import model.dao.UserDAO;
import model.dto.CastingDirectorDTO;
import model.dto.PerformerDTO;
import model.dto.ProductionManagerDTO;
import model.dto.UserDTO;
import utils.NotificationUtil;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@WebServlet("/registration")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class Registration extends HttpServlet {

    // Regex Patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{8,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");
        String phoneNumber = req.getParameter("phoneNumber");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String roleStr = req.getParameter("role");

        List<String> errors = new ArrayList<>();
        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/register.jsp");


        if (firstName == null || firstName.trim().isEmpty()) errors.add("Nome obbligatorio.");
        if (lastName == null || lastName.trim().isEmpty()) errors.add("Cognome obbligatorio.");

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) errors.add("Email non valida.");

        if (phoneNumber == null || !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            errors.add("Il numero di telefono deve contenere esattamente 10 cifre.");
        }

        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            errors.add("Password debole (min 8 caratteri, Maiuscola, Minuscola, Numero, Speciale).");
        }
        if (!password.equals(confirmPassword)) errors.add("Le password non coincidono.");


        UserDTO.Role roleEnum = null;
        try {
            if (roleStr != null && !roleStr.isEmpty()) {
                roleEnum = UserDTO.Role.valueOf(roleStr);
            } else {
                errors.add("Seleziona un ruolo.");
            }
        } catch (IllegalArgumentException e) {
            errors.add("Ruolo non valido.");
        }


        if (roleEnum == UserDTO.Role.Performer) {
            String genderParam = req.getParameter("gender");
            String catParam = req.getParameter("category");
            String description = req.getParameter("description");
            Part photoPart = req.getPart("profilePhoto");
            Part cvPart = req.getPart("cvFile");

            if (genderParam == null || genderParam.isEmpty()) errors.add("Seleziona un genere.");
            if (catParam == null || catParam.isEmpty()) errors.add("Seleziona una categoria.");
            if (description == null || description.trim().isEmpty()) errors.add("Inserisci una descrizione.");

            // Verifica presenza file foto
            if (photoPart == null || photoPart.getSize() == 0) {
                errors.add("La foto profilo è obbligatoria per i Performer.");
            }

            if (cvPart == null || cvPart.getSize() == 0) {
                errors.add("Il Curriculum Vitae è obbligatorio.");
            } else if (!cvPart.getContentType().equals("application/pdf")) {
                errors.add("Il CV deve essere in formato PDF.");
            }
        }


        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            dispatcher.forward(req, resp);
            return;
        }


        DataSource ds = (DataSource) getServletContext().getAttribute("ds");
        UserDAO userDAO = new UserDAO(ds);

        try {
            if (userDAO.getByEmail(email) != null) {
                errors.add("Esiste già un account con questa email.");
                req.setAttribute("errors", errors);
                dispatcher.forward(req, resp);
                return;
            }

            UserDTO newUser = new UserDTO();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setPhoneNumber(phoneNumber);
            newUser.setRole(roleEnum);

            newUser.setRegistrationDate(LocalDateTime.now());

            // Hash Password
            String hashedPassword = userDAO.hashPassword(password);
            newUser.setPasswordHash(hashedPassword);


            userDAO.save(newUser);
            int newUserId = newUser.getUserID();


            if (roleEnum == UserDTO.Role.Performer) {
                savePerformerProfile(req, ds, newUserId);
            }
            else if (roleEnum == UserDTO.Role.CastingDirector) {
                CastingDirectorDAO cdDAO = new CastingDirectorDAO(ds);
                CastingDirectorDTO cd = new CastingDirectorDTO();
                cd.setUserID(newUserId);
                cdDAO.save(cd);
            }
            else if (roleEnum == UserDTO.Role.ProductionManager) {
                ProductionManagerDAO pmDAO = new ProductionManagerDAO(ds);
                ProductionManagerDTO pm = new ProductionManagerDTO();
                pm.setUserID(newUserId);
                pmDAO.save(pm);
            }

            HttpSession session = req.getSession();
            session.setAttribute("user", newUser);
            NotificationUtil.sendNotification(req, "Registrazione completata! Benvenut* " + firstName, "success");
            resp.sendRedirect(req.getContextPath() + "/login");

        } catch (SQLException e) {
            e.printStackTrace();
            errors.add("Errore del server durante la registrazione. Riprova più tardi.");
            req.setAttribute("errors", errors);
            dispatcher.forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            errors.add("Errore imprevisto: " + e.getMessage());
            req.setAttribute("errors", errors);
            dispatcher.forward(req, resp);
        }
    }

    // Metodo helper per salvare il profilo Performer
    private void savePerformerProfile(HttpServletRequest req, DataSource ds, int userId) throws Exception {
        PerformerDAO performerDAO = new PerformerDAO(ds);
        PerformerDTO performer = new PerformerDTO();

        performer.setUserID(userId);
        performer.setDescription(req.getParameter("description"));

        performer.setGender(PerformerDTO.Gender.valueOf(req.getParameter("gender")));
        performer.setCategory(PerformerDTO.Category.valueOf(req.getParameter("category")));


        String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdir();


        String photoName = saveFile(req.getPart("profilePhoto"), uploadPath);
        performer.setProfilePhoto(photoName);

        Part cvPart = req.getPart("cvFile");
        if (cvPart != null && cvPart.getSize() > 0) {
            performer.setCvData(cvPart.getInputStream().readAllBytes());
            performer.setCvMimeType(cvPart.getContentType());
        }

        performerDAO.save(performer);
    }


    private String saveFile(Part part, String uploadPath) throws IOException {
        if (part != null && part.getSize() > 0) {
            String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            // Rimuovi spazi e caratteri speciali dal nome file
            fileName = fileName.replaceAll("\\s+", "_");
            // Aggiungi UUID per renderlo unico
            String uniqueName = UUID.randomUUID().toString() + "_" + fileName;

            part.write(uploadPath + File.separator + uniqueName);
            return uniqueName;
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/register.jsp");
        dispatcher.forward(req, resp);
    }
}