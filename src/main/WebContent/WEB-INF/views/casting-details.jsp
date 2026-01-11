<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="${casting.title} - Dettagli" scope="request"/>

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

<main class="container mt-3 mb-3">

    <div class="mb-3">
        <a href="javascript:history.back()" class="btn btn-secondary btn-sm">
            <i class="fas fa-arrow-left"></i> Torna indietro
        </a>
    </div>

    <div class="casting-header-section">
        <span class="casting-category-badge">${casting.category}</span>
        <h1 class="casting-big-title">${casting.title}</h1>
        <div class="casting-production-subtitle">
            Produzione: <strong>${productionTitle}</strong>
        </div>
    </div>

    <div class="casting-detail-grid">

        <div class="detail-content">
            <h3 class="detail-section-title">Descrizione e Requisiti</h3>
            <div class="detail-description-text">
                ${casting.description}
            </div>
        </div>

        <aside class="info-sidebar-card">
            <h4 class="info-sidebar-title"><i class="fas fa-info-circle"></i> Riepilogo</h4>

            <div class="info-item">
                <i class="fas fa-map-marker-alt info-icon"></i>
                <div>
                    <span class="info-label">Luogo</span>
                    <span class="info-value">${casting.location}</span>
                </div>
            </div>

            <div class="info-item">
                <i class="far fa-calendar-check info-icon"></i>
                <div>
                    <span class="info-label">Pubblicato il</span>
                    <span class="info-value">${casting.publishDate.toLocalDate()}</span>
                </div>
            </div>

            <div class="info-item">
                <i class="far fa-clock info-icon" style="color: var(--color-status-error);"></i>
                <div>
                    <span class="info-label">Scadenza Candidature</span>
                    <span class="info-value text-danger">${casting.deadline.toLocalDate()}</span>
                </div>
            </div>

            <hr class="sidebar-divider">

            <div class="action-area">
                <c:choose>

                    <%-- CASO 1: Già Candidato --%>
                    <c:when test="${alreadyApplied}">
                        <button class="btn btn-lg btn-disabled" disabled>
                            <i class="fas fa-check-circle"></i> Candidatura Inviata
                        </button>
                        <a href="${pageContext.request.contextPath}/performer/applications" class="link-secondary">
                            Gestisci le mie candidature
                        </a>
                    </c:when>

                    <%-- CASO 2: Performer Loggato --%>
                    <c:when test="${sessionScope.user.role == 'Performer'}">
                        <a href="${pageContext.request.contextPath}/performer/review-application?id=${casting.castingID}"
                           class="btn btn-primary btn-lg btn-block">
                            Candidati Ora
                        </a>
                    </c:when>

                    <%-- CASO 3: Utente Ospite --%>
                    <c:when test="${empty sessionScope.user}">
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary btn-lg btn-block">
                            Accedi per Candidarti
                        </a>
                        <p style="font-size: 0.85rem; color: #666; margin-top: 10px;">
                            Non hai un account? <a href="${pageContext.request.contextPath}/register">Registrati</a>
                        </p>
                    </c:when>

                    <%-- CASO 4: Staff (CD o PM) --%>
                    <c:otherwise>
                        <span class="text-secondary" style="font-style: italic;">
                            Visualizzazione Staff (Non puoi candidarti)
                        </span>
                    </c:otherwise>
                </c:choose>
            </div>
        </aside>

    </div>

</main>

<jsp:include page="/WEB-INF/components/footer.jsp" />
</body>
</html>