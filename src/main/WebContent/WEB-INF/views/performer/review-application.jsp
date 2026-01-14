<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Riepilogo Candidatura" scope="request"/>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle}</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/casting.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/application.css">

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

<jsp:include page="/WEB-INF/components/header.jsp" />

<main class="container mt-3 mb-3 casting-container">

    <div class="casting-card review-card">

        <div class="text-center mb-3">
            <h2 class="form-title">Conferma Candidatura</h2>
            <p class="form-subtitle">Controlla le informazioni prima di inviare.</p>
        </div>

        <div class="form-grid">

            <div class="review-box review-box-casting">
                <h4 class="review-section-title">
                    <i class="fas fa-bullhorn"></i> Stai applicando per:
                </h4>
                <p><strong>Titolo:</strong> ${casting.title}</p>
                <p><strong>Produzione:</strong> ${productionTitle}</p>
                <p><strong>Categoria:</strong> <span class="review-badge">${casting.category}</span></p>
                <p style="margin-bottom: 0;"><strong>Luogo:</strong> ${casting.location}</p>
            </div>

            <div class="review-box review-box-user">
                <h4 class="review-section-title">
                    <i class="fas fa-user-check"></i> Dati che verranno inviati:
                </h4>

                <div class="user-summary-container">
                    <c:choose>
                        <c:when test="${not empty performer.profilePhoto}">
                            <img src="${pageContext.request.contextPath}/uploads/${performer.profilePhoto}"
                                 alt="Foto Profilo" class="review-avatar-img">
                        </c:when>
                        <c:otherwise>
                            <div class="review-avatar-placeholder">
                                <i class="fas fa-user"></i>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="user-details">
                        <h4>${sessionScope.user.firstName} ${sessionScope.user.lastName}</h4>

                        <p class="user-contact-row">
                            <i class="fas fa-envelope"></i> ${sessionScope.user.email}
                        </p>

                        <p class="user-contact-row">
                            <i class="fas fa-phone"></i> ${sessionScope.user.phoneNumber}
                        </p>
                    </div>
                </div>

                <hr class="review-divider">

                <p><strong>Documentazione:</strong>
                    <span class="doc-status-success">
                        <i class="fas fa-check-circle"></i> Curriculum Vitae allegato
                    </span>
                </p>

                <div class="doc-link-wrapper">
                    <a href="${pageContext.request.contextPath}/download-cv?id=${performer.performerID}"
                       target="_blank" class="doc-link">
                        <i class="fas fa-file-pdf"></i> Visualizza il file che verrà inviato
                    </a>
                </div>
            </div>

        </div>

        <form action="${pageContext.request.contextPath}/performer/apply" method="post">
            <input type="hidden" name="id" value="${casting.castingID}">

            <button type="submit" class="btn btn-primary btn-block btn-lg">
                <i class="fas fa-paper-plane"></i> Conferma e Invia Candidatura
            </button>
        </form>

        <div class="text-center mt-2">
            <a href="${pageContext.request.contextPath}/" class="login-link">Annulla e torna alla Home</a>
        </div>

    </div>

</main>

<jsp:include page="/WEB-INF/components/footer.jsp" />
</body>
</html>