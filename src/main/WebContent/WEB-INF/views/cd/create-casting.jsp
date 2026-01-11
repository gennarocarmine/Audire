<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Nuovo Annuncio Casting" scope="request"/>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle}</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/casting.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

<jsp:include page="/WEB-INF/components/header.jsp" />

<main class="container mt-3 mb-3 casting-container">

    <div class="casting-card">

        <div class="casting-header">
            <div class="casting-icon">
                <i class="fas fa-bullhorn"></i>
            </div>
            <h2 class="form-title">Pubblica Nuovo Casting</h2>
            <p class="form-subtitle">Crea un annuncio per trovare i talenti per la tua produzione.</p>
        </div>

        <c:if test="${not empty errors}">
            <div class="alert-danger mb-2">
                <c:forEach var="error" items="${errors}">
                    <p>${error}</p>
                </c:forEach>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/cd/create-casting" method="post" id="castingForm">

            <div class="form-grid">

                <div class="form-group full-width">
                    <label for="productionID" class="form-label">Produzione <span class="required-asterisk">*</span></label>
                    <select name="productionID" id="productionID" class="form-select" required>
                        <option value="">-- Seleziona il progetto --</option>
                        <c:forEach var="prod" items="${myProductions}">
                            <option value="${prod.productionID}">${prod.title} (${prod.type})</option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty myProductions}">
                        <small class="text-danger">Attenzione: Non sei assegnato a nessuna produzione attiva.</small>
                    </c:if>
                </div>

                <div class="form-group full-width">
                    <label for="title" class="form-label">Titolo Annuncio <span class="required-asterisk">*</span></label>
                    <input type="text" id="title" name="title" class="form-input" required
                           placeholder="Es. Cercasi attore protagonista per thriller">
                </div>

                <div class="form-group">
                    <label for="category" class="form-label">Categoria Ruolo <span class="required-asterisk">*</span></label>
                    <select name="category" id="category" class="form-select" required>
                        <option value="">-- Seleziona --</option>
                        <option value="Attore_Attrice">Attore / Attrice</option>
                        <option value="Musicista">Musicista</option>
                        <option value="Doppiatore_trice">Doppiatore / Doppiatrice</option>
                        <option value="Ballerino">Ballerino / Ballerina</option>
                        <option value="Cantante">Cantante</option>
                        <option value="Qualsiasi">Altro</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="deadline" class="form-label">Scadenza Candidature <span class="required-asterisk">*</span></label>
                    <input type="date" id="deadline" name="deadline" class="form-input" required>
                </div>

                <div class="form-group full-width">
                    <label for="location" class="form-label">Luogo (o Remoto) <span class="required-asterisk">*</span></label>
                    <input type="text" id="location" name="location" class="form-input" required
                           placeholder="Es. Roma, Milano, o 'Casting Online'">
                </div>

                <div class="form-group full-width">
                    <label for="description" class="form-label">Descrizione e Requisiti <span class="required-asterisk">*</span></label>
                    <textarea id="description" name="description" class="form-textarea" required
                              placeholder="Descrivi il ruolo, l'età scenica, caratteristiche fisiche, skill richieste..."></textarea>
                    <small class="char-counter">Minimo 20 caratteri</small>
                </div>

            </div>

            <button type="submit" class="btn btn-primary btn-block btn-lg"
            ${empty myProductions ? 'disabled' : ''}>
                <i class="fas fa-paper-plane"></i> Pubblica Ora
            </button>

            <div class="text-center mt-2">
                <a href="${pageContext.request.contextPath}/cd/dashboard" class="login-link">Annulla</a>
            </div>

        </form>
    </div>

</main>

<jsp:include page="/WEB-INF/components/footer.jsp" />

<script src="${pageContext.request.contextPath}/scripts/form-utils.js"></script>
<script src="${pageContext.request.contextPath}/scripts/casting-validation.js"></script>

</body>
</html>