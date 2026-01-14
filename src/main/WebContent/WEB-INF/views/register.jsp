<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Registrazione" scope="request"/>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle}</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

<jsp:include page="/WEB-INF/components/header.jsp" />

<main class="container mt-3 mb-3">
    <div class="card registration-card">
        <h1 class="text-center mb-2">Crea il tuo Account</h1>

        <c:if test="${not empty errors}">
            <div class="alert-danger mb-2">
                <c:forEach var="error" items="${errors}">
                    <p>${error}</p>
                </c:forEach>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/registration" method="post"
              enctype="multipart/form-data" id="registrationForm" novalidate>

            <div class="mb-2 registration-section">
                <h3 class="registration-section-title">
                    <i class="fas fa-user"></i> Informazioni Personali
                </h3>

                <div class="mb-2">
                    <label for="firstName">Nome <span class="required-asterisk">*</span></label>
                    <input type="text" id="firstName" name="firstName"
                           value="${param.firstName}" required
                           pattern="[A-Za-zÀ-ÿ\s']{2,50}"
                           title="Il nome deve contenere solo lettere (2-50 caratteri)">
                </div>

                <div class="mb-2">
                    <label for="lastName">Cognome <span class="required-asterisk">*</span></label>
                    <input type="text" id="lastName" name="lastName"
                           value="${param.lastName}" required
                           pattern="[A-Za-zÀ-ÿ\s']{2,50}"
                           title="Il cognome deve contenere solo lettere (2-50 caratteri)">
                </div>

                <div class="mb-2">
                    <label for="email">Email <span class="required-asterisk">*</span></label>
                    <input type="email" id="email" name="email"
                           value="${param.email}" required
                           maxlength="254"
                           title="Inserire un indirizzo email valido">
                </div>

                <div class="mb-2">
                    <label for="phoneNumber">Numero di Telefono <span class="required-asterisk">*</span></label>
                    <input type="tel" id="phoneNumber" name="phoneNumber"
                           value="${param.phoneNumber}" required
                           pattern="^\d{10}$"
                           title="Inserire un numero di telefono valido (10 cifre)">
                    <small class="form-helper-text">
                        Formato: 10 cifre senza spazi
                    </small>
                </div>

                <div class="mb-2">
                    <label for="password">Password <span class="required-asterisk">*</span></label>
                    <div class="password-wrapper">
                        <input type="password" id="password" name="password" required
                               pattern="^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\W_]).{8,}$"
                               title="La password deve contenere almeno 8 caratteri, una maiuscola, una minuscola, un numero e un carattere speciale">
                        <i class="fas fa-eye toggle-password" data-target="password"></i>
                    </div>
                    <small class="form-helper-text">
                        Minimo 8 caratteri: una maiuscola, una minuscola, un numero e un carattere speciale
                    </small>
                </div>

                <div class="mb-2">
                    <label for="confirmPassword">Conferma Password <span class="required-asterisk">*</span></label>
                    <div class="password-wrapper">
                        <input type="password" id="confirmPassword" name="confirmPassword" required
                               title="Le password devono coincidere">
                        <i class="fas fa-eye toggle-password" data-target="confirmPassword"></i>
                    </div>
                </div>

                <div class="mb-2">
                    <label for="role">Ruolo <span class="required-asterisk">*</span></label>
                    <select id="role" name="role" required>
                        <option value="">-- Seleziona il tuo ruolo --</option>
                        <option value="Performer" ${param.role == 'Performer' ? 'selected' : ''}>
                            Performer
                        </option>
                        <option value="CastingDirector" ${param.role == 'CastingDirector' ? 'selected' : ''}>
                            Casting Director
                        </option>
                        <option value="ProductionManager" ${param.role == 'ProductionManager' ? 'selected' : ''}>
                            Production Manager
                        </option>
                    </select>
                </div>
            </div>

            <div id="performerFields" class="performer-fields">
                <h3 class="registration-section-title performer-section-title">
                    <i class="fas fa-star"></i> Profilo Artistico
                </h3>

                <div class="mb-2">
                    <label for="gender">Genere</label>
                    <select id="gender" name="gender">
                        <option value="">-- Seleziona --</option>
                        <option value="M" ${param.gender == 'M' ? 'selected' : ''}>Maschile</option>
                        <option value="F" ${param.gender == 'F' ? 'selected' : ''}>Femminile</option>
                        <option value="Altro" ${param.gender == 'Altro' ? 'selected' : ''}>Altro</option>
                    </select>
                </div>

                <div class="mb-2">
                    <label for="category">Categoria Professionale</label>
                    <select id="category" name="category">
                        <option value="">-- Seleziona la tua categoria --</option>
                        <option value="Attore_Attrice" ${param.category == 'Attore_Attrice' ? 'selected' : ''}>Attore/Attrice</option>
                        <option value="Musicista" ${param.category == 'Musicista' ? 'selected' : ''}>Musicista</option>
                        <option value="Cantante" ${param.category == 'Cantante' ? 'selected' : ''}>Cantante</option>
                        <option value="Ballerino" ${param.category == 'Ballerino' ? 'selected' : ''}>Ballerino/a</option>
                        <option value="Doppiatore_trice" ${param.category == 'Doppiatore_trice' ? 'selected' : ''}>Doppiatore/trice</option>
                        <option value="Qualsiasi" ${param.category == 'Qualsiasi' ? 'selected' : ''}>Qualsiasi</option>
                    </select>
                </div>

                <div class="mb-2">
                    <label for="profilePhoto">Foto Profilo</label>
                    <input type="file" id="profilePhoto" name="profilePhoto"
                           accept="image/jpeg,image/jpg,image/png"
                           title="Formati supportati: JPG, PNG (max 10MB)">
                    <small class="form-helper-text">
                        Formati: JPG, PNG - Dimensione massima: 10MB
                    </small>
                </div>

                <div class="mb-2">
                    <label for="cvFile">Curriculum Vitae (PDF) <span class="required-asterisk">*</span></label>
                    <input type="file" id="cvFile" name="cvFile"
                           accept="application/pdf"
                           title="Formato PDF, max 10MB">
                    <small class="form-helper-text">
                        Formato: PDF - Dimensione massima: 10MB
                    </small>
                </div>

                <div class="mb-2">
                    <label for="description">Presentazione Personale</label>
                    <textarea id="description" name="description" rows="4"
                              placeholder="Raccontaci di te, delle tue esperienze e delle tue aspirazioni..."
                              maxlength="1000">${param.description}</textarea>
                </div>
            </div>

            <button type="submit" class="btn btn-primary btn-block btn-lg">
                <i class="fas fa-user-plus"></i> Conferma Registrazione
            </button>

            <p class="text-center mt-2 login-link-text">
                Hai già un account?
                <a href="${pageContext.request.contextPath}/login" class="login-link">
                    Accedi qui
                </a>
            </p>
        </form>

    </div>
</main>

<jsp:include page="/WEB-INF/components/footer.jsp" />

<script src="${pageContext.request.contextPath}/scripts/register-validation.js"></script>
<script src="${pageContext.request.contextPath}/scripts/form-utils.js"></script>
<script src="${pageContext.request.contextPath}/scripts/mobile-menu.js"></script>

</body>
</html>