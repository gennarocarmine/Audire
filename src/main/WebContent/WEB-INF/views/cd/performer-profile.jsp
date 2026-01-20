<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Profilo: ${performerUser.firstName} ${performerUser.lastName}" scope="request"/>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle}</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/casting.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/profile.css">

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

<jsp:include page="/WEB-INF/components/header.jsp" />

<main class="container mt-3 mb-3">

    <div class="page-header">
        <h2 class="page-title"><i class="fas fa-id-card"></i> Scheda Candidato</h2>
        <a href="javascript:history.back()" class="btn btn-secondary btn-sm">
            <i class="fas fa-arrow-left"></i> Torna alla lista
        </a>
    </div>

    <div class="profile-grid-layout">

        <div class="casting-card profile-card">

            <div class="profile-header">
                <div class="profile-avatar-wrapper">
                    <c:choose>
                        <c:when test="${not empty performer.profilePhoto}">
                            <img src="${pageContext.request.contextPath}/uploads/${performer.profilePhoto}"
                                 class="profile-avatar-img" alt="Foto Profilo">
                        </c:when>
                        <c:otherwise>
                            <div class="profile-avatar-placeholder">
                                <i class="fas fa-user"></i>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="profile-info-content">
                    <h3>${performerUser.firstName} ${performerUser.lastName}</h3>
                    <div class="profile-category">${performer.category}</div>

                    <div class="profile-contact-row">
                        <i class="fas fa-envelope"></i>
                        <a href="mailto:${performerUser.email}" class="profile-email-link">${performerUser.email}</a>
                    </div>
                    <div class="profile-contact-row">
                        <i class="fas fa-phone"></i>
                        <span>${performerUser.phoneNumber}</span>
                    </div>
                </div>
            </div>

            <hr>

            <h4 class="form-title">Presentazione & Caratteristiche</h4>
            <div class="profile-bio-box">
                <p class="profile-bio-text">${performer.description}</p>
                <c:if test="${not empty performer.gender}">
                    <p><strong>Genere:</strong> ${performer.gender}</p>
                </c:if>
            </div>

            <h4 class="form-title">Curriculum Vitae</h4>
            <c:choose>
                <c:when test="${not empty performer.cvData}">
                    <a href="${pageContext.request.contextPath}/download-cv?id=${performer.performerID}"
                       class="btn btn-primary">
                        <i class="fas fa-file-pdf"></i> Scarica CV Completo
                    </a>
                </c:when>
                <c:otherwise>
                    <p class="cv-missing-msg">
                        <i class="fas fa-exclamation-circle"></i> Nessun CV caricato.
                    </p>
                </c:otherwise>
            </c:choose>

        </div>

        <c:if test="${not empty application}">
            <div class="casting-card evaluation-card">
                <h4 class="evaluation-title">
                    <i class="fas fa-clipboard-check"></i> Valutazione
                </h4>

                <form action="${pageContext.request.contextPath}/cd/update-status" method="post">
                    <input type="hidden" name="appId" value="${application.applicationID}">

                    <div class="form-group">
                        <label class="form-label">Stato Candidatura</label>
                        <select name="status" class="form-select status-select">
                            <option value="In_attesa" ${application.status == 'In_attesa' ? 'selected' : ''}>In Attesa</option>
                            <option value="Shortlist" class="opt-shortlist" ${application.status == 'Shortlist' ? 'selected' : ''}>Shortlist</option>
                            <option value="Selezionata" class="opt-selected" ${application.status == 'Selezionata' ? 'selected' : ''}>Selezionata</option>
                            <option value="Rifiutata" class="opt-rejected" ${application.status == 'Rifiutata' ? 'selected' : ''}>Rifiutata</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Feedback / Note Interne</label>
                        <textarea name="feedback" class="form-textarea" rows="5"
                                  placeholder="Scrivi qui note sul provino o feedback per il candidato...">${application.feedback}</textarea>
                    </div>

                    <button type="submit" class="btn btn-primary btn-block">
                        Aggiorna Valutazione
                    </button>
                </form>
            </div>
        </c:if>

    </div>

</main>

<jsp:include page="/WEB-INF/components/footer.jsp" />
</body>
</html>