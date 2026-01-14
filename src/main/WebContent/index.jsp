<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home | Audire </title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/home.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

<jsp:include page="/WEB-INF/components/header.jsp" />

<main class="container mt-3">

    <div class="home-hero">
        <h1>Benvenuti su Audire</h1>
        <p>La piattaforma per connettere talenti e produzioni. Scopri i casting aperti oggi.</p>
    </div>

    <div class="section-title">
        <h2><i class="fas fa-star"></i> Casting In Evidenza</h2>
    </div>

    <div class="casting-grid">
        <c:choose>
            <%-- SE CI SONO CASTING --%>
            <c:when test="${not empty activeCastings}">
                <c:forEach var="c" items="${activeCastings}">

                    <article class="casting-card-public">
                        <div class="card-header">
                            <span class="card-category">${c.category}</span>
                            <h3 class="card-title">
                                <a href="${pageContext.request.contextPath}/casting-details?id=${c.castingID}" style="text-decoration: none; color: inherit;">
                                        ${c.title}
                                </a>
                            </h3>
                        </div>

                        <div class="card-body">
                            <div class="production-info">
                                <i class="fas fa-video"></i> ${productionTitles[c.castingID]}
                            </div>

                            <p class="card-desc">
                                    ${c.description}
                            </p>

                            <div class="card-meta">
                                <span><i class="fas fa-map-marker-alt"></i> ${c.location}</span>
                                <span class="text-danger">
                                    <i class="far fa-clock"></i> Scade: ${c.deadline.toLocalDate()}
                                </span>
                            </div>
                        </div>

                        <div class="card-footer">
                            <c:choose>
                                <%-- 1. UTENTE PERFORMER: Bottone Candidati --%>
                                <c:when test="${sessionScope.user.role == 'Performer'}">
                                    <a href="${pageContext.request.contextPath}/performer/review-application?id=${c.castingID}"
                                       class="btn btn-apply">
                                        Candidati Ora
                                    </a>
                                </c:when>

                                <%-- 2. UTENTE OSPITE (Non loggato): Invito al login --%>
                                <c:when test="${empty sessionScope.user}">
                                    <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary btn-block">
                                        Accedi per Candidarti
                                    </a>
                                </c:when>

                                <%-- 3. ALTRI RUOLI (PM, CD): Bottone Dettagli (o niente) --%>
                                <c:otherwise>
                                    <span class="text-secondary" style="font-size: 0.9rem;">
                                        <i class="fas fa-info-circle"></i> Solo per Performer
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </article>

                </c:forEach>
            </c:when>

            <%-- SE NON CI SONO CASTING --%>
            <c:otherwise>
                <div class="col-12 text-center p-5" style="grid-column: 1 / -1;">
                    <i class="fas fa-search fa-3x text-secondary mb-3"></i>
                    <h3>Nessun casting attivo al momento.</h3>
                    <p>Torna a trovarci presto!</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</main>

<jsp:include page="/WEB-INF/components/footer.jsp" />
<script src="${pageContext.request.contextPath}/scripts/mobile-menu.js"></script>

</body>
</html>